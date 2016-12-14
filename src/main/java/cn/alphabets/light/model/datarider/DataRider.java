package cn.alphabets.light.model.datarider;


import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import com.mongodb.client.model.Filters;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static cn.alphabets.light.Constant.DEFAULT_PACKAGE_NAME;
import static cn.alphabets.light.Constant.MODEL_PREFIX;

/**
 * DataRider
 * Created by lilin on 2016/11/12.
 */
public class DataRider {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(DataRider.class);
    private static final List<String> METHOD = Arrays.asList(
            "", "add", "update", "remove", "list", "search", "get", "count"
    );
    private Class<? extends ModCommon> clazz;
    private ModBoard board;


    /**
     * New instance with Mod Class
     *
     * @param clazz mod class
     * @return instance
     */
    public static DataRider ride(Class clazz) {
        DataRider dataRider = new DataRider();
        dataRider.clazz = clazz;
        return dataRider;
    }

    /**
     * New instance with board
     *
     * @param board board info
     * @return instance
     */
    public static DataRider ride(ModBoard board) {
        DataRider dataRider = new DataRider();
        dataRider.board = board;

        String className = MODEL_PREFIX + WordUtils.capitalize(board.getSchema());
        String packageName = Model.reserved.contains(board.getSchema())
                ? DEFAULT_PACKAGE_NAME + ".entity"
                : Environment.instance().getPackages() + ".entity";

        try {
            dataRider.clazz = (Class<? extends ModCommon>) Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            throw DataRiderException.EntityClassNotFound(packageName + "." + className, board);
        }

        return dataRider;
    }

    /**
     * shortcut for board "add" method
     *
     * @param params DBParams
     * @param <T>    mod class
     * @return added mod instance
     */
    public <T extends ModCommon> T add(DBParams params) {
        return (T) this.call("add", params);
    }

    /**
     * shortcut for board "get" method
     *
     * @param params DBParams
     * @param <T>    mod class
     * @return fetched mod instance
     */
    public <T extends ModCommon> T get(DBParams params) {
        return (T) this.call("get", params);
    }

    /**
     * shortcut for board "list" method
     *
     * @param params DBParams
     * @param <T>    mod class
     * @return fetched mod instance list
     */
    public <T extends ModCommon> Plural<T> list(DBParams params) {
        return (Plural<T>) this.call("list", params);
    }

    /**
     * shortcut for board "remove" method
     *
     * @param params DBParams
     * @return removed db record count
     */
    public Long remove(DBParams params) {
        return (Long) this.call("remove", params);
    }

    /**
     * shortcut for board "update" method
     *
     * @param params DBParams
     * @param <T>    mod class
     * @return updated mod instance
     */
    public <T extends ModCommon> T update(DBParams params) {
        return (T) this.call("update", params);
    }

    /**
     * shortcut for board "count" method
     *
     * @param params DBParams
     * @return db record count
     */
    public Long count(DBParams params) {
        return (Long) this.call("count", params);
    }

    public <T extends ModCommon> Plural<T> search(DBParams params) {
        //TODO: search method
        throw new UnsupportedOperationException("rider search");
    }

    /**
     * call data rider by board method
     *
     * @param boardMethod method of board
     * @param params      DBParams
     * @return db result
     * @throws DataRiderException exception
     */
    public Object call(String boardMethod, DBParams params) throws DataRiderException {
        if (board != null) {
            logger.warn("The board is exist , arg 'boardName' will be ignored");
        } else {
            board = getBoard(clazz, boardMethod);
        }

        return call(params);
    }

    /**
     * call data rider
     *
     * @param params DBParams
     * @return db result
     * @throws DataRiderException exception
     */
    public Object call(DBParams params) throws DataRiderException {
        if (board == null) {
            throw DataRiderException.BoardNotFound("unknown api");
        }

        Object result = callCtrl(board, params);
        result = options(params.getHandler(), result, board);
        return result;
    }


