package cn.alphabets.light.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Model
 * Created by lilin on 2016/11/19.
 */
public class Entity implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ModBase.class);

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

        Map<String, PropertyDescriptor> properties = new ConcurrentHashMap<>();
        getProperties(this.getClass(), properties);

        Document documentt = new Document();
        properties.forEach((key, property) -> {
            try {

                Object val = property.getReadMethod().invoke(this);

                if (val instanceof List) {
                    val = parseList((List) val);
                }

                if (val instanceof Entity) {
                    val = ((Entity) val).toDocument();
                }

                documentt.put(key, val);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e);
                throw new RuntimeException(e);
            }
        });

        return documentt;
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

                // If there is an annontion definition, get the defined name
                for (Annotation annotation : x.getDeclaredAnnotations()) {
                    if (annotation instanceof JsonProperty) {
                        key = ((JsonProperty) annotation).value();
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
}
