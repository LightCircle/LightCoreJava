package cn.alphabets.light.model.datarider;


import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mysql.Controller;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQLRider
 * <p>
 * Created by lilin on 2016/11/12.
 */
public class SQLRider extends Rider {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(SQLRider.class);

    /**
     * invoke controller method to perform db operation
     *
     * @param board  board info
     * @param params DBParams
     * @return db operation result
     */
    Object call(Context handler, Class clazz, ModBoard board, Params params) {

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

    Params adaptToBoard(Context handler, Class clazz, ModBoard board, Params params) {
        String script = buildScript(handler, board, params);
        return Params.clone(params, script, board.getSchema(), clazz);
    }

    private String buildScript(Context handler, ModBoard board, Params params) {

        if (!StringUtils.isEmpty(board.getScript())) {
            return board.getScript();
        }

        // SELECT
        List<String> selects = new ArrayList<>();
        board.getSelects().forEach(item -> {
            if (item.getSelect()) {
                selects.add(String.format("`%s`.`%s`", board.getSchema(), item.getKey()));
            }
        });

        // SORT
        List<String> sorts = new ArrayList<>();
        board.getSorts().stream()
                .sorted(Comparator.comparingInt(item -> Integer.parseInt(item.getOrder())))
                .forEach(item ->
                        sorts.add(String.format("`%s`.`%s` %s", board.getSchema(), item.getKey(), item.getOrder())));

        // WHERE
        List<List<String>> where = new ArrayList<>();

        Map<String, List<ModBoard.Filters>> group = board.getFilters()
                .stream()
                .collect(Collectors.groupingBy(ModBoard.Filters::getGroup));

        group.values().forEach(item -> {
            List<String> and = new ArrayList<>();
            item.forEach(i -> and.add(compiler(board.getSchema(), i.getKey(), i.getOperator(), i.getParameter())));
            where.add(and);
        });

        if (board.getType() == Constant.API_TYPE_LIST || board.getType() == Constant.API_TYPE_GET) {
            return selectStatement(params, handler.getDomain(), board.getSchema(), selects, where, sorts);
        }

        if (board.getType() == Constant.API_TYPE_COUNT) {
            return selectStatement(params, handler.getDomain(), board.getSchema(), null, where, null);
        }

        if (board.getType() == Constant.API_TYPE_ADD) {
            return insertStatement(params, handler.getDomain(), board.getSchema());
        }

        if (board.getType() == Constant.API_TYPE_UPDATE) {
            return updateStatement(params, handler.getDomain(), board.getSchema(), where);
        }

        if (board.getType() == Constant.API_TYPE_REMOVE) {
            return deleteStatement(params, handler.getDomain(), board.getSchema(), where);
        }

        logger.warn("Type is not recognized");
        return "";
    }

    private String selectStatement(
            Params params, String db, String schema,
            List<String> selects, List<List<String>> where, List<String> sorts) {

        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");

        // 没有指定select项目，则通过count(1)获取件数
        if (selects != null && selects.size() > 0) {
            builder.append(StringUtils.join(selects, ","));
        } else {
            builder.append(" COUNT(1) AS COUNT ");
        }

        builder.append(String.format(" FROM `%s`.`%s`", db, schema));
        builder.append(getWhere(params, schema, where));


        // 排序
        if (sorts != null && sorts.size() > 0) {
            builder.append(" ORDER BY ");
            builder.append(StringUtils.join(sorts, ","));
        }

        return builder.toString();
    }

    private String getWhere(Params params, String schema, List<List<String>> where) {

        StringBuilder builder = new StringBuilder();

        // 没有指定where，尝试使用_id检索
        if (where == null || where.size() <= 0) {
            builder.append(" WHERE ");

            // 只获取有效的项目
            List<String> list = new ArrayList<>();
            list.add(String.format("`%s`.`valid` = 1", schema));

            // 添加_id条件
            if (params.getId() != null) {
                list.add(String.format("`%s`.`_id` = <%%= condition._id %%>", schema));
            }

            builder.append(StringUtils.join(list, " AND "));
        }

        // 没有OR条件，所有项目用 AND 连接
        if (where != null && where.size() == 1) {
            builder.append(" WHERE ");

            // 只获取有效的项目
            List<String> list = where.get(0);
            list.add(String.format("`%s`.`valid` = 1", schema));

            builder.append(StringUtils.join(list, " AND "));
        }

        // 有OR条件，所有项目先用AND连接，然后再用OR连接
        if (where != null && where.size() > 1) {
            List<String> or = where.stream()
                    .map(item -> {
                        List<String> list = new ArrayList<>(item);
                        list.add(String.format("`%s`.`valid` = 1", schema));
                        return StringUtils.join(list, " AND ");
                    })
                    .collect(Collectors.toList());

            builder.append(" WHERE ");
            builder.append(StringUtils.join(or, " OR "));
        }

        return builder.toString();
    }

    private String insertStatement(Params params, String db, String schema) {

        ModStructure structure = getStruct(schema);
        Map<String, Map<String, String>> items = ((Map<String, Map<String, String>>) structure.getItems());
        Set<String> keys = items.keySet();

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("INSERT INTO `%s`.`%s` (", db, schema));

        // INSERT语句 字段定义 （只做成字段值在data里存在，并且字段不等于_id的项目）
        final List<String> column = new ArrayList<>();
        column.add("`createAt`");
        column.add("`createBy`");
        column.add("`updateAt`");
        column.add("`updateBy`");
        column.add("`valid`");

        keys.stream()
                .filter(item -> !item.equals("_id") && params.getData().containsKey(item))
                .forEach(item -> column.add(String.format("`%s`", item)));
        builder.append((StringUtils.join(column, ",")));

        builder.append(") VALUES (");

        // INSERT语句 值定义
        final List<String> value = new ArrayList<>();
        value.add("<%= data.createAt %>");
        value.add("<%= data.createBy %>");
        value.add("<%= data.updateAt %>");
        value.add("<%= data.updateBy %>");
        value.add("<%= data.valid %>");

        keys.stream()
                .filter(item -> !item.equals("_id") && params.getData().containsKey(item))
                .forEach(item -> value.add(String.format("<%%= data.%s %%>", item)));

        builder.append(StringUtils.join(value, ","));
        builder.append(")");

        return builder.toString();
    }

