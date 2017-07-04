package cn.alphabets.light.model.datarider;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import com.mongodb.client.model.Filters;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OptionsBuilder
 * Created by lilin on 2017/7/4.
 */
public class OptionsBuilder {

    //key
    private String key;
    //fields contains by option
    private List<String> field;
    //foreign key
    private String link;
    //option structure
    private String structure;

    /**
     * attach options info accord board info
     *
     * @param handler Context
     * @param result  DB result will be attached
     * @param board   board info
     * @return DB result with options attached
     */
    public static Object fetchOptions(Context handler, Object result, ModBoard board) {

        if (result instanceof ModCommon || result instanceof Plural) {

            List<OptionsBuilder> optionsBuilders = new ArrayList<>();
            board.getSelects().forEach(select -> {
                if (select.getSelect() && StringUtils.isNotEmpty(select.getOption())) {
                    optionsBuilders.add(
                            new OptionsBuilder(
                                    select.getKey(),
                                    select.getFields(),
                                    select.getLink(),
                                    select.getOption()));
                }
            });

            HashMap<String, HashMap<String, ModCommon>> options = new HashMap<>();

            optionsBuilders.stream()
                    .collect(Collectors.groupingBy(OptionsBuilder::getStructure))
                    .forEach((s, optionsBuilders1) -> {
                        OptionsBuilder.OptionsBuilderGroup optionsBuilderGroup = new OptionsBuilder.OptionsBuilderGroup(optionsBuilders1, s);
                        options.put(s, optionsBuilderGroup.build(handler, result));
                    });


//            TODO:
//            if (result instanceof ModCommon) {
//                ((ModCommon) result).setOptions(options.size() == 0 ? null : options);
//            }

            if (result instanceof Plural) {
                ((Plural) result).setOptions(options.size() == 0 ? null : options);
            }
        }
        return result;
    }

    public OptionsBuilder(String key, List<String> field, String link, String structure) {
        this.key = key;
        this.field = field;
        this.link = link;
        this.structure = structure;
    }


    public HashMap<String, ModCommon> build(Context handler, Object result) {
        ModStructure modStructure = CacheManager.INSTANCE.getStructures().stream().filter(s -> s.getSchema().equals(structure)).findFirst().get();

        HashMap<String, ModCommon> option = new HashMap<>();

        //convert to typed value
        TypeConverter convertor = new TypeConverter(handler);

        //field type
        String valueType = ((HashMap<String, HashMap>) modStructure.getItems()).get(link).get("type").toString().trim().toLowerCase();

        //collect field value
        List fieldValues = new ArrayList();
        if (result instanceof ModCommon) {
            Object value = ((ModCommon) result).getFieldValue(key);
            Object converted = convertor.convert(valueType, value);
            if (converted instanceof List) {
                fieldValues.addAll((Collection) convertor.convert(valueType, value));
            } else {
                fieldValues.add(convertor.convert(valueType, value));
            }
        } else if (result instanceof Plural) {
            ((Plural) result).getItems().forEach(item -> {
                Object value = ((ModCommon) item).getFieldValue(key);
                Object converted = convertor.convert(valueType, value);
                if (converted instanceof List) {
                    fieldValues.addAll((Collection) convertor.convert(valueType, value));
                } else {
                    fieldValues.add(convertor.convert(valueType, value));
                }
            });
        } else {
            return option;
        }
        fieldValues.removeAll(Collections.singleton(null));
        if (fieldValues.size() == 0) {
            return option;
        }

        Bson condition;
        String table;
        Class clazz;
        if (modStructure.getParent().length() > 0) {
            table = modStructure.getParent();
            condition = Filters.and(Filters.in(link, fieldValues), Filters.eq("type", structure));
        } else {
            table = structure;
            condition = Filters.in(link, fieldValues);
        }
        clazz = Model.getEntityType(structure);


        Model model = new Model(handler.getDomain(), handler.getCode(), table, clazz);

        model.list(condition, field).forEach(item -> {
            option.put(item.toDocument(true).get(link).toString(), item);
        });
        return option;

    }

    public String getStructure() {
        return structure;
    }

    public static class OptionsBuilderGroup {
        private List<OptionsBuilder> list;
        private String structure;

        public OptionsBuilderGroup(List<OptionsBuilder> list, String structure) {
            this.list = list;
            this.structure = structure;
        }

        public HashMap<String, ModCommon> build(Context handler, Object result) {
            HashMap<String, ModCommon> option = new HashMap<>();
            List<HashMap<String, ModCommon>> results = list.stream().map(qb -> qb.build(handler, result)).collect(Collectors.toList());
            results.forEach(option::putAll);
            return option;
        }
    }
}
