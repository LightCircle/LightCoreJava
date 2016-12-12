package cn.alphabets.light.model.datarider2;


import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
    public long remove(DBParams params) {
        return (long) this.call("remove", params);
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
    public long count(DBParams params) {
        return (long) this.call("count", params);
    }

    public <T extends ModCommon> Plural<T> search(DBParams params) {
        //todo
        throw new UnsupportedOperationException("rider search");
    }

    public Object call(String boardMethod, DBParams params) throws DataRiderException {
        if (board != null) {
            logger.warn("The board is exist , arg 'boardName' will be ignored");
        } else {
            board = getBoard(clazz, boardMethod);
        }

        return callCtrl(board, params);
    }

    public Object call(DBParams params) throws DataRiderException {
        if (board == null) {
            throw DataRiderException.BoardNotFound("unknown api");
        }

        return callCtrl(board, params);
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
}