    private String updateStatement(Params params, String db, String schema, List<List<String>> where) {

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("UPDATE `%s`.`%s` SET ", db, schema));

        // UPDATE语句 字段定义 （只做成字段值在data里存在，并且字段不等于_id的项目）
        final List<String> column = new ArrayList<>();
        column.add("`updateAt` = <%= data.updateAt %>");
        column.add("`updateBy` = <%= data.updateBy %>");

        params.getData().keySet().forEach(item -> column.add(String.format("`%s` = <%%= data.%s %%>", item, item)));
        builder.append((StringUtils.join(column, ",")));

        builder.append(getWhere(params, schema, where));
        return builder.toString();
    }

    private String deleteStatement(Params params, String db, String schema, List<List<String>> where) {

        // return String.format("DELETE FROM `%s`.`%s` ", db, schema) + getWhere(params, schema, where);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("UPDATE `%s`.`%s` SET ", db, schema));

        // UPDATE语句 字段定义 （只做成字段值在data里存在，并且字段不等于_id的项目）
        final List<String> column = Arrays.asList(
                "`updateAt` = <%= data.updateAt %>",
                "`updateBy` = <%= data.updateBy %>",
                "`valid` = <%= data.valid %>"
        );

        builder.append((StringUtils.join(column, ",")));
        builder.append(getWhere(params, schema, where));
        return builder.toString();
    }

    private String compiler(String schema, String key, String operator, String value) {

        switch (operator) {
            case "$eq":
                return String.format("`%s`.`%s` = <%%= condition.%s %%>", schema, key, value);
            case "$ne":
                return String.format("`%s`.`%s` <> <%%= condition.%s %%>", schema, key, value);
            case "$gt":
                return String.format("`%s`.`%s`> <%%= condition.%s %%>", schema, key, value);
            case "$gte":
                return String.format("`%s`.`%s` >= <%%= condition.%s %%>", schema, key, value);
            case "$lt":
                return String.format("`%s`.`%s` < <%%= condition.%s %%>", schema, key, value);
            case "$lte":
                return String.format("`%s`.`%s` <= <%%= condition.%s %%>", schema, key, value);
        }

        throw new RuntimeException("Core has not yet supported the operator.");
    }
}
