package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.Json;
import cn.alphabets.light.model.ModBase;
import cn.alphabets.light.model.Result;
import org.bson.Document;

import java.util.Date;
import java.util.List;

/**
 * Controller
 */
public class Controller {

    private Model model;
    private Context.Params params;
    private String uid;

    public Controller(Context handler) {
        this(handler, null);
    }

    public Controller(Context handler, String table) {
        this.model = new Model(handler.getDomain(), handler.getCode(), table);
        this.params = handler.params;
        this.uid = handler.uid();
    }

    public <T extends ModBase> Result<T> list() {

        if (!this.params.getCondition().containsKey("valid")) {
            this.params.getCondition().put("valid", Constant.VALID);
        }

        List<T> items = this.model.list(this.params.getCondition(),
                this.params.getSelect(),
                this.params.getSort(),
                this.params.getSkip(),
                this.params.getLimit());

        return new Result<>(this.count(), items);
    }

    public String add() {

        Document data = new Document();
        data.putAll(this.params.getData());
        data.put("createAt", new Date());
        data.put("updateAt", new Date());
        data.put("createBy", this.uid);
        data.put("updateBy", this.uid);
        data.put("valid", Constant.VALID);
        data.remove("_id");

        return this.model.add(data);
    }

    public Long count() {
        if (!this.params.getCondition().containsKey("valid")) {
            this.params.getCondition().put("valid", Constant.VALID);
        }

        return this.model.count(this.params.getCondition());
    }
}
