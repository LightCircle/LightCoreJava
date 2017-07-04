package cn.alphabets.light.model.datarider;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OptionsBuilder
 * Created by lilin on 2017/7/4.
 */
class OptionsBuilder {

    // 数据的Key
    private String key;

    // 获取的内容
    private List<String> field;

    // 关联检索的字段
    private String link;

    // 关联的表名
    private String structure;

    private OptionsBuilder(String key, List<String> field, String link, String structure) {
        this.key = key;
        this.field = field;
        this.link = link;
        this.structure = structure;
    }

    /**
     * attach options info accord board info
     *
     * @param handler Context
     * @param result  DB result will be attached
     * @param board   board info
     * @return DB result with options attached
     */
    static Object fetchOptions(Context handler, Object result, ModBoard board) {

        if (!(result instanceof Singular || result instanceof Plural)) {
            return result;
        }

        List<OptionsBuilder> builders = new ArrayList<>();
        board.getSelects().forEach(select -> {
            if (select.getSelect() && StringUtils.isNotEmpty(select.getOption())) {
                builders.add(
                        new OptionsBuilder(select.getKey(), select.getFields(), select.getOption(), select.getLink()));
            }
        });

        Map<String, Map<String, ModCommon>> options = new HashMap<>();

        builders.stream()
                .collect(Collectors.groupingBy(OptionsBuilder::getStructure))
                .forEach((s, consumer) -> {
                    OptionsBuilder.OptionsBuilderGroup group = new OptionsBuilder.OptionsBuilderGroup(consumer);
                    options.put(s, group.build(handler, result));
                });

        // 单个文档的时候
        if (result instanceof Singular) {
            ((Singular) result).options = options.size() == 0 ? null : options;
        }

        // 文档列表的时候
        if (result instanceof Plural) {
            ((Plural) result).options = options.size() == 0 ? null : options;
        }

        return result;
    }


    private Map<String, ModCommon> build(Context handler, Object result) {

        Map<String, ModCommon> option = new HashMap<>();

        if (!(result instanceof Singular || result instanceof Plural)) {
            return option;
        }

        ModStructure structure = CacheManager.INSTANCE.getStructures()
                .stream()
                .filter(s -> s.getSchema().equals(this.structure))
                .findFirst()
                .get();

        // convert to typed value
        TypeConverter converter = new TypeConverter(handler);

        // field type
        String valueType = ((Map<String, Map>) structure.getItems()).get(link).get("type").toString().trim().toLowerCase();

        // collect field value
        List<Object> fieldValues = new ArrayList<>();
        if (result instanceof Singular) {
            Object value = ((Singular) result).item.getFieldValue(key);
            Object converted = converter.convert(valueType, value);
            if (converted instanceof List) {
                fieldValues.addAll((Collection) converter.convert(valueType, value));
            } else {
                fieldValues.add(converter.convert(valueType, value));
            }
        }
        if (result instanceof Plural) {
            ((Plural) result).items.forEach(item -> {
                Object value = ((ModCommon) item).getFieldValue(key);
                Object converted = converter.convert(valueType, value);
                if (converted instanceof List) {
                    fieldValues.addAll((Collection) converter.convert(valueType, value));
                } else {
                    fieldValues.add(converter.convert(valueType, value));
                }
            });
        }

        fieldValues.removeAll(Collections.singleton(null));
        if (fieldValues.size() == 0) {
            return option;
        }

        boolean hasParent = structure.getParent().length() > 0;

        // 组合option的检索条件
        String table = hasParent ? structure.getParent() : this.structure;
        Class clazz = Model.getEntityType(this.structure);
        Bson condition = hasParent
                ? Filters.and(Filters.in(link, fieldValues), Filters.eq("type", this.structure))
                : Filters.in(link, fieldValues);

        // 为了将link字段也选择出来，clone一个field并添加link字段
        List<String> select = new ArrayList<>(field);
        select.add(link);

        // 检索
        Model model = new Model(handler.getDomain(), handler.getCode(), table, clazz);
        model.list(condition, select).forEach(item -> {
            option.put(item.toDocument(true).get(link).toString(), item);
        });

        return option;
    }

    private String getStructure() {
        return this.structure;
    }

    private static class OptionsBuilderGroup {
        private List<OptionsBuilder> list;

        private OptionsBuilderGroup(List<OptionsBuilder> list) {
            this.list = list;
        }

        private Map<String, ModCommon> build(Context handler, Object result) {
            Map<String, ModCommon> option = new HashMap<>();
            List<Map<String, ModCommon>> results = list.stream()
                    .map(qb -> qb.build(handler, result))
                    .collect(Collectors.toList());

            results.forEach(option::putAll);
            return option;
        }
    }
}