    /**
     * attach options info accord board info
     *
     * @param handler Context
     * @param result  DB result will be attached
     * @param board   board info
     * @return DB result with options attached
     */
    private Object options(Context handler, Object result, ModBoard board) {

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

            HashMap<String, HashMap<String, ? extends ModCommon>> options = new HashMap<>();

            optionsBuilders.stream()
                    .collect(Collectors.groupingBy(OptionsBuilder::getStructure))
                    .forEach((s, optionsBuilders1) -> {
                        OptionsBuilderGroup optionsBuilderGroup = new OptionsBuilderGroup(optionsBuilders1, s);
                        options.put(s, optionsBuilderGroup.build(handler, result));
                    });


            if (result instanceof ModCommon) {
                ((ModCommon) result).setOptions(options.size() == 0 ? null : options);
            }

            if (result instanceof Plural) {
                ((Plural) result).setOptions(options.size() == 0 ? null : options);
            }
        }
        return result;
    }

    /**
     * invoke controller method to perform db operation
     *
     * @param board  board info
     * @param params DBParams
     * @return db operation result
     */
    private Object callCtrl(ModBoard board, DBParams params) {
        params = params.adaptToBoard(this, board);
        Controller ctrl = new Controller(params);
        String methodName = METHOD.get(board.getType().intValue());
        try {
            Method method = ctrl.getClass().getMethod(methodName);
            return method.invoke(ctrl);
        } catch (InvocationTargetException e) {
            throw DataRiderException.ControllerMethodCallFailed(methodName, e.getTargetException());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw DataRiderException.ControllerMethodCallFailed(methodName, e);
        }
    }

    /**
     * find board by mod class & board method (eg : get,remove etc.)
     *
     * @param clazz  Mod class
     * @param method board method
     * @return board
     */
    private static ModBoard getBoard(Class<? extends ModCommon> clazz, String method) {

        String api = String.format("/api/%s/%s", WordUtils.uncapitalize(clazz.getSimpleName().replace(MODEL_PREFIX, "")), method);

        for (ModBoard board : CacheManager.INSTANCE.getBoards()) {
            if (board.getApi().toLowerCase().equals(api.toLowerCase())) {
                return board;
            }
        }

        throw DataRiderException.BoardNotFound(api);
    }

    /**
     * return mod class associate with this data rider
     *
     * @return mod class
     */
    public Class<? extends ModCommon> getClazz() {
        return clazz;
    }


    static class OptionsBuilder {

        //key
        private String key;
        //fields contains by option
        private List<String> field;
        //foreign key
        private String link;
        //option structure
        private String structure;

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
            TypeConvertor convertor = new TypeConvertor(new DBParams(handler));

            //field type
            String valueType = ((HashMap<String, HashMap>) modStructure.getItems()).get(link).get("type").toString().trim().toLowerCase();

            //collect field value
            List fieldValues = new ArrayList();
            if (result instanceof ModCommon) {
                Object value = ((ModCommon) result).toDocument().get(key);
                Object converted = convertor.convert(valueType, value);
                if (converted instanceof List) {
                    fieldValues.addAll((Collection) convertor.convert(valueType, value));
                } else {
                    fieldValues.add(convertor.convert(valueType, value));
                }
            } else if (result instanceof Plural) {
                ((Plural) result).getItems().forEach(item -> {
                    Object value = ((ModCommon) item).toDocument(true).get(key);
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
            if (modStructure.getKind() == 1) {
                table = DBParams.extendType.get(modStructure.getType());
                condition = Filters.and(Filters.in(link, fieldValues), Filters.eq("type", structure));
            } else {
                table = structure;
                condition = Filters.in(link, fieldValues);
            }


            Model model = new Model(handler.getDomain(), handler.getCode(), table);

            model.list(condition, field).forEach(item -> {
                option.put(item.toDocument(true).get(link).toString(), item);
            });
            return option;

        }

        public String getStructure() {
            return structure;
        }
    }

    static class OptionsBuilderGroup {
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
