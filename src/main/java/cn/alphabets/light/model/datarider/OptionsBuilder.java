package cn.alphabets.light.model.datarider;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private Map<String, String> conditions;

    // 关联的表名
    private String schema;

    private OptionsBuilder(String key, List<String> field, Map<String, String> conditions, String schema) {
        this.key = key;
        this.field = field;
        this.conditions = conditions;
        this.schema = schema;
    }

    private Map<String, Document> build(Context handler, Object result) {

        Map<String, Document> option = new HashMap<>();
        if (!(result instanceof Singular || result instanceof Plural)) {
            return option;
        }

        // 获取表定义
        ModStructure structure = CacheManager.INSTANCE.getStructures()
                .stream()
                .filter(s -> s.getSchema().equals(this.schema))
                .findFirst()
                .get();

        // convert to typed value
        TypeConverter converter = new TypeConverter(handler);

        Document condition = new Document();

        // 遍历所有定义的条件，将$开头的变量替换成实际的值
        this.conditions.forEach((k, v) -> {

            if (String.valueOf(v).charAt(0) == '$') {
                // 如果条件里的值以$开头，说明是引用，去document里查找引用的值

                String key = (String.valueOf(v)).substring(1);

                // field type
                String defineType = (String) ((Map<String, Map>) structure.getItems()).get(k).get("type");
                final String valueType = (k.equals("_id") ? "ObjectId" : defineType).trim().toLowerCase();

                // 处理单个对象
                if (result instanceof Singular) {
                    Object value = ((Singular) result).item.getFieldValue(key);
                    Object converted = converter.convert(valueType, value);
                    if (converted instanceof List) {
                        condition.put(k, new Document("$in", converted));
                    } else {
                        condition.put(k, converted);
                    }
                }

                // 处理列表对象
                if (result instanceof Plural) {
                    List<Object> values = new ArrayList<>();
                    ((Plural) result).items.forEach(item -> {
                        Object value = ((ModCommon) item).getFieldValue(key);
                        values.add(converter.convert(valueType, value));
                    });

                    condition.put(k, new Document("$in", values));
                }
            } else {

                // 否则直接作为条件
                condition.put(k, v);
            }
        });

        if (condition.size() <= 0) {
            return option;
        }

        boolean hasParent = structure.getParent() != null && structure.getParent().length() > 0;
        if (hasParent) {
            condition.put("type", this.schema);
        }

        String optionKey = this.findOptionKeyName();
        List<String> select = new ArrayList<>(field);
        select.add(optionKey);

        // 检索
        Model model = new Model(handler.getDomain(), handler.getCode(), this.schema, Entity.getEntityType(this.schema));
        model.list(condition, select).forEach(item -> {
            option.put(item.get(optionKey).toString(), item);
        });

        return option;
    }

    private String findOptionKeyName() {
        for (Map.Entry<String, String> entry : this.conditions.entrySet()) {

            if (entry.getValue().charAt(0) != '$') {
                continue;
            }

            if (this.key.equals(entry.getValue().substring(1))) {
                return entry.getKey();
            }
        }
        return "_id";
    }

    private String getSchema() {
        return this.schema;
    }

    // 处理schema单位的options值
    private static class OptionsBuilderGroup {

        private List<OptionsBuilder> builders;

        // schema所属的builders
        private OptionsBuilderGroup(List<OptionsBuilder> builders) {
            this.builders = builders;
        }

        // 逐一检索option值，结果保存到列表中
        private Map<String, Document> build(Context handler, Object result) {
            Map<String, Document> option = new HashMap<>();
            List<Map<String, Document>> results = builders.stream()
                    .map(builder -> builder.build(handler, result))
                    .collect(Collectors.toList());
            results.forEach(option::putAll);
            return option;
        }
    }

    /**
     * attach options info accord board info
     *
     * @param handler Context
     * @param data    DB data will be attached
     * @param board   board info
     * @return DB data with options attached
     */
    static Object fetchOptions(Context handler, Object data, ModBoard board) {

        if (data == null) {
            return null;
        } else if (data instanceof Singular) {
            if (((Singular) data).item == null) {
                return data;
            }
        } else if (data instanceof Plural) {
            if (((Plural) data).items.size() <= 0) {
                return data;
            }
        } else {
            return data;
        }

        List<OptionsBuilder> builders = new ArrayList<>();

        // 遍历选择项目（选择项目设定中包含关联信息）
        board.getSelects().forEach(select -> {

            // 选择，并且设定了关联表的才进行处理
            if (select.getSelect() && StringUtils.isNotEmpty(select.getSchema())) {
                builders.add(new OptionsBuilder(
                        select.getKey(),
                        select.getFields(),
                        (Map<String, String>) select.getConditions(),
                        select.getSchema()));
            }
        });

        // 应该使用document
        Map<String, Map<String, Document>> options = new HashMap<>();

        builders.stream()
                .collect(Collectors.groupingBy(OptionsBuilder::getSchema))
                .forEach((s, builder) -> options.put(s, new OptionsBuilderGroup(builder).build(handler, data)));

        // 单个文档的时候
        if (data instanceof Singular) {
            ((Singular) data).options = options.size() == 0 ? null : options;
        }

        // 文档列表的时候
        if (data instanceof Plural) {
            ((Plural) data).options = options.size() == 0 ? null : options;
        }

        return data;
    }

}
