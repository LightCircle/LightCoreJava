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
import java.util.*;
import java.util.regex.Pattern;
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

    static void invokeBefore(Context handler, String clazz, List<Document> data) {
        if (clazz == null) {
            return;
        }

        try {
            Method method = Class.forName(clazz).getMethod("before", Context.class, List.class);
            method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find before method. skip. " + clazz);
        }
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

    static void invokeDump(Context handler, String clazz, List<Document> data) {
        if (clazz == null) {
            return;
        }

        try {
            Method method = Class.forName(clazz).getMethod("dump", Context.class, List.class);
            method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find dump method. skip. " + clazz);
        }
    }

    static void fetchLinkData(Context handler, ModEtl.Mappings mapping) {

        Document data = handler.params.getData();

        // 没有定义schema（没有关联），则直接返回
        if (StringUtils.isEmpty(mapping.getSchema())) {
            return;
        }

        // 生成关联检索条件
        Document condition = getCondition(handler, mapping);

        // 选择项目，这里把条件字段一并取出，是为了保存原值到original里（）
        List<String> select = new ArrayList<String>(mapping.getFields());
        condition.forEach((k, v) -> select.add(k));

        Document original = new Document();
        List<Object> newValue = new ArrayList<>();

        Model model = new Model(handler.domain(), handler.code(), mapping.getSchema());
        model.list(condition, select).forEach(item -> {

            // 保存新获取的值到列表里
            Object value = item.get(mapping.getFields().get(0));
            if (value instanceof ObjectId) {
                value = ((ObjectId) value).toHexString();
            }
            newValue.add(value);

            // Original对象是，以新值为key，旧值为value的Hash。为了要获取新值对应的旧值，需要获取用key项目对应的检索条件字段名
            String originalKey = null;
            for (Map.Entry entry : ((Map<String, String>) mapping.getConditions()).entrySet()) {
                if (("$" + mapping.getKey()).equals(entry.getValue())) {
                    originalKey = (String) entry.getKey();
                }
            }

            // 保存原值
            if (originalKey != null) {
                original.put(String.valueOf(value), item.get(originalKey));
            }
        });

        // 替换原来的值
        boolean isListValue = MPath.detectValue(key(mapping), data) instanceof List;
        MPath.setValueByJsonPath(data, Arrays.asList(key(mapping).split(Pattern.quote("."))),
                isListValue ? newValue : newValue.get(0));

        if (original.size() > 0) {
            data.put("_original", original);
        }
    }

    static Document getCondition(Context handler, ModEtl.Mappings mapping) {

        Document condition = new Document("valid", 1);

        // 遍历所有定义的条件{ group: $name, valid: 1 }
        Map<String, String> defines = (Map<String, String>) mapping.getConditions();
        defines.forEach((k, v) -> {

            if (String.valueOf(v).charAt(0) == '$') {
                // 如果条件里的值以$开头，说明是引用，去document里查找引用的值

                String key = (String.valueOf(v)).substring(1);
                Object real = MPath.detectValue(key, handler.params.getData());

                if (real == null) {
                    condition.put(k, null);
                    return;
                }

                if (real instanceof List) {
                    if ("_id".equals(k)) {
                        real = ((List<String>) real)
                                .stream()
                                .filter(Objects::nonNull)
                                .map(ObjectId::new)
                                .collect(Collectors.toList());
                    }
                    condition.put(k, new Document("$in", real));
                } else {
                    condition.put(k, "_id".equals(k) ? new ObjectId((String) real) : real);
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
