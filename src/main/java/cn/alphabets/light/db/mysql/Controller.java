package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Constant;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller
 */
public class Controller {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(Controller.class);

    private Context handler;
    private Params params;
    private Model model;
    private String uid;

    public Controller(Context handler) {
        this(handler, null);
    }

    public Controller(Context handler, Params params) {
        this.handler = handler;
        this.uid = handler.uid();
        this.params = params;

        if (params == null) {
            this.params = handler.params;
        }
        this.model = new Model(handler.domain(), handler.getCode());
    }


    public <T extends ModCommon> Plural<T> list() {
        logger.debug("[LIST] DB params : " + params.toString());
        List<Document> items = this.model.list(params.getScript(), params.getCondition());

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Plural<>((long) items.size(), items
                .stream()
                .map(document -> (T) ModCommon.fromDocument(document, type))
                .collect(Collectors.toList()));
    }


    public <T extends ModCommon> Singular<T> get() {
        logger.debug("[GET] DB params : " + params.toString());

        Document document = this.model.get(params.getScript(), params.getCondition());

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Singular(ModCommon.fromDocument(document, type));
    }


    public <T extends ModCommon> Singular<T> add() {
        logger.debug("[ADD] DB params : " + params.toString());

        Document document = params.getData();
        document.put("createAt", new Date());
        document.put("createBy", this.uid);
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        document.put("valid", Constant.VALID);

        Document confirmed = Entity.fromDocument(
                document,
                params.getClazz(),
                this.handler.tz()).toDocument();

        // TODO: 返回插入的项目
        // SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_name = 'name' AND table_schema = 'schema'
        return new Singular(this.model.add(params.getScript(), confirmed));
    }

    public <T extends ModCommon> Singular<T> update() {
        logger.debug("[UPDATE] DB params : " + params.toString());

        Document document = params.getData();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);

        Document confirmed = Entity.fromDocument(
                document,
                params.getClazz(),
                this.handler.tz()).toDocument();

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Update condition can not be empty.");
        }

        return new Singular(this.model.update(params.getScript(), confirmed, condition));
    }

    public Long remove() {
        logger.debug("[REMOVE] DB params : " + params.toString());

        Document document = new Document();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        document.put("valid", Constant.INVALID);

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Remove condition can not be empty.");
        }

        return this.model.update(params.getScript(), document, condition);
    }

    public Long count() {
        return this.model.count(params.getScript(), params.getCondition());
    }

}
