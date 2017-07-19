package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
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
import java.util.function.Function;
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

    public static <T extends ModCommon> List<T> fromDocument(List<Document> documents, Class<T> clazz, TimeZone tz) {
        if (documents == null || documents.size() <= 0) {
            return null;
        }

        return documents.stream().map(document -> Entity.fromDocument(document, clazz, tz)).collect(Collectors.toList());
    }

    public static <T extends ModCommon> T fromDocument(Document document, Class<T> clazz, TimeZone tz) {
        if (document == null) {
            return null;
        }

        try {
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
     * TODO: 与MPath类的方法合并
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
                if (step == null) return step;
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

    /**
     * 获取Entity类的类型，通过反射生成具体表名对应的类型
     * - 系统表的Entity在 cn.alphabets.light.entity 包下
     * - 而用户表的Entity在 用户包名.entity 下
     *
     * @param structure 表名称
     * @return 类型
     */
    @JsonIgnore
    public static Class getEntityType(String structure) {
        String className = Constant.MODEL_PREFIX + WordUtils.capitalize(structure);

        // 如果前缀是系统表，那么包名称使用 cn.alphabets.light，否则使用用户定义的包名
        String packageName = system.contains(structure)
                ? Constant.DEFAULT_PACKAGE_NAME + ".entity"
                : Environment.instance().getPackages() + ".entity";

        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(Constant.DEFAULT_PACKAGE_NAME + ".entity." + className);
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    @JsonIgnore
    public static List<String> system = Arrays.asList(
            Constant.SYSTEM_DB_BOARD,
            Constant.SYSTEM_DB_CONFIG,
            Constant.SYSTEM_DB_VALIDATOR,
            Constant.SYSTEM_DB_I18N,
            Constant.SYSTEM_DB_STRUCTURE,
            Constant.SYSTEM_DB_BOARD,
            Constant.SYSTEM_DB_ROUTE,
            Constant.SYSTEM_DB_TENANT,
            Constant.SYSTEM_DB_FILE,
            Constant.SYSTEM_DB_ETL,
            Constant.SYSTEM_DB_SETTING,
            Constant.SYSTEM_DB_FUNCTION,
            Constant.SYSTEM_DB_CODE
    );
}
