package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.Json;
import cn.alphabets.light.model.ModBase;
import cn.alphabets.light.model.Plural;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller
 */
public class Controller {

    private Model model;
    private Context.Params params;
    private String uid;

    public Controller(Context handler, Class table) {
        this(handler, table.getSimpleName().toLowerCase());
    }
    public Controller(Context handler, String table) {
        this.model = new Model(handler.getDomain(), handler.getCode(), table);
        this.params = handler.params;
        this.uid = handler.uid();
    }

    public <T extends ModBase> Plural<T> list() {

        if (!this.params.getCondition().containsKey("valid")) {
            this.params.getCondition().put("valid", Constant.VALID);
        }

        List<T> items = this.model.list(this.params.getCondition(),
                this.params.getSelect(),
                this.params.getSort(),
                this.params.getSkip(),
                this.params.getLimit());

        return new Plural<>(this.count(), items);
    }

    public String add() {

        if (this.params.getData() instanceof List) {
            List<Document> data = new ArrayList<>();
            for (Object json: (List)this.params.getData()) {
                Document document = new Document();
                document.putAll((Json)json);
                document.put("createAt", new Date());
                document.put("updateAt", new Date());
                document.put("createBy", this.uid);
                document.put("updateBy", this.uid);
                document.put("valid", Constant.VALID);
                document.remove("_id");
                data.add(document);
            }

            return this.model.add(data).get(0);
        }

        Document document = new Document();
        document.putAll((Document)this.params.getData());
        document.put("createAt", new Date());
        document.put("updateAt", new Date());
        document.put("createBy", this.uid);
        document.put("updateBy", this.uid);
        document.put("valid", Constant.VALID);
        document.remove("_id");
        return this.model.add(document);
    }

    public Long count() {
        if (!this.params.getCondition().containsKey("valid")) {
            this.params.getCondition().put("valid", Constant.VALID);
        }

        return this.model.count(this.params.getCondition());
    }

    public <T extends ModBase> T get() {

        Document condition = new Document();

        if (this.params.getId() != null) {
            Object id = this.params.getId();
            condition.put("_id", (id instanceof String) ? new ObjectId((String)id) : id);
        } else {
            condition.putAll(this.params.getCondition());
        }

        if (!this.params.getCondition().containsKey("valid")) {
            condition.put("valid", Constant.VALID);
        }
        return this.model.get(condition);
    }


    public Long delete() {

        Document condition = new Document();

        if (this.params.getId() != null) {
            Object id = this.params.getId();
            condition.put("_id", (id instanceof String) ? new ObjectId((String)id) : id);
        } else {
            condition.putAll(this.params.getCondition());
        }

        assert condition.size() > 0 : "The delete condition can not be null";
        return this.model.delete(condition);
    }

    public Long remove() {

        Document condition = new Document();

        if (this.params.getId() != null) {
            Object id = this.params.getId();
            condition.put("_id", (id instanceof String) ? new ObjectId((String)id) : id);
        } else {
            condition.putAll(this.params.getCondition());
        }

        if (!this.params.getCondition().containsKey("valid")) {
            condition.put("valid", Constant.VALID);
        }

        assert condition.size() > 0 : "The remove condition can not be null";
        return this.model.remove(condition);
    }
}
