package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.datarider.DBParams;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

/**
 * Controller
 */
public class Controller {
    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(Controller.class);

    private DBParams params;
    private Model model;
    private String uid;


    public Controller(DBParams params) {
        this.params = params;
        this.model = new Model(params.getDomain(), params.getCode(), params.getTable(), params.getClazz());
        this.uid = params.getUid();
    }

    public <T extends ModCommon> Plural<T> list() {
        logger.debug("[LIST] DB params : " + params.toString());
        List<T> items = this.model.list(params.getCondition(),
                params.getSelect(),
                params.getSort(),
                params.getSkip(),
                params.getLimit());

        return new Plural<>(this.count(), items);
    }

    public <T extends ModCommon> T get() {
        logger.debug("[GET] DB params : " + params.toString());

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Get condition can not be empty.");
        }
        return this.model.get(condition, params.getSelect());
    }


    public <T extends ModCommon> T add() {
        logger.debug("[ADD] DB params : " + params.toString());
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


    public <T extends ModCommon> T update() {
        logger.debug("[UPDATE] DB params : " + params.toString());
        Document document = params.getData();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        Document confirmed = Entity.fromDocument(document, params.getClazz(), params.getHandler()).toDocument(true);

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Update condition can not be empty.");
        }
        this.model.update(condition, confirmed);
        //TODO: multi document updated ,return what?
        return this.model.get(condition, params.getSelect());
    }


    public Long remove() {
        logger.debug("[REMOVE] DB params : " + params.toString());
        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Remove condition can not be empty.");
        }
        return this.model.remove(params.getCondition());
    }

    public Long count() {
        return this.model.count(this.params.getCondition());
    }


    public ByteArrayOutputStream readStreamFromGrid() {
        return this.model.readStreamFromGrid(params.getCondition().getObjectId("_id"));
    }

    public ModFile readStreamFromGrid(OutputStream outputStream) {
        return this.model.readStreamFromGrid(params.getCondition().getObjectId("_id"), outputStream);
    }

    public GridFSFile writeFileToGrid(RequestFile file) {
        return this.model.writeFileToGrid(file);
    }

    public void deleteFromGrid() {
        this.model.deleteFromGrid(params.getCondition().getObjectId("_id"));
    }


}
