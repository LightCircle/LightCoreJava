package cn.alphabets.light.model.datarider;

import cn.alphabets.light.Constant;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.job.JobContext;
import cn.alphabets.light.model.ModCommon;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Collectors;

import static cn.alphabets.light.Constant.*;

/**
 * Created by luohao on 2016/11/30.
 */
public class DBParams {

    private static final Logger logger = LoggerFactory.getLogger(DBParams.class);

    public static HashMap<Long, String> extendType = new HashMap<Long, String>() {{
        put(1L, "user");
        put(2L, "group");
        put(3L, "file");
        put(4L, "category");
    }};

    private Document condition;
    private Document select;
    private Document sort;
    private Document data;
    private int skip;
    private int limit;
    private String table;
    private Class<? extends ModCommon> clazz;
    private Context handler;

    public DBParams(Context aHandler) {
        this(aHandler, false);
    }

    public DBParams(Context aHandler, boolean attach) {
        handler = aHandler;
        if (attach) {

            if (aHandler instanceof JobContext) {
                throw DataRiderException.ParameterUnsatisfied("[JobContext can not be attach to DBParams]");
            }

            try {
                Document json = handler.params.getJson();
                //condition from handler
                if (json.containsKey(PARAM_FREE)) {
                    this.condition = new Document(PARAM_FREE, json.get(PARAM_FREE));
                } else {

                    Document condition = new Document();
                    if (json.containsKey(PARAM_ID)) {
                        condition.append("_id", new ObjectId(json.getString(PARAM_ID)));
                    }
                    if (json.containsKey(PARAM_CONDITION)) {
                        condition.putAll((Document) json.get(PARAM_CONDITION));
                    }
                    this.condition = condition;
                }


                //select from handler
                this.select = (Document) json.get(PARAM_SELECT);
                //sort from handler
                this.sort = (Document) json.get(PARAM_SORT);
                //data from handler
                this.data = (Document) json.get(PARAM_DATA);

                //skip
                String skip = json.getString(Constant.PARAM_SKIP);
                if (StringUtils.isNotEmpty(skip)) {

                    try {
                        this.skip = Integer.parseInt(skip);
                    } catch (NumberFormatException e) {
                        logger.warn("error get [skip] ,use default 0", e);
                        this.skip = 0;
                    }
                }

                //limit
                String limit = json.getString(Constant.PARAM_LIMIT);
                if (StringUtils.isNotEmpty(limit)) {
                    try {
                        this.limit = Integer.parseInt(limit);
                    } catch (NumberFormatException e) {
                        logger.warn("error get [limit] ,use default 0", e);
                        this.limit = 0;
                    }
                }
            } catch (Exception e) {
                throw DataRiderException.ParameterUnsatisfied("[Handler]", e);
            }
        }
    }

    public DBParams condition(Document condition) {
        this.condition = condition;
        return this;
    }

    public DBParams select(Document select) {
        this.select = select;
        return this;
    }

    public DBParams sort(Document sort) {
        this.sort = sort;
        return this;
    }

    public DBParams data(Document data) {
        this.data = data;
        return this;
    }

    public DBParams data(ModCommon data) {
        this.data = data.toDocument();
        return this;
    }

    public DBParams skip(int skip) {
        this.skip = skip;
        return this;
    }

    public DBParams limit(int limit) {
        this.limit = limit;
        return this;
    }

    public Document getCondition() {
        if (condition == null) {
            condition = new Document();
            return condition;
        }
        return condition;
    }

    public Document getSelect() {
        return select;
    }

    public Document getSort() {
        return sort;
    }

    public Document getData() {
        return data;
    }

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public String getDomain() {
        return handler.getDomain();
    }

    public String getCode() {
        return handler.getCode();
    }

