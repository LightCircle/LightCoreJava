package cn.alphabets.light.model;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
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
    public static List<?> parseList(List<?> list) {

        return list.stream().map((x) -> {
            if (x instanceof Entity) {
                return toDocument(x);
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
     * @param object object
     * @return document
     */
    public static Document toDocument(Object object) {

        Map<String, PropertyDescriptor> properties = new ConcurrentHashMap<>();
        getProperties(object.getClass(), properties);

        Document documentt = new Document();
        properties.forEach((key, property) -> {
            try {

                Object val = property.getReadMethod().invoke(object);

                if (val instanceof List) {
                    val = parseList((List) val);
                }

                if (val instanceof Entity) {
                    val = toDocument(val);
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
    public static void getProperties(Class clazz, Map<String, PropertyDescriptor> properties) {

        Arrays.stream(clazz.getDeclaredFields()).forEach((x) -> {
            try {

                // The static property is ignored
                if (Modifier.isStatic(x.getModifiers())) {
                    return;
                }

                properties.put(x.getName(), new PropertyDescriptor(x.getName(), clazz));
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