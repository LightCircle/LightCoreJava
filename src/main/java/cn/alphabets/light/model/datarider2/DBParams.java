package cn.alphabets.light.model.datarider2;

import cn.alphabets.light.Constant;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.ModCommon;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.TimeZone;

import static cn.alphabets.light.Constant.*;

/**
 * Created by luohao on 2016/11/30.
 */
public class DBParams {
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
                this.skip = Integer.parseInt(skip);
            }

            //limit
            String limit = json.getString(Constant.PARAM_LIMIT);
            if (StringUtils.isNotEmpty(limit)) {
                this.limit = Integer.parseInt(limit);
            }
        }
    }

    public DBParams Condition(Document condition) {
        this.condition = condition;
        return this;
    }

    public DBParams Select(Document select) {
        this.condition = condition;
        return this;
    }

    public DBParams Sort(Document sort) {
        this.condition = condition;
        return this;
    }

    public DBParams Data(Document data) {
        this.condition = condition;
        return this;
    }

    public Document getCondition() {
        if (condition == null) {
            return new Document();
        }
        return condition;
    }

    public void setCondition(Document condition) {
        this.condition = condition;
    }

    public Document getSelect() {
        return select;
    }

    public void setSelect(Document select) {
        this.select = select;
    }

    public Document getSort() {
        return sort;
    }

    public void setSort(Document sort) {
        this.sort = sort;
    }

    public Document getData() {
        return data;
    }

    public void setData(Document data) {
        this.data = data;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
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
