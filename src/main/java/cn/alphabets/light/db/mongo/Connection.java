package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Environment;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Connection
 * Typically you only create one MongoClient instance for a given database cluster and use it across your application.
 */
public class Connection {

    private static final Logger logger = LogManager.getLogger(Connection.class);
    private static MongoClient instance;

    private Connection() {
    }

    public static MongoClient instance(Environment env) {
        if (instance == null) {
            MongoClientURI uri = new MongoClientURI(
                    String.format("mongodb://%s:%s@%s:%s/?authMechanism=%s&authSource=%s",
                            env.getMongoUser(),
                            env.getMongoPass(),
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
