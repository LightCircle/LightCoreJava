package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.datarider2.DBParams;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

/**
 * Controller
 */
public class Controller2 {
    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(Controller2.class);

    private DBParams params;
    private Model model;
    private String uid;


    public Controller2(DBParams params) {
        this.params = params;
        this.model = new Model(params.getDomain(), params.getCode(), params.getTable(), params.getClazz());
        this.uid = params.getUid();
    }

    public <T extends ModCommon> Plural<T> list() {
        logger.debug("DB params : " + params.toString());
        List<T> items = this.model.list(params.getCondition(),
                params.getSelect(),
                params.getSort(),
                params.getSkip(),
                params.getLimit());

        return new Plural<>(this.count(), items);
    }

    public <T extends ModCommon> T get() {
        logger.debug("DB params : " + params.toString());
        return this.model.get(params.getCondition(), params.getSelect());
    }

    /**
     * Add document
     *
     * @param <T> ModBase
     * @return document
     */
    public <T extends ModCommon> T add() {
        logger.debug("DB params : " + params.toString());
        Document document = params.getData();
        document.put("createAt", new Date());
        document.put("updateAt", new Date());
        document.put("createBy", this.uid);
        document.put("updateBy", this.uid);
        document.put("valid", Constant.VALID);
        document.put("_id", new ObjectId());
        Document confirmed = Entity.fromDocument(document, params.getClazz(), params.getHandler()).toDocument();
        return this.model.add(confirmed);
    }


    public Long update() {
        logger.debug("DB params : " + params.toString());
        Document document = params.getData();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        Document confirmed = Entity.fromDocument(document, params.getClazz(), params.getHandler()).toDocument(true);
        return this.model.update(params.getCondition(), confirmed);
    }

    public Long count() {
        return this.model.count(this.params.getCondition());
    }

}
