package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.model.ModBase;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.apache.commons.lang3.text.WordUtils;
import org.atteo.evo.inflector.English;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
        MongoClient client = Connection.instance(Environment.instance());
        this.db = client.getDatabase(domain);
        this.name = table;

        if (table != null) {
            table = English.plural(table);
            if (!Constant.SYSTEM_DB.equals(domain)) {
                table = code + '.' + table;
            }
        }

        if (table != null) {
            this.collection = this.db.getCollection(table);
        }

        logger.info("table : " + table);
    }

    private Class getModType() {

        String className = WordUtils.capitalize(this.name);
        String packageName = reserved.contains(this.name)
                ? Constant.DEFAULT_PACKAGE_NAME + ".model"
                : Environment.instance().getPackages() + ".model";

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

    public List<Document> list_(Document condition, List<String> fieldNames) {
        List<Document> result = new ArrayList<>();

        FindIterable<Document> find = this.collection.find(condition);
        FindIterable<Document> projection = find.projection(Projections.include(fieldNames));
        projection.forEach((Block<? super Document>) document -> {
            result.add(document);
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

    private List<String> reserved = Arrays.asList(
            Constant.SYSTEM_DB_BOARD,
            Constant.SYSTEM_DB_CONFIG,
            Constant.SYSTEM_DB_VALIDATOR,
            Constant.SYSTEM_DB_I18N,
            Constant.SYSTEM_DB_STRUCTURE,
            Constant.SYSTEM_DB_BOARD,
            Constant.SYSTEM_DB_ROUTE,
            Constant.SYSTEM_DB_TENANT
    );
}
