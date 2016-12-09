package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Environment;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


/**
 * Connection
 * Typically you only create one MongoClient instance for a given database cluster and use it across your application.
 */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static MongoClient instance;

    private Connection() {
    }

    public static MongoClient instance(Environment env) {
        if (instance == null) {
            MongoClientURI uri = new MongoClientURI(
                    String.format("mongodb://%s:%s/",
                            env.getMongoHost(),
                            env.getMongoPort(),
                            env.getMongoAuth(),
                            env.getAppName()
                    ));

            logger.info("host : " + env.getMongoHost());
            logger.info("port : " + env.getMongoPort());
            logger.info("user : " + env.getMongoUser());
            logger.info("pass : " + env.getMongoPass());
            instance = new MongoClient(uri);
        }

        return instance;
    }
}
