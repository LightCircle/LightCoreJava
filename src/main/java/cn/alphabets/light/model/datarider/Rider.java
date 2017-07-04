package cn.alphabets.light.model.datarider;


import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static cn.alphabets.light.Constant.MODEL_PREFIX;
import static cn.alphabets.light.Constant.VALID;

/**
 * DataRider
 * <p>
 * Created by lilin on 2016/11/12.
 */
public class Rider {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(Rider.class);
    private static final List<String> METHOD = Arrays.asList(
            "", "add", "update", "remove", "list", "search", "get", "count"
    );

    public static Object call(Context handler, Class clazz, String boardMethod) throws DataRiderException {
        return call(handler, clazz, boardMethod);
    }

    public static Object call(Context handler, Class clazz, String boardMethod, Params params) throws DataRiderException {

        ModBoard board = getBoard(clazz, boardMethod);
        if (board == null) {
            throw DataRiderException.BoardNotFound("unknown api");
        }

        Object result = call(handler, clazz, board, params);
        return OptionsBuilder.fetchOptions(handler, result, board);
    }


    /**
     * invoke controller method to perform db operation
     *
     * @param board  board info
     * @param params DBParams
     * @return db operation result
     */
    private static Object call(Context handler, Class clazz, ModBoard board, Params params) {

        Params newParams = adaptToBoard(handler, clazz, board, params == null ? handler.params : params);
        Controller controller = new Controller(handler, newParams);
        String methodName = METHOD.get(board.getType().intValue());

        try {
            return controller.getClass().getMethod(methodName).invoke(controller);
        } catch (InvocationTargetException e) {
            throw DataRiderException.ControllerMethodCallFailed(methodName, e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw DataRiderException.ControllerMethodCallFailed(methodName, e);
        }
    }


    public static Params adaptToBoard(Context handler, Class clazz, ModBoard board, Params params) {

        ModStructure structure = getStruct(board.getSchema());

        // part1. build select
        Document select = buildSelect(board, params);

        // part2. build condition
        Document condition = buildCondition(handler, board, structure, params);

        // part3. build sort
        Document sort = buildSort(board, params);

        // part4. change condition for extended structure
        Document data = params.getData();
        if (structure.getParent().length() > 0) {

            // 检索条件添加type
            condition.append("type", structure.getSchema());

            if (data != null && !data.containsKey("type")) {
                data.append("type", structure.getSchema());
            }
        }

        return new Params(condition, data, select, sort,
                params.get_id(), params.getSkip(), params.getLimit(), params.getFiles(), board.getSchema(), clazz);
    }

    private static Document buildCondition(Context handler, ModBoard board, ModStructure structure, Params params) {

        final Document condition = params.getCondition();

        if (condition == null) {
            return new Document("valid", VALID);
        }

        if (condition.containsKey("free")) {
            return (Document) condition.get("free");
        }

        if (condition.containsKey("_id")) {
            return new Document().append("_id", condition.get("_id")).append("valid", VALID);
        }

        TypeConverter converter = new TypeConverter(handler);
        List<Document> or = new ArrayList<>();

        // group condition by filter group
        Map<String, List<ModBoard.Filters>> grouped = board.getFilters()
                .stream()
                .collect(Collectors.groupingBy(ModBoard.Filters::getGroup));

        // build group condition
        grouped.forEach((s, filters) -> {

            Document section = new Document();
            filters.forEach(filter -> {

                String parameter = filter.getKey();
                String key = filter.getParameter();
                String perator = filter.getOperator();

                // build reserved
                Object value = reserved(handler, key);
                if (value != null) {
                    section.put(parameter, value);

                } else if (condition.containsKey(key)) {

                    value = condition.get(key);
                    String valueType = detectValueType(structure, parameter);
                    if (section.containsKey(parameter)) {
                        ((Document) section.get(parameter)).put(perator, converter.convert(valueType, value));
                    } else {
                        section.put(parameter, new Document(perator, converter.convert(valueType, value)));
                    }
                }

            });

            // skip empty section
            if (section.size() > 0) {
                or.add(section);
            }
        });

        Document result = new Document();
        if (or.size() == 1) {
            result = or.get(0);
        }
        if (or.size() > 1) {
            result.put("$or", or);
        }

        result.put("valid", VALID);
        return result;
    }

    /**
     * 通过查找 structure 中的定义，来识别给定字段的类型
     *
     * @param structure ModStructure
     * @param parameter 要识别类型的字段名
     * @return 类型名称
     */
    private static String detectValueType(ModStructure structure, String parameter) {

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

    /**
     * build select
     * <p>
     * if select is passed from client, use the passed select
     * or use the select get from board
     * <p>
     * select passed from client should be below
     * {"field1":1,"field2":1}
     *
     * @param board  board info
     * @param params request params
     */
    private static Document buildSelect(ModBoard board, Params params) {

        final Document select = params.getSelect();

        // get from board
        if (select == null) {
            Document confirmed = new Document();
            board.getSelects().forEach(s -> {
                if (s.getSelect()) {
                    confirmed.put(s.getKey(), 1);
                }
            });
            return confirmed;
        }

        // passed from client.  {field1:'1',field2:'1'}  ->  {field1:1,field2:1}
        Document confirmed = new Document();
        select.forEach((s, o) -> confirmed.put(s, o instanceof String ? Integer.parseInt((String) o) : o));
        return confirmed;
    }

    /**
     * build sort
     * <p>
     * if sort is passed from client, use the passed sort
     * or use the sort get from board
     * <p>
     * sort passed from client should be below
     * {"field1":-1,"field2":1}
     *
     * @param board  board info
     * @param params request params
     */
    private static Document buildSort(ModBoard board, Params params) {

        final Document sort = params.getSort();

        // get from board
        if (sort == null) {
            Document confirmed = new Document();
            board.getSorts().forEach(s -> {
                confirmed.put(s.getKey(), "desc".equals(s.getOrder()) ? -1 : 1);
            });
            return confirmed;
        }

        // passed from client. {field1:'1',field2:'-1'}  ->  {field1:1,field2:-1}
        Document confirmed = new Document();
        sort.forEach((s, o) -> confirmed.put(s, o instanceof String ? Integer.parseInt((String) o) : o));
        return confirmed;
    }

    private static Object reserved(Context handler, String keyword) {

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
        return update(handler, clazz);
    }

    public static <T extends ModCommon> Singular<T> update(Context handler, Class clazz, Params params) {
        return (Singular<T>) Rider.call(handler, clazz, "update", params);
    }

    public static Long count(Context handler, Class clazz) {
        return count(handler, clazz);
    }

    public static Long count(Context handler, Class clazz, Params params) {
        return (Long) Rider.call(handler, clazz, "count", params);
    }

    public static <T extends ModCommon> Plural<T> search(Context handler, Class clazz) {
        return search(handler, clazz);
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
    private static ModBoard getBoard(Class clazz, String method) {

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

    private static ModStructure getStruct(String schema) {
        return CacheManager.INSTANCE.getStructures()
                .stream()
                .filter(s -> s.getSchema().equals(schema))
                .findFirst()
                .get();
    }

    /**
     * 获取Entity的类型
     * - name为系统表，或kind为系统api时，返回cn.alphabets.light.entity包下的类型
     * - 否则返回用户包下的类型
     *
     * @param name 表名称
     * @param kind api类型
     * @return Entity类型
     */
    public static Class getEntityType(String name, Long kind) {

        boolean usingLightEntity = Model.system.contains(name) || Constant.KIND_BOARD_SYSTEM_DATA.equals(kind);

        String packageName = usingLightEntity
                ? Constant.DEFAULT_PACKAGE_NAME + ".entity"
                : Environment.instance().getPackages() + ".entity";

        String className = Constant.MODEL_PREFIX + WordUtils.capitalize(name);

        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(Constant.DEFAULT_PACKAGE_NAME + ".entity." + className);
            } catch (ClassNotFoundException e1) {
                throw DataRiderException.EntityClassNotFound(packageName + "." + className);
            }
        }
    }

}
