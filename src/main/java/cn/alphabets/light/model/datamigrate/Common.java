package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.validator.MPath;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Common
 * Created by lilin on 2017/7/12.
 */
public class Common {

    private static final Logger logger = LoggerFactory.getLogger(EtlImporter.class);

    static void invokeInitialize(Context handler, String clazz, Model model) {
        if (clazz == null) {
            return;
        }

        try {
            Method method = Class.forName(clazz).getMethod("initialize", Context.class, Model.class);
            method.invoke(method.getDeclaringClass().newInstance(), handler, model);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find initialize method. skip. " + clazz);
        }
    }

    static List<Document> invokeBefore(Context handler, String clazz, List<Document> data) {
        if (clazz == null) {
            return null;
        }

        try {
            Method method = Class.forName(clazz).getMethod("before", Context.class, List.class);
            return (List<Document>) method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find before method. skip. " + clazz);
        }

        return null;
    }

    static void invokeParse(Context handler, String clazz, Document data) {
        if (clazz == null) {
            return;
        }

        try {
            Method method = Class.forName(clazz).getMethod("parse", Context.class, Document.class);
            method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find parse method. skip. " + clazz);
        }
    }

    static List<Document> invokeValid(Context handler, String clazz, Document data) {
        if (clazz == null) {
            return null;
        }

        try {
            Method method = Class.forName(clazz).getMethod("parse", Context.class, Document.class);
            return (List<Document>) method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find parse method. skip. " + clazz);
        }

        return null;
    }

    static void invokeAfter(Context handler, String clazz, Document data) {
        if (clazz == null) {
            return;
        }

        try {
            Method method = Class.forName(clazz).getMethod("after", Context.class, Document.class);
            method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find after method. skip. " + clazz);
        }
    }

    static void fetchLinkData(Context handler, ModEtl.Mappings mapping) {

        Document data = handler.params.getData();

        // 没有定义schema（没有关联），则直接返回
        if (StringUtils.isEmpty(mapping.getSchema())) {
            return;
        }

        // 用key，在行数据中找出对应的数据，如果值不是数组则转换成数组
        // 转换成数组是为了后续统一操作，值为数组的场景是：Excel里用逗号分隔的内容，会转变为数组
        Object object = MPath.detectValue(key(mapping), data);
        boolean isListValue = object instanceof List;
        List<Object> values = isListValue ? (List<Object>) object : Arrays.asList(object);

        Model model = new Model(handler.domain(), handler.code(), mapping.getSchema());
        Document original = new Document();

        // 遍历所有的值，如果对应的key是_id，那么转换为ObjectId，并最终生成检索用的条件
        values.forEach(value -> {
            Document fetched = model.get(getCondition(handler, mapping), mapping.getFields());
            if (fetched != null) {
                Object fetchedValue = fetched.get(mapping.getFields());
                if (fetchedValue instanceof ObjectId) {
                    fetchedValue = ((ObjectId) fetchedValue).toHexString();
                }

                // 保留原值，原值以新值为Key保存起来，需要的时候可以用新值作为索引获取原来的值（错误处理时使用）
                original.put(String.valueOf(fetchedValue), value);

                // 替换原来的值
                MPath.setValueByJsonPath(data, Arrays.asList(key(mapping).split(".")), fetchedValue);
            }
        });

        data.put("_original", original);
    }

    private static Document getCondition(Context handler, ModEtl.Mappings mapping) {

        Document condition = new Document();

        // 遍历所有定义的条件{ group: $name, valid: 1 }
        Document defines = (Document) mapping.getConditions();
        defines.forEach((k, v) -> {

            if (String.valueOf(v).charAt(0) == '$') {
                // 如果条件里的值以$开头，说明是引用，去document里查找引用的值

                String key = (String.valueOf(v)).substring(1);
                Object real = MPath.detectValue(key, handler.params.getData());

                if (real instanceof List) {
                    if ("_id".equals(key)) {
                        real = ((List<String>) real).stream().map(ObjectId::new).collect(Collectors.toList());
                    }
                    condition.put(key, new Document("$in", real));
                } else {
                    condition.put(k, "_id".equals(key) ? new ObjectId((String) real) : real);
                }
            } else {

                // 否则直接作为条件
                condition.put(k, v);
            }
        });

        return condition;
    }

    static String key(ModEtl.Mappings mapping) {

        String key = mapping.getVariable();
        if (!StringUtils.isEmpty(key)) {
            return key;
        }

        key = mapping.getKey();
        if (!StringUtils.isEmpty(key)) {
            return key;
        }

        return "";
    }
}
