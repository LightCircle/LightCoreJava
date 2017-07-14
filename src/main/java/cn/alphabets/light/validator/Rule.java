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
import java.util.Map;

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

            Object error = invokeRule(instance, handler, validator);
            if (error != null) {
                errors.add((Document) error);
            }
        });

        logger.debug("End check");
        return errors.size() > 0 ? errors : null;
    }

    public static Object format(Object value, Object sanitize) {

        if (((Map<String, Object>) sanitize).size() <= 0) {
            return value;
        }

        Sanitize instance = new Sanitize();
        return invokeSanitize(instance, value, (Map<String, Object>) sanitize);
    }

    Document required(Context handler, ModValidator rule) {
        Object value = MPath.detectValue(rule.getKey(), handler.params.getJson());
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

        Object value = MPath.detectValue(rule.getKey(), handler.params.getJson());
        if (StringUtils.isNumeric(String.valueOf(value))) {
            return null;
        }

        return this.makeError(rule, value);
    }

    Document matches(Context handler, ModValidator rule) {
        if (this.prepare(handler, rule)) {
            return null;
        }

        Object value = MPath.detectValue(rule.getKey(), handler.params.getJson());

        if (String.valueOf(value).matches((String) rule.getOption())) {
            return null;
        }

        return this.makeError(rule, value);
    }

    private static Object invokeRule(Rule instance, Context handler, ModValidator rule) {
        try {
            Method method = instance.getClass().getDeclaredMethod(rule.getRule(), Context.class, ModValidator.class);
            return method.invoke(instance, handler, rule);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object invokeSanitize(Sanitize instance, Object data, Map<String, Object> sanitize) {
        try {
            Method method = instance.getClass().getDeclaredMethod(
                    (String) sanitize.get("rule"),
                    Object.class,
                    Map.class);

            return method.invoke(instance, data, sanitize);
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
            Object value = MPath.detectValue(rule.getKey(), json);
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
            String requestValue = String.valueOf(MPath.detectValue(rule.getKey(), json));

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
                Object reference = MPath.detectValue(value.substring(1), handler.params.getJson());

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
