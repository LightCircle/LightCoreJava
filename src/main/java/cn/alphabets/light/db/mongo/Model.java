package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
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

    @FunctionalInterface
    public interface Callback<E> {
        void callback(Throwable error, E result);
    }

    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> collection;

    public Model(String domain, String code) {
        this.initialize(domain, code, null, null);
    }

    public Model(String domain, String code, String table, String define) {
        this.initialize(domain, code, table, define);
    }

    private void initialize(String domain, String code, String table, String define) {
        this.client = Connection.instance(Config.instance().environment);
        this.db = this.client.getDatabase(domain);

        if (table != null) {
            this.collection = this.db.getCollection(table);
        }

        logger.info("table : " + table);
    }

    void getBy(Callback<List<Document>> handle) {

        this.collection.find().into(new ArrayList<>(),
                new SingleResultCallback<List<Document>>() {
                    @Override
                    public void onResult(final List<Document> result, final Throwable error) {
                        handle.callback(error, result);
                    }
                });
    }

}
