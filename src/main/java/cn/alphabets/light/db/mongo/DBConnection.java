package cn.alphabets.light.db.mongo;

import cn.alphabets.light.AppOptions;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jongo.Jongo;


/**
 * Created by luohao on 16/10/22.
 */
public class DBConnection extends MongoClient {

    AppOptions options;

    public DBConnection(AppOptions opts) {
        super(new MongoClientURI(
                String.format("mongodb://%s:%s@%s:%s/%s",
                        opts.getDbUser(),
                        opts.getDbPass(),
                        opts.getDbHost(),
                        opts.getDbPort(),
                        opts.getAppDomain())));
        options = opts;
    }

    public MongoDatabase getDB() {
        return super.getDatabase(options.getAppDomain());
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return getDB().getCollection(collectionName);
    }

    public org.jongo.MongoCollection getCollection2(String collectionName) {
        Jongo jongo = new Jongo(getDB(options.getAppDomain()));

        return jongo.getCollection(collectionName);
    }


}
