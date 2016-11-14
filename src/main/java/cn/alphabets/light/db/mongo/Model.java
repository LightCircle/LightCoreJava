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

import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Indexes.descending;

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
        this(domain, code, null);
    }

    public Model(String domain, String code, String table) {

        MongoClient client = Connection.instance(Environment.instance());

        this.db = client.getDatabase(domain);
        this.name = table;

        if (table != null) {
            table = English.plural(table);
            if (!Constant.SYSTEM_DB.equals(domain)) {
                table = code + '.' + table;
            }

            this.collection = this.db.getCollection(table);
        }

        logger.info("table : " + table);
    }

    public <T extends ModBase> List<T> list() {
        return this.list(null);
    }

    public <T extends ModBase> List<T> list(Document condition) {
        return this.list(condition, null);
    }

    public <T extends ModBase> List<T> list(Document condition, List<String> fieldNames) {
        return this.list(condition, fieldNames, null, 0, Constant.DEFAULT_LIMIT);
    }

    public <T extends ModBase> List<T> list(
            Document condition,
            List<String> fieldNames,
            List<String> sortField,
            int skipCount,
            int limitCount) {

        // default value
        condition = condition == null ? new Document() : condition;
        fieldNames = fieldNames == null ? Collections.emptyList() : fieldNames;
        sortField = sortField == null ? Collections.emptyList() : sortField;

        // set fetch condition
        FindIterable<Document> find = this.collection.find(condition);
        FindIterable<Document> skip = find.skip(skipCount);
        FindIterable<Document> limit = skip.limit(limitCount);
        FindIterable<Document> sort = limit.sort(descending(sortField));
        FindIterable<Document> projection = sort.projection(Projections.include(fieldNames));

        // fetch and convert
        List<T> result = new ArrayList<>();
        projection.forEach((Block<? super Document>) document -> {
            result.add((T) ModBase.fromDoc(document, this.getModelType()));
        });
        return result;
    }

    public List<Document> document(Document condition, List<String> fieldNames) {

        FindIterable<Document> find = this.collection.find(condition);
        FindIterable<Document> projection = find.projection(Projections.include(fieldNames));

        List<Document> result = new ArrayList<>();
        projection.forEach((Block<? super Document>) result::add);
        return result;
    }

    public <T extends ModBase> T get() {
        return this.get(null, null);
    }

    public <T extends ModBase> T get(Document condition) {
        return this.get(condition, null);
    }

    public <T extends ModBase> T get(Document condition, List<String> fieldNames) {

        // default value
        condition = condition == null ? new Document() : condition;
        fieldNames = fieldNames == null ? Collections.emptyList() : fieldNames;

        // set fetch condition
        FindIterable<Document> find = this.collection.find(condition);
        FindIterable<Document> projection = find.projection(Projections.include(fieldNames));

        Document document = find.first();
        return (T) ModBase.fromDoc(document, this.getModelType());
    }

    public Long count(Document condition) {
        return this.collection.count(condition);
    }

    public String add(Document document) {
        this.collection.insertOne(document);
        return document.get("_id").toString();
    }

    public List<String> add(List<Document> document) {
        this.collection.insertMany(document);
        return document.stream().map(item -> item.get("_id").toString()).collect(Collectors.toList());
    }

    private Class getModelType() {

        String className = WordUtils.capitalize(this.name);
        String packageName = reserved.contains(this.name)
                ? Constant.DEFAULT_PACKAGE_NAME + ".entity"
                : Environment.instance().getPackages() + ".entity";

        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
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
