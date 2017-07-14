package cn.alphabets.light.validator;

import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MPath
 * Created by lilin on 2017/7/12.
 */
public class MPath {

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
    public static Object detectValue(String path, Object data) {
        if (data instanceof Document) {
            return detectValueFromDocument(path, (Document) data);
        }

        if (data instanceof List) {
            return detectValueFromList(path, (List<Object>) data);
        }

        return null;
    }

    /**
     * Use Json Path to set the value of the Json object. Supports embedded objects
     *
     * @param source target json object
     * @param path   json path
     * @param val    value
     */
    @SuppressWarnings("unchecked")
    public static void setValueByJsonPath(Document source, List<String> path, Object val) {

        Object parent = source;

        for (int i = 0; i < path.size(); i++) {

            String key = path.get(i);
            boolean isLast = (i == path.size() - 1);

            if (isLast) {
                boolean isList = key.equals("") || NumberUtils.isDigits(key);
                if (isList) {
                    ((List<Object>) parent).add(val);
                } else {
                    ((Document) parent).put(key, val);
                }
                return;
            }

            boolean isListValue = path.get(i + 1).equals("") || NumberUtils.isDigits(path.get(i + 1));
            boolean isObjectValue = !isListValue;

            if (NumberUtils.isDigits(key)) {

                if (((List<?>) parent).size() > Integer.parseInt(key)) {
                    parent = ((List<?>) parent).get(Integer.parseInt(key));
                } else {
                    if (isListValue) {
                        ((List<Object>) parent).add(new ArrayList<>());
                    }
                    if (isObjectValue) {
                        ((List<Document>) parent).add(new Document());
                    }
                    parent = ((List<?>) parent).get(Integer.parseInt(key));
                }
            } else {

                if (((Document) parent).containsKey(key)) {
                    parent = ((Document) parent).get(key);
                } else {
                    if (isListValue) {
                        ((Document) parent).put(key, new ArrayList<>());
                    }
                    if (isObjectValue) {
                        ((Document) parent).put(key, new Document());
                    }
                    parent = ((Document) parent).get(key);
                }
            }
        }
    }

    private static Object detectValueFromDocument(String path, Document data) {

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
            return detectValueFromDocument(residue, (Document) value);
        }

        if (value instanceof List) {
            return detectValueFromList(residue, (List<Object>) value);
        }

        return null;
    }

    private  static Object detectValueFromList(String path, List<Object> data) {
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
            return detectValueFromDocument(residue, (Document) value);
        }

        if (value instanceof List) {
            return detectValueFromList(residue, (List<Object>) value);
        }

        return null;
    }

}
