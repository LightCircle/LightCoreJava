package cn.alphabets.light.model;


import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.entity.Board;
import cn.alphabets.light.entity.Structure;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.exception.MethodNotFoundException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DataRider
 * Created by lilin on 2016/11/12.
 */
public class DataRider {


    private String clazz;

    private List<Structure> structures;
    private List<Board> boards;
    private static final List<String> METHOD = Arrays.asList(
            "", "add", "update", "remove", "list", "search", "get", "count", "upsert"
    );

    public DataRider(Class clazz) {
        this(clazz.getSimpleName().toLowerCase());
    }

    public DataRider(String clazz) {
        this.clazz = clazz.toLowerCase();

        this.structures = CacheManager.INSTANCE.getStructures();
        this.boards = CacheManager.INSTANCE.getBoards();
    }

    public String add(Context handler) {
        return (String) this.call(handler);
    }

    public String update() {
        return null;
    }

    public String remove() {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends ModBase> Plural<T> list(Context handler) {
        return (Plural<T>) this.call(handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends ModBase> T get(Context handler) {
        return (T) this.call(handler);
    }

    public String search() {
        return null;
    }

    public String count() {
        return null;
    }

    public String upsert() {
        return null;
    }

    public Object call(Context handler) {
        return this.call(handler, null);
    }

    public Object call(Context handler, String action) {

        if (action == null) {
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
            action = stacktrace[3].getMethodName();
        }

        Board board = getBoard(action);
        if (board == null) {
            throw new MethodNotFoundException("Board method not found.");
        }

        handler.params.setCondition(this.getFilter(handler, board));

        // TODO: set sort

        Controller ctrl = new Controller(handler, this.clazz);
        try {
            Method method = ctrl.getClass().getMethod(METHOD.get(board.getType().intValue()));
            return method.invoke(ctrl);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new MethodNotFoundException("Board method not found.");
        }
    }

    private Json compare(String operator, String field, Object value) {
        return new Json(field, new Json(operator, value));
    }

    private Object reserved(Context handler, String keyword) {

        if ("$uid".equals(keyword)) {
            return handler.uid();
        }

        if ("$corp".equals(keyword)) {
            return handler.getCode();
        }

        if ("$sysdate".equals(keyword)) {
            return LocalDate.now();
        }

        if ("$systime".equals(keyword)) {
            return LocalTime.now();
        }

        return keyword;
    }

    private Board getBoard(String action) {

        for (Board board : this.boards) {
            if (board.getClass_().equals(this.clazz) && board.getAction().equals(action)) {
                return board;
            }
        }

        // TODO: default board
        return new Board();
    }

    private void getSort() {

    }

    private Json getFilter(Context handler, Board board) {
        final Json data = handler.params.getCondition();
        final Json or = new Json();

        board.getFilters().forEach((filter) -> {

            String parameter = filter.getParameter();
            String defaults = filter.getDefault_();
            String operator = filter.getOperator();
            String key = filter.getKey();
            String group = filter.getGroup();

            Object value = null;

            // If the parameter is not specified, the default value
            if (StringUtils.isEmpty(parameter) && StringUtils.isNotEmpty(defaults)) {
                value = this.reserved(handler, defaults);
            } else {

                if (data.containsKey(parameter)) {
                    value = data.get(parameter);
                }

                // Can not get to the parameter value, the default value
                if (value == null && StringUtils.isNotEmpty(defaults)) {
                    value = this.reserved(handler, defaults);
                }
            }

            // If there is no value, the condition is ignored
            if (value == null) {
                return;
            }

            if (!or.containsKey(group)) {
                or.put(group, new Json());
            }

            Json and = (Json) or.get(group);
            and.putAll(compare(operator, key, value));
        });

        // No condition to return empty
        if (or.size() < 1) {
            return or;
        }

        // If only one condition or group, are removed or comparison operators
        if (or.size() == 1) {
            return (Json) or.values().toArray()[0];
        }

        return new Json("$or", or.values().stream().map((val) -> val).collect(Collectors.toList()));
    }
}
