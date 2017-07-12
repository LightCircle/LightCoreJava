package cn.alphabets.light.validator;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModValidator;
import cn.alphabets.light.http.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rule
 * Created by lilin on 2017/7/11.
 */
public class Rule {

    private static final Logger logger = LoggerFactory.getLogger(Rule.class);

    public static List<Document> isValid(Context handler, String group) {

        logger.debug("Start checking");

        List<ModValidator> validators = CacheManager.INSTANCE.getValidators();
        List<Document> errors = new ArrayList<>();

        Rule instance = new Rule();
        validators.forEach(validator -> {
            if (!validator.getGroup().equals(group)) {
                return;
            }

            Object error = invoke(instance, handler, validator);
            if (error != null) {
                errors.add((Document) error);
            }
        });

        logger.debug("End check");
        return errors.size() > 0 ? errors : null;
    }

    Document required(Context handler, ModValidator rule) {
        Object value = this.detectValue(rule.getKey(), handler.params.getJson());
        if (value == null) {
            return this.makeError(rule);
        }

        if (value instanceof String) {
            if (((String) value).trim().equals("")) {
                return this.makeError(rule, value);
            }
        }

        return null;
    }

    Document unique(Context handler, ModValidator rule) {
        if (this.prepare(handler, rule)) {
            return null;
        }

        long count = fetchCount(handler, rule);
        if (count > 0) {
            return this.makeError(rule);
        }

        return null;
    }

    Document exists(Context handler, ModValidator rule) {
        if (this.prepare(handler, rule)) {
            return null;
        }

        long count = fetchCount(handler, rule);
        if (count <= 0) {
            return this.makeError(rule);
        }

        return null;
    }

    Document numeric(Context handler, ModValidator rule) {
        if (this.prepare(handler, rule)) {
            return null;
        }

        Object value = this.detectValue(rule.getKey(), handler.params.getJson());
        if (StringUtils.isNumeric(String.valueOf(value))) {
            return null;
        }

        return this.makeError(rule, value);
    }

    Document matches(Context handler, ModValidator rule) {
        if (this.prepare(handler, rule)) {
            return null;
        }

        Object value = this.detectValue(rule.getKey(), handler.params.getJson());

        if (String.valueOf(value).matches((String) rule.getOption())) {
            return null;
        }

        return this.makeError(rule, value);
    }

    private static Object invoke(Rule instance, Context handler, ModValidator rule) {
        try {
            Method method = instance.getClass().getDeclaredMethod(rule.getRule(), Context.class, ModValidator.class);
            return method.invoke(instance, handler, rule);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 校验方法预处理
     *
     * @param handler context
     * @param rule    规则
     * @return 不进行校验直接返回，方法返回true（视做正常）
     */
    boolean prepare(Context handler, ModValidator rule) {

        Document json = handler.params.getJson();

        // 非严格模式，值为空时不认为异常
        if (!rule.getStrict()) {
            Object value = this.detectValue(rule.getKey(), json);
            if (value == null) {
                return true;
            }

            if (value instanceof String && ((String) value).trim().equals("")) {
                return true;
            }
        }

        // 进行校验的前提条件判断（如果不满足条件则不进行校验）
        if (rule.getCondition() != null && rule.getCondition().getKey() != null) {

            // 请求参数里的值
            String requestValue = String.valueOf(this.detectValue(rule.getKey(), json));

            // 设定的条件值
            String compareValue = rule.getCondition().getParameter();
            switch (rule.getCondition().getParameter()) {
                case "$eq":
                    return compareValue.equals(requestValue);
                case "$ne":
                    return !compareValue.equals(requestValue);
                case "$gt":
                    return compareValue.compareTo(requestValue) < 0;
                case "$gte":
                    return compareValue.compareTo(requestValue) <= 0;
                case "$lt":
                    return compareValue.compareTo(requestValue) > 0;
                case "$lte":
                    return compareValue.compareTo(requestValue) >= 0;
            }
        }

        return false;
    }

    /**
     * 获取指定条件的数据件数
     *
     * @param handler context
     * @param rule    条件
     * @return 数据件数
     */
    long fetchCount(Context handler, ModValidator rule) {

        Document condition = new Document();
        Document option = (Document) rule.getOption();

        List<Document> conditions = (List<Document>) option.get("conditions");
        conditions.forEach(item -> {

            String parameter = item.getString("parameter");
            String value = item.getString("value");

            // 参数为引用类型的（第一个字母为$），那么在handler.params里取值做为条件
            if (value.charAt(0) == '$') {
                Object reference = this.detectValue(value.substring(1), handler.params.getJson());

                if (reference instanceof String) {

                    // 合法的ObjectId，那么进行转换
                    if (parameter.equals("_id") && ObjectId.isValid((String) reference)) {
                        reference = new ObjectId((String) reference);
                    }
                    condition.put(parameter, reference);
                    return;
                }

                // 列表类型，则使用mongo的$in操作符
                if (reference instanceof List) {
                    condition.put(parameter, new Document("$in", reference));
                    return;
                }
            }

            condition.put(parameter, value);
        });

        return new Model(handler.domain(), handler.code(), option.getString("schema")).count(condition);
    }

    /**
     * 通过指定的 key path 获取值嵌套Document中的值，支持数组类型。类似于node中的mpath
     * <p>
     * 假设key=a.1.b，先通过.符号分隔成多个segment
     * 1. 当父节点的值不是数组时，返回对象parent.segment
     * 2. 当父节点的值是数组类型
     * 2.1 如果segment是整数 将父节点替换为 parent[segment]
     * 2.2 如果segment不是整数 遍历数组并获取parent.segment替换数组内容
     *
     * @param path 路径
     * @param data 数据，可以使文档也可以是文档列表
     * @return 解析的值
     */
    Object detectValue(String path, Object data) {
        if (data instanceof Document) {
            return this.detectValueFromDocument(path, (Document) data);
        }

        if (data instanceof List) {
            return this.detectValueFromList(path, (List<Object>) data);
        }

        return null;
    }

    private Object detectValueFromDocument(String path, Document data) {

        String[] keys = path.split("\\.");
        if (keys.length < 1) {
            return null;
        }

        String key = keys[0];
        Object value = data.get(key);
        if (keys.length == 1 || value == null) {
            return value;
        }

        String residue = path.replace(key + ".", "");
        if (value instanceof Document) {
            return this.detectValueFromDocument(residue, (Document) value);
        }

        if (value instanceof List) {
            return this.detectValueFromList(residue, (List<Object>) value);
        }

        return null;
    }

    private Object detectValueFromList(String path, List<Object> data) {
        String[] keys = path.split("\\.");
        if (keys.length < 1) {
            return null;
        }

        String key = keys[0];
        Object value;
        if (key.matches("^\\d+$")) {
            value = data.get(Integer.parseInt(key));
        } else {
            value = data.stream().map(item -> {
                if (item instanceof Document) {
                    return ((Document) item).get(key);
                }
                return null;
            }).collect(Collectors.toList());
        }

        if (keys.length == 1 || value == null) {
            return value;
        }

        String residue = path.replace(key + ".", "");
        if (value instanceof Document) {
            return this.detectValueFromDocument(residue, (Document) value);
        }

        if (value instanceof List) {
            return this.detectValueFromList(residue, (List<Object>) value);
        }

        return null;
    }

    private Document makeError(ModValidator rule) {
        return makeError(rule, null);
    }

    private Document makeError(ModValidator rule, Object value) {
        Document error = new Document();
        error.put("name", rule.getName());
        error.put("message", rule.getMessage());
        error.put("value", value);
        return error;
    }

}
