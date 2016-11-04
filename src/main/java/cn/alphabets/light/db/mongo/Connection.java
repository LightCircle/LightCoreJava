package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import com.mongodb.ConnectionString;
import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection
 * Typically you only create one MongoClient instance for a given database cluster and use it across your application.
 */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static MongoClient instance;

    private Connection() {
    }

    public static MongoClient instance(Config.Environment environment) {
        if (instance == null) {
            ConnectionString uri = new ConnectionString(
                    String.format("mongodb://%s:%s@%s:%s/?authMechanism=%s&authSource=%s",
                            environment.getMongoUser(),
                            environment.getMongoPass(),
                            environment.getMongoHost(),
                            environment.getMongoPort(),
                            environment.getMongoAuth(),
                            environment.getAppName()
                    ));

            logger.info("host : " + environment.getMongoHost());
            logger.info("port : " + environment.getMongoPort());
            logger.info("user : " + environment.getMongoUser());
            logger.info("pass : " + environment.getMongoPass());
            instance = MongoClients.create(uri);
        }

        return instance;
    }
}
