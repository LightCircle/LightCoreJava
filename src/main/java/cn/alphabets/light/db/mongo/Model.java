package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import cn.alphabets.light.model.ModBase;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.atteo.evo.inflector.English;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Model
 */
public class Model {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);

    private MongoDatabase db;
    private MongoCollection<Document> collection;

    private Model() {
    }

    public Model(String domain, String code) {
        this.initialize(domain, code, null);
    }

    public Model(String domain, String code, String table) {
        this.initialize(domain, code, table);
    }

    private void initialize(String domain, String code, String table) {
        MongoClient client = Connection.instance(Config.instance().environment);
        this.db = client.getDatabase(domain);

        if (table != null) {
            table = English.plural(table);
            if (!Config.Constant.SYSTEM_DB.equals(domain)) {
                table = code + '.' + table;
            }
        }

        if (table != null) {
            this.collection = this.db.getCollection(table);
        }

        logger.info("table : " + table);
    }

    public <T extends ModBase> List<T> list(Class clazz) {
        List<T> result = new ArrayList<>();

        this.collection
                .find(Document.parse("{valid:1}"))
                .projection(Projections.exclude("createAt", "updateAt", "valid", "createBy", "updateBy"))
                .forEach((Block<? super Document>) document -> {
                    result.add((T) ModBase.fromDoc(document, clazz));
                });

        return result;
    }

    public <T extends ModBase> T get(Class clazz) {

        Document document = this.collection
                .find(Document.parse("{valid:1}"))
                .projection(Projections.exclude("createAt", "updateAt", "valid", "createBy", "updateBy"))
                .first();

        return (T) ModBase.fromDoc(document, clazz);
    }

    public <T extends ModBase> String add(T document) {
        return "";
    }
}
