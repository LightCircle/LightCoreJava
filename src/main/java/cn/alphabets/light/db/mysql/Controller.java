package cn.alphabets.light.db.mysql;

import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

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

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Get condition can not be empty.");
        }

        Document document = this.model.get(params.getScript(), condition);

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Singular(ModCommon.fromDocument(document, type));
    }

}
