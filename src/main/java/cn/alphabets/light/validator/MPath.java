package cn.alphabets.light.validator;

import org.bson.Document;

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
