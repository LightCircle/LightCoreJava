package cn.alphabets.light.model.datarider;


import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static cn.alphabets.light.Constant.MODEL_PREFIX;

/**
 * DataRider
 * <p>
 * Created by lilin on 2016/11/12.
 */
public abstract class Rider {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(Rider.class);

    static final List<String> METHOD = Arrays.asList(
            "", "add", "update", "remove", "list", "search", "get", "count"
    );

    abstract Object call(Context handler, Class clazz, ModBoard board, Params params);

    abstract Params adaptToBoard(Context handler, Class clazz, ModBoard board, Params params);

    public static Object call(Context handler, Class clazz, String boardMethod) throws DataRiderException {
        return call(handler, clazz, boardMethod, null);
    }

    public static Object call(Context handler, Class clazz, String boardMethod, Params params) throws DataRiderException {

        ModBoard board = getBoard(clazz, boardMethod);
        if (board == null) {
            throw DataRiderException.BoardNotFound("unknown api");
        }

        return getRider(clazz).call(handler, clazz, board, params);
    }

    static Rider getRider(Class clazz) {
//        ModStructure struct = getStruct(clazz);
//        boolean isRDB = struct.getKind().equals(Constant.STRUCT_KIND_MYSQL_SYSTEM)
//                || struct.getKind().equals(Constant.STRUCT_KIND_MYSQL_USER);
        return Environment.instance().isRDB() ? new SQLRider() : new MongoRider();
    }

    public static <T extends ModCommon> Plural<T> list(Context handler, Class clazz) {
        return list(handler, clazz, null);
    }

    public static <T extends ModCommon> Plural<T> list(Context handler, Class clazz, Params params) {
        return (Plural<T>) Rider.call(handler, clazz, "list", params);
    }

    public static <T extends ModCommon> Singular<T> add(Context handler, Class clazz) {
        return add(handler, clazz, null);
    }

    public static <T extends ModCommon> Singular<T> add(Context handler, Class clazz, Params params) {
        return (Singular<T>) Rider.call(handler, clazz, "add", params);
    }

    public static <T extends ModCommon> Singular<T> get(Context handler, Class clazz) {
        return get(handler, clazz, null);
    }

    public static <T extends ModCommon> Singular<T> get(Context handler, Class clazz, Params params) {
        return (Singular<T>) Rider.call(handler, clazz, "get", params);
    }

    public static Long remove(Context handler, Class clazz) {
        return remove(handler, clazz, null);
    }

    public static Long remove(Context handler, Class clazz, Params params) {
        return (Long) Rider.call(handler, clazz, "remove", params);
    }

    public static <T extends ModCommon> Singular<T> update(Context handler, Class clazz) {
        return update(handler, clazz, null);
    }

    public static <T extends ModCommon> Singular<T> update(Context handler, Class clazz, Params params) {
        return (Singular<T>) Rider.call(handler, clazz, "update", params);
    }

    public static Long count(Context handler, Class clazz) {
        return count(handler, clazz, null);
    }

    public static Long count(Context handler, Class clazz, Params params) {
        return (Long) Rider.call(handler, clazz, "count", params);
    }

    public static <T extends ModCommon> Plural<T> search(Context handler, Class clazz) {
        return search(handler, clazz, null);
    }

    public static <T extends ModCommon> Plural<T> search(Context handler, Class clazz, Document params) {
        throw new UnsupportedOperationException("rider search");
    }

    /**
     * find board by mod class & board method (eg : get,remove etc.)
     *
     * @param clazz  Mod class
     * @param method board method
     * @return board
     */
    static ModBoard getBoard(Class clazz, String method) {

        // TODO: 支持URL参数，如 /api/user/:id

        String api = String.format("/api/%s/%s",
                WordUtils.uncapitalize(clazz.getSimpleName().replace(MODEL_PREFIX, "")),
                method);

        for (ModBoard board : CacheManager.INSTANCE.getBoards()) {
            if (board.getApi().toLowerCase().equals(api.toLowerCase())) {
                return board;
            }
        }

        throw DataRiderException.BoardNotFound(api);
    }

    static ModStructure getStruct(String schema) {
        return CacheManager.INSTANCE.getStructures()
                .stream()
                .filter(s -> s.getSchema().equals(schema))
                .findFirst()
                .get();
    }

    static ModStructure getStruct(Class clazz) {

        String name = clazz.getSimpleName();
        name = name.substring(3);
        name = name.substring(0, 1).toLowerCase() + name.substring(1);

        return getStruct(name);
    }

    static Object reserved(Context handler, String keyword) {

        if ("$uid".equals(keyword)) {
            return handler.uid();
        }

        if ("$corp".equals(keyword)) {
            return handler.code();
        }

        if ("$sysdate".equals(keyword)) {
            return new Date();
        }

        if ("$systime".equals(keyword)) {
            return new Date();
        }

        return null;
    }


    /**
     * 通过查找 structure 中的定义，来识别给定字段的类型
     *
     * @param structure ModStructure
     * @param parameter 要识别类型的字段名
     * @return 类型名称
     */
    String detectValueType(ModStructure structure, String parameter) {

        Map<String, Map> items = (Map<String, Map>) structure.getItems();

        // 多层结构的数据，可以包含.标识符，如 address.city
        if (parameter.contains(".")) {

            String[] array = parameter.split("\\.");
            Map<String, Map> subTypeInfo = (Map<String, Map>) items.get(array[0]).get("contents");

            return subTypeInfo.get(array[1]).get("type").toString().toLowerCase();
        } else {
            return items.get(parameter).get("type").toString().trim().toLowerCase();
        }
    }
}
