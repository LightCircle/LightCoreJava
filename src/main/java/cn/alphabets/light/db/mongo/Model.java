package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import cn.alphabets.light.model.ModBase;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
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
    private String name;

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
        this.name = table;

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

    private Class getModType() {

        // TODO: 临时写死package, 需要泛用化。可否去掉Mod前缀?
        String packageName = "cn.alphabets.light.model";
        String className = "Mod" + WordUtils.capitalize(this.name);

        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public <T extends ModBase> List<T> list() {
        return list(Document.parse("{valid:1}"), new ArrayList<>());
    }

    public <T extends ModBase> List<T> list(Document condition, List<String> fieldNames) {
        List<T> result = new ArrayList<>();

        FindIterable<Document> find = this.collection.find(condition);
        FindIterable<Document> projection = find.projection(Projections.include(fieldNames));
        projection.forEach((Block<? super Document>) document -> {
            result.add((T) ModBase.fromDoc(document, this.getModType()));
        });

        return result;
    }

    public <T extends ModBase> T get() {

        Document document = this.collection
                .find(Document.parse("{valid:1}"))
                .projection(Projections.exclude("createAt", "updateAt", "valid", "createBy", "updateBy"))
                .first();

        return (T) ModBase.fromDoc(document, this.getModType());
    }

    public <T extends ModBase> String add(T document) {
        return "";
    }
}