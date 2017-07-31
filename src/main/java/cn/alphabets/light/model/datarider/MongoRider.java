package cn.alphabets.light.model.datarider;


import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.alphabets.light.Constant.VALID;

/**
 * DataRider
 * <p>
 * Created by lilin on 2016/11/12.
 */
public class MongoRider extends Rider {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(MongoRider.class);

    /**
     * invoke controller method to perform db operation
     *
     * @param board  board info
     * @param params DBParams
     * @return db operation result
     */
    public Object call(Context handler, Class clazz, ModBoard board, Params params) {

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

        ModStructure structure = getStruct(board.getSchema());

        // part1. build select
        Document select = buildSelect(board, params);

        // part2. build condition
        Document condition = buildCondition(handler, board, structure, params);

        // part3. build sort
        Document sort = buildSort(board, params);

        // part4. change condition for extended structure
        Document data = params.getData();
        if (structure.getParent() != null && structure.getParent().length() > 0) {

            // 检索条件添加type
            condition.append("type", structure.getSchema());

            if (data != null && !data.containsKey("type")) {
                data.append("type", structure.getSchema());
            }
        }

        return Params.clone(params, condition, data, select, sort, board.getSchema(), clazz);
    }

    private Document buildCondition(Context handler, ModBoard board, ModStructure structure, Params params) {

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
    private Document buildSelect(ModBoard board, Params params) {

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
    private Document buildSort(ModBoard board, Params params) {

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
}