    public Class<? extends ModCommon> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends ModCommon> clazz) {
        this.clazz = clazz;
    }

    public String getUid() {
        return handler.uid();
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Context getHandler() {
        return handler;
    }


    public DBParams adaptToBoard(DataRider rider, ModBoard board) {

        ModStructure structure = CacheManager.INSTANCE.getStructures().stream().filter(s -> s.getSchema().equals(board.getSchema())).findFirst().get();

        //part1. build select
        buildSelect(board);

        //part2. build condition
        buildCondition(board, structure);

        //part3. build sort
        buildSort(board);

        //part4. change table name for extended structure
        if (structure.getKind() == 1) {
            table = extendType.get(structure.getType());
            condition.append("type", structure.getSchema());
        } else {
            table = rider.getClazz().getSimpleName().replace(MODEL_PREFIX, "").toLowerCase();
        }

        //part5. set clazz
        clazz = rider.getClazz();

        return this;
    }

    private void buildCondition(ModBoard board, ModStructure structure) {
        if (condition == null) {
            condition = new Document("valid", VALID);
        } else if (condition.containsKey("free")) {
            condition = (Document) condition.get("free");
        } else if (condition.containsKey("_id")) {
            condition = new Document()
                    .append("_id", condition.get("_id"))
                    .append("valid", VALID);
        } else {

            TypeConvertor convertor = new TypeConvertor(this);
            List<Document> conditionOr = new ArrayList<>();

            //group condition by filter group
            Map<String, List<ModBoard.Filters>> grouped = board.getFilters().stream().collect(Collectors.groupingBy(ModBoard.Filters::getGroup));

            //build group condition
            grouped.forEach((s, filters) -> {

                Document section = new Document();
                filters.forEach(filter -> {

                    String parameter = filter.getKey();
                    String key = filter.getParameter();

                    //build reserved
                    Object reservedValue = reserved(key);
                    if (reservedValue != null) {
                        section.put(parameter, reservedValue);
                    } else if (condition.containsKey(key)) {
                        Object value = condition.get(key);
                        String valueType = ((HashMap<String, HashMap>) structure.getItems()).get(parameter).get("type").toString().trim().toLowerCase();
                        if (section.containsKey(parameter)) {
                            ((Document) section.get(parameter)).put(filter.getOperator(), convertor.convert(valueType, value));
                        } else {
                            section.put(parameter, new Document(filter.getOperator(), convertor.convert(valueType, value)));
                        }
                    }

                });

                //is section is not empty,add to conditionOr
                if (section.size() > 0) {
                    conditionOr.add(section);
                }
            });

            condition = new Document();

            if (conditionOr.size() == 1) {
                condition = conditionOr.get(0);
            } else if (conditionOr.size() > 1) {
                condition.put("$or", conditionOr);
            }

            condition.put("valid", VALID);
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
     * @param board board info
     */
    private void buildSelect(ModBoard board) {

        //passed from client
        if (select != null) {
            // {field1:'1',field2:'1'}  ->  {field1:1,field2:1}
            Document confirmed = new Document();
            select.forEach((s, o) -> confirmed.put(s, o instanceof String ? Integer.parseInt((String) o) : o));
            select = confirmed;
        } else {
            //get from board
            select = new Document();
            board.getSelects().forEach(s -> {
                if (s.getSelect()) {
                    select.put(s.getKey(), 1);
                }
            });
        }


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
     * @param board board info
     */
    private void buildSort(ModBoard board) {
        //passed from client
        if (sort != null) {
            // {field1:'1',field2:'-1'}  ->  {field1:1,field2:-1}
            Document confirmed = new Document();
            sort.forEach((s, o) -> confirmed.put(s, o instanceof String ? Integer.parseInt((String) o) : o));
            sort = confirmed;
        } else {
            //get from board
            sort = new Document();
            board.getSorts().forEach(s -> {
                sort.put(s.getKey(), "desc".equals(s.getOrder()) ? -1 : 1);
            });
        }
    }

    private Object reserved(String keyword) {

        if ("$uid".equals(keyword)) {
            return this.getUid();
        }

        if ("$corp".equals(keyword)) {
            return this.getCode();
        }

        if ("$sysdate".equals(keyword)) {
            return new Date();
        }

        if ("$systime".equals(keyword)) {
            return new Date();
        }

        return null;
    }

    @Override
    public String toString() {
        return "\n{" +
                "\n\ttable = " + getTable() +
                "\n\tclass = " + (clazz == null ? "null" : clazz.getName()) +
                "\n\tcondition = " + (condition == null ? "null" : condition.toJson()) +
                "\n\tselect = " + (select == null ? "null" : select.toJson()) +
                "\n\tsort = " + (sort == null ? "null" : sort.toJson()) +
                "\n\tdata = " + (data == null ? "null" : data.toJson()) +
                "\n\tskip = " + skip +
                "\n\tlimit = " + limit +
                "\n\tuid = " + getUid() +
                "\n\tdoamin = " + getDomain() +
                "\n\tcode = " + getCode() +
                "\n}";
    }
}
