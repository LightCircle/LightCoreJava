package cn.alphabets.light.db.mongo;

import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.Json;
import cn.alphabets.light.model.ModBase;
import cn.alphabets.light.model.Result;

import java.util.List;

/**
 * Controller
 */
public class Controller {

    private String table;
    private Model model;
    private Context.Params params;


    public Controller(Context handler) {
        this(handler, null);
    }

    public Controller(Context handler, String table) {
        this.model = new Model(handler.getDomain(), handler.getCode(), table);
        this.params = handler.params;
    }

    public <T extends ModBase> Result<T> list() {

        Json valid = new Json("valid", 1);
        this.params.getCondition().putAll(valid);

        Long total = this.model.count(this.params.getCondition());

        List<T> items = this.model.list(this.params.getCondition(),
                this.params.getSelect(),
                this.params.getSort(),
                this.params.getSkip(),
                this.params.getLimit());

        return new Result<>(total, items);
    }
}
