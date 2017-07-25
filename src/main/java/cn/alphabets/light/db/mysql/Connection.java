package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Environment;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;

/**
 * Connection
 * Created by lilin on 2017/7/17.
 */
public class Connection {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);
    private static BasicDataSource instance;

    private Connection() {
    }

    public static java.sql.Connection instance(Environment env) throws SQLException {
        if (instance == null) {

            logger.info("host : " + env.getMySQLHost());
            logger.info("port : " + env.getMySQLPort());
            logger.info("user : " + env.getMySQLUser());
            logger.info("pass : " + env.getMySQLPass());
            logger.info("db   : " + env.getAppName());

            createDataSource(env);
        }

        java.sql.Connection connection = instance.getConnection();

        // renew connection
        if (connection.isClosed()) {
            createDataSource(env);
            connection = instance.getConnection();
        }

        return connection;
    }

    private static void createDataSource(Environment env) {
        instance = new BasicDataSource();
        instance.setDriverClassName("com.mysql.jdbc.Driver");
        instance.setUrl(String.format("jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&useSSL=true",
                env.getMySQLHost(),
                env.getMySQLPort(),
                env.getAppName()
        ));

        instance.setUsername(env.getMySQLUser());
        instance.setPassword(env.getMySQLPass());

        instance.setInitialSize(5);
        instance.setMaxIdle(10);
        instance.setMaxTotal(100);
    }

}
