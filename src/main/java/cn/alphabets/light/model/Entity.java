package cn.alphabets.light.model;

import cn.alphabets.light.http.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Model
 * Created by lilin on 2016/11/19.
 */
@JsonPropertyOrder(alphabetic = true)
public class Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);

    @JsonIgnore
    private static ObjectMapper objectMapper = new ObjectMapper();

    //ObjectMapper for different timezone
    @JsonIgnore
    private static ConcurrentHashMap<TimeZone, ObjectMapper> objectMappers = new ConcurrentHashMap<>();


    /**
     * Converts list
     *
     * @param list list
     * @return result
     */
    private List<?> parseList(List<?> list) {

        return list.stream().map((x) -> {
            if (x instanceof Entity) {
                return ((Entity) x).toDocument();
            }

            if (x instanceof List) {
                return parseList((List) x);
            }

            return x;
        }).collect(Collectors.toList());
    }

    /**
     * Converts an object to a Document
     *
     * @return document
     */
    public Document toDocument() {
        return toDocument(false);
    }

    public Document toDocument(boolean ignoreNullValue) {

        Map<String, PropertyDescriptor> properties = new ConcurrentHashMap<>();
        getProperties(this.getClass(), properties);

        Document document = new Document();
        properties.forEach((key, property) -> {
            try {

                Object val = property.getReadMethod().invoke(this);

                if (val instanceof List) {
                    val = parseList((List) val);
                }

                if (val instanceof Entity) {
                    val = ((Entity) val).toDocument();
                }

                if (!ignoreNullValue || val != null) {
                    document.put(key, val);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        });

        return document;
    }

    /**
     * Gets all the class properties, including the parent class
     *
     * @param clazz      class type
     * @param properties result
     */
    private static void getProperties(Class clazz, Map<String, PropertyDescriptor> properties) {

        Arrays.stream(clazz.getDeclaredFields()).forEach((x) -> {
            try {

                // The static property is ignored
                if (Modifier.isStatic(x.getModifiers())) {
                    return;
                }

                String key = x.getName();


                for (Annotation annotation : x.getDeclaredAnnotations()) {
                    // If there is an "JsonProperty" annotation definition, get the defined name
                    if (annotation instanceof JsonProperty) {
                        JsonProperty jp = ((JsonProperty) annotation);
                        if (jp.access() == JsonProperty.Access.READ_ONLY) {
                            return;
                        }
                        key = jp.value();
                    }
                }

                properties.put(key, new PropertyDescriptor(x.getName(), clazz));
            } catch (IntrospectionException e) {

                // Ignore properties that do not have getter and setter methods
                logger.warn(e);
            }
        });

        // Including the parent class
        if (clazz.getSuperclass() != Object.class) {
            getProperties(clazz.getSuperclass(), properties);
        }
    }

    /**
     * Convert Document to a class
     *
     * @param document document
     * @param clazz    class type
     * @param <T>      Entity type
     * @return class
     */
    public static <T extends ModCommon> T fromDocument(Document document, Class<T> clazz) {
        if (document == null) {
            return null;
        }

        try {
            return objectMapper.readValue(document.toJson(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends ModCommon> T fromDocument(Document document, Class<T> clazz, Context handler) {
        if (document == null) {
            return null;
        }

        try {
            TimeZone tz = handler.getTimeZone();
            ObjectMapper mapper = objectMappers.get(tz);
            if (mapper == null) {
                mapper = new ObjectMapper();
                mapper.setTimeZone(tz);
                objectMappers.put(tz, mapper);
            }
            return mapper.readValue(document.toJson(), clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get field value by name
     * <p>
     * support sub class field like "xx.xxx"
     *
     * @param fieldName field name
     * @return field value
     */
    @JsonIgnore
    public Object getFieldValue(String fieldName) {

        try {
            String fields[] = fieldName.split("\\.");

            Object step = this;

            for (String f : Arrays.asList(fields)) {
                PropertyDescriptor pd =
                        new PropertyDescriptor(Generator.reserved.contains(f) ? f + "_" : f,
                                step.getClass());
                step = pd.getReadMethod().invoke(step);
            }

            return step;
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Error get field value : " + fieldName, e);
        }
        return null;
    }
}
