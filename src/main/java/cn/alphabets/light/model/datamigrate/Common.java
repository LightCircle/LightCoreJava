package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.validator.MPath;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
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
            logger.debug("Did not find initialize method." + clazz);
        }
    }

    static List<Document> invokeBefore(Context handler, String clazz, List<Document> data) {

        if (clazz == null) {
            return null;
        }

        try {
            Method method = Class.forName(clazz).getMethod("initialize", Context.class, List.class);
            return (List<Document>) method.invoke(method.getDeclaringClass().newInstance(), handler, data);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
            logger.debug("Did not find initialize method." + clazz);
        }

        return null;
    }

    static void fetchLinkData(Context handler, ModEtl.Mappings mapping) {

        // 没有定义schema（没有关联），则直接返回
        if (mapping.getSchema() == null) {
            return;
        }

        // 用key，在行数据中找出对应的数据，如果值不是数组则转换成数组
        // 转换成数组是为了后续统一操作，值为数组的场景是：Excel里用逗号分隔的内容，会转变为数组
        List<Object> values;
        Object object = MPath.detectValue(key(mapping), handler.params.getData());
        if (object instanceof List) {
            values = (List<Object>) object;
        } else {
            values = new ArrayList<>();
            values.add(object);
        }

        Model model = new Model(handler.domain(), handler.code(), mapping.getSchema());

        // 遍历所有的值，如果对应的key是_id，那么转换为ObjectId，并最终生成检索用的条件
        values.forEach(value -> {

            Document condition = new Document();

            // 遍历所有定义的条件{ group: $name, valid: 1 }
            Document defines = (Document) mapping.getConditions();
            defines.forEach((k, v) -> {

                if (String.valueOf(v).charAt(0) == '$'){
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

            Document result = model.get(condition);


        });



    }

    static String key(ModEtl.Mappings mapping) {

        String key = mapping.getVariable();
        if (key == null) {
            key = mapping.getKey();
        }

        if (key == null) {
            return "";
        }

        return key;
    }
}
