package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.model.ModCommon;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;

import static cn.alphabets.light.Constant.*;

/**
 * 请求参数
 * <p>
 * Created by luohao on 2016/12/26.
 */
public class Params {

    private static final Logger logger = LoggerFactory.getLogger(Params.class);

    private Document json;
    private List<RequestFile> files;
    private Document condition;
    private Document select;
    private Document sort;
    private Object data;
    private int skip;
    private int limit;
    private String table;
    private String script;
    private Class<? extends ModCommon> clazz;

    public Params() {
    }

    public Params(Document json) {
        this(json, null);
    }

    public Params(Document json, List<RequestFile> files) {
        this.json = json;
        this.files = files;

        try {
            // condition from handler
            if (json.containsKey(PARAM_FREE)) {
                this.condition = new Document(PARAM_FREE, json.get(PARAM_FREE));
            } else {

                this.condition = new Document();

                // 如果包含id字段，尝试转换成ObjectID类
                if (json.containsKey(PARAM_ID)) {
                    if (ObjectId.isValid(json.getString(PARAM_ID))) {
                        this.id(json.getString(PARAM_ID));
                    } else {
                        this.condition.append("id", json.getString(PARAM_ID));
                    }
                }

                if (json.containsKey(PARAM_CONDITION)) {
                    this.condition.putAll((Document) json.get(PARAM_CONDITION));
                }
            }

            // select from handler
            this.select = (Document) json.get(PARAM_SELECT);

            // sort from handler
            this.sort = (Document) json.get(PARAM_SORT);

            // data from handler
            this.data = json.get(PARAM_DATA);

            // skip
            String skip = json.getString(Constant.PARAM_SKIP);
            if (StringUtils.isNotEmpty(skip)) {
                try {
                    this.skip = Integer.parseInt(skip);
                } catch (NumberFormatException e) {
                    logger.warn("error get [skip] ,use default 0", e);
                    this.skip = 0;
                }
            }

            // limit
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

    /**
     * 这个构造函数主要用于DataRider中，通过Board定义正规化参数值时使用
     * 目的主要是，正规化过程中不修改用于指定的handler.params内的参数
     *
     * @param params    params
     * @param condition condition
     * @param data      data
     * @param select    select
     * @param sort      sort
     * @param table     table
     * @param clazz     clazz
     * @return new params
     */
    public static Params clone(
            Params params, Document condition, Object data, Document select, Document sort, String table, Class clazz){

        Params newParams = new Params();
        newParams.condition = condition;
        newParams.data = data;
        newParams.select = select;
        newParams.sort = sort;
        newParams.skip = params.getSkip();
        newParams.limit = params.getLimit();
        newParams.files = params.getFiles();
        newParams.table = table;
        newParams.clazz = clazz;
        return newParams;
    }

    public static Params clone(Params params, String script, String table, Class clazz){

        Params newParams = new Params();
        newParams.script = script;
        newParams.condition = params.getCondition();
        newParams.data = params.getData();
        newParams.skip = params.getSkip();
        newParams.limit = params.getLimit();
        newParams.files = params.getFiles();
        newParams.table = table;
        newParams.clazz = clazz;
        return newParams;
    }

    public Params condition(Document condition) {
        this.condition = condition;
        return this;
    }

    public Params data(Document data) {
        this.data = data;
        return this;
    }

    public Params data(ModCommon data) {
        this.data = data.toDocument();
        return this;
    }

    public Params select(Document select) {
        this.select = select;
        return this;
    }

    public Params sort(Document sort) {
        this.sort = sort;
        return this;
    }

    public Params skip(int skip) {
        this.skip = skip;
        return this;
    }

    public Params limit(int limit) {
        this.limit = limit;
        return this;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getScript() {
        return script;
    }

    public Params script(String script) {
        this.script = script;
        return this;
    }

    public Class<? extends ModCommon> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends ModCommon> clazz) {
        this.clazz = clazz;
    }

    public String get(String key) {
        return this.json.getString(key);
    }

    public void set(String key, Object val) {
        this.json.put(key, val);
    }

    // 允许自定义的字符串类型的_id
    public Params id(String id) {
        if (ObjectId.isValid(id)) {
            return this.id(new ObjectId(id));
        }

        if (this.condition == null) {
            this.condition = new Document();
        }
        this.condition.put("_id", id);
        return this;
    }

    public Params id(ObjectId id) {
        if (this.condition == null) {
            this.condition = new Document();
        }
        this.condition.put("_id", id);

        return this;
    }

    public Document getId() {
        if (this.condition == null) {
            this.condition = new Document();
        }

        if (this.condition.containsKey("_id")) {
            return new Document().append("_id", this.condition.get("_id"));
        }

        return null;
    }

    public Document getCondition() {
        if (condition == null){
            condition = new Document();
        }
        return condition;
    }

    public Document getSelect() {
        if (select == null){
            select = new Document();
        }
        return select;
    }

    public Document getSort() {
        if (sort == null){
            sort = new Document();
        }
        return sort;
    }

    public Document getData() {
        if (data == null){
            data = new Document();
        }
        return (Document) data;
    }

    public List<Document> getDatas() {
        if (data == null || !(data instanceof List)) {
            return null;
        }
        return (List<Document>) data;
    }

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }

    public List<RequestFile> getFiles() {
        return files;
    }

    public void files(List<RequestFile> files) {
        this.files = files;
    }

    public void file(RequestFile file) {
        this.files(Arrays.asList(file));
    }

    public Document getJson() {
        return json;
    }

    @Override
    public String toString() {
        return "\n{" +
                "\n\tcondition = " + (condition == null ? "null" : condition.toJson()) +
                "\n\tselect = " + (select == null ? "null" : select.toJson()) +
                "\n\tsort = " + (sort == null ? "null" : sort.toJson()) +
                "\n\tdata = " + (data == null ? "null" : ((Document) data).toJson()) +
                "\n\tskip = " + skip +
                "\n\tlimit = " + limit +
                "\n\tscript = " + script +
                "\n}";
    }

}
