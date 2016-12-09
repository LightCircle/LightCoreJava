package cn.alphabets.light.model.datarider2;


import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Controller2;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static cn.alphabets.light.Constant.*;

/**
 * DataRider
 * Created by lilin on 2016/11/12.
 */
public class DataRider2 {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(DataRider2.class);
    private static final List<String> METHOD = Arrays.asList(
            "", "add", "update", "remove", "list", "search", "get", "count", "upsert"
    );
    private Class<? extends ModCommon> clazz;
    private ModBoard board;

    public static DataRider2 For(Class clazz) {
        DataRider2 dataRider2 = new DataRider2();
        dataRider2.clazz = clazz;
        return dataRider2;
    }


    public static DataRider2 For(ModBoard board) {
        DataRider2 dataRider2 = new DataRider2();
        dataRider2.board = board;

        String className = MODEL_PREFIX + WordUtils.capitalize(board.getSchema());
        String packageName = Model.reserved.contains(board.getSchema())
                ? DEFAULT_PACKAGE_NAME + ".entity"
                : Environment.instance().getPackages() + ".entity";

        try {
            dataRider2.clazz = (Class<? extends ModCommon>) Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            throw DataRiderException.EntityClassNotFound(packageName + "." + className, board);
        }

        return dataRider2;
    }


    public <T extends ModCommon> T Add(DBParams params) {
        return (T) this.call("add", params);
    }

    public <T extends ModCommon> T Get(DBParams params) {
        return (T) this.call("get", params);
    }

    public <T extends ModCommon> Plural<T> List(DBParams params) {
        return (Plural<T>) this.call("list", params);
    }


    public long Remove(DBParams params) {
        throw new UnsupportedOperationException("rider remove");
    }


    public <T extends ModCommon> T Update(DBParams params) {
        throw new UnsupportedOperationException("rider update");
    }

    public <T extends ModCommon> T Upsert(DBParams params) {
        throw new UnsupportedOperationException("rider upsert");
    }

    public long Count(DBParams params) {
        throw new UnsupportedOperationException("rider count");
    }

    public <T extends ModCommon> Plural<T> Search(DBParams params) {
        throw new UnsupportedOperationException("rider search");
    }

    public Object call(String boardName, DBParams params) throws DataRiderException {
        if (board != null) {
            logger.warn("The board is exist , arg 'boardName' will be ignored");
        } else {
            board = getBoard(clazz, boardName);
        }

        return callCtrl(board, params);
    }

    public Object call(DBParams params) throws DataRiderException {
        if (board == null) {
            throw DataRiderException.BoardNotFound("unknown api");
        }

        return callCtrl(board, params);
    }

    private Object callCtrl(ModBoard board, DBParams params) {
        params = adaptToBoard(board, params);
        Controller2 ctrl = new Controller2(params);
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


    private static ModBoard getBoard(Class<? extends ModCommon> clazz, String boardName) {

        String api = String.format("/api/%s/%s", WordUtils.uncapitalize(clazz.getSimpleName().replace(MODEL_PREFIX, "")), boardName);

        for (ModBoard board : CacheManager.INSTANCE.getBoards()) {
            if (board.getApi().toLowerCase().equals(api.toLowerCase())) {
                return board;
            }
        }

        throw DataRiderException.BoardNotFound(api);
    }


    private DBParams adaptToBoard(ModBoard board, DBParams params) {

        ModStructure structure = CacheManager.INSTANCE.getStructures().stream().filter(s -> s.getSchema().equals(board.getSchema())).findFirst().get();


        //todo 根据 board 构造 select


        //todo 根据 board 构造 condition(基本完成)

        if (params.getCondition().containsKey("free")) {
            params.setCondition((Document) params.getCondition().get("free"));
        } else if (params.getCondition().containsKey("_id")) {
            params.setCondition(new Document()
                    .append("_id", params.getCondition().get("_id"))
                    .append("valid", VALID));
        } else {

            TypeConvertor convertor = new TypeConvertor(params);
            List<Document> condList = new ArrayList<>();
            Map<String, List<ModBoard.Filters>> grouped = board.getFilters().stream().collect(Collectors.groupingBy(ModBoard.Filters::getGroup));
            grouped.forEach((s, filters) -> {
                Document section = new Document();
                filters.forEach(filter -> {

                    String parameter = filter.getKey();
                    String key = filter.getParameter();

                    Object reservedValue = reserved(params, key);
                    if (reservedValue != null) {
                        section.put(parameter, reservedValue);
                    } else if (params.getCondition().containsKey(key)) {
                        Object value = params.getCondition().get(key);
                        String valueType = ((HashMap<String, HashMap>) structure.getItems()).get(parameter).get("type").toString().trim().toLowerCase();
                        if (section.containsKey(parameter)) {
                            ((Document) section.get(parameter)).put(filter.getOperator(), convertor.Convert(valueType, value));
                        } else {
                            section.put(parameter, new Document(filter.getOperator(), convertor.Convert(valueType, value)));
                        }
                    }

                });
                if (section.size() > 0) {
                    condList.add(section);
                }
            });

            Document condition = new Document();

            if (condList.size() == 1) {
                condition = condList.get(0);
            } else if (condList.size() > 1) {
                condition.put("$or", condList);
            }

            condition.put("valid", VALID);
            params.setCondition(condition);
        }


        //todo 根据 board 构造 sort


        //todo 扩展类型的 structure 执行一次表明转换
        HashMap<Long, String> extendType = new HashMap<Long, String>() {{
            put(1L, "user");
            put(2L, "group");
            put(3L, "file");
            put(4L, "category");
        }};
        if (structure.getKind() == 1) {
            params.setTable(extendType.get(structure.getType()));
            params.setCondition(params.getCondition().append("type", structure.getSchema()));
        } else {
            params.setTable(clazz.getSimpleName().replace(MODEL_PREFIX, "").toLowerCase());
        }

        params.setClazz(clazz);

        return params;
    }


    private Object reserved(DBParams params, String keyword) {

        if ("$uid".equals(keyword)) {
            return params.getUid();
        }

        if ("$corp".equals(keyword)) {
            return params.getCode();
        }

        if ("$sysdate".equals(keyword)) {
            return new Date();
        }

        if ("$systime".equals(keyword)) {
            return new Date();
        }

        return null;
    }
}
