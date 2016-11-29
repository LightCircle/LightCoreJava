package cn.alphabets.light;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * User application configuration file
 */
public class Environment {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);
    private static Environment instance;
    public final Args args = new Args();

    public ConfigFile.ConfigApp app;
    public ConfigFile.ConfigMongoDB mongodb;
    public ConfigFile.ConfigMySQL mysql;

    private Environment() {
    }

    public static Environment instance() {
        if (instance == null) {
            instance = new Yaml().loadAs(ClassLoader.getSystemResourceAsStream("config.yml"), Environment.class);
        }
        return instance;
    }

    public static Environment instance(String file) {
        if (instance == null) {
            try {
                instance = new Yaml().loadAs(new FileInputStream(new File(file)), Environment.class);
            } catch (FileNotFoundException e) {
                logger.error("The Config file not found.", e);
            }
        }
        return instance;
    }

    public static void clean() {
        instance = null;
    }

    /**
     * Command line parameters
     */
    public static class Args {
        public void initArgs(String[] args) {
            List<String> list = Arrays.asList(args);
            this.local = list.contains("-local");
            this.push = list.contains("-push");
            this.dump = list.contains("-dump");
            this.restore = list.contains("-restore");
        }

        public boolean local;
        public boolean push;
        public boolean dump;
        public boolean restore;
    }

    public String getAppName() {
        if (this.args.local) {
            return this.app.getDomain();
        }
        return System.getenv(Constant.ENV_LIGHT_APP_NAME);
    }

    public int getAppPort() {
        if (this.args.local) {
            return this.app.getPort();
        }
        return Integer.parseInt(System.getenv(Constant.ENV_LIGHT_APP_PORT));
    }

    public String getMongoHost() {
        if (this.args.local) {
            return this.mongodb.getHost();
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_HOST);
    }

    public String getMongoPort() {
        if (this.args.local) {
            return String.valueOf(this.mongodb.getPort());
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_PORT);
    }

    public String getMongoUser() {
        if (this.args.local) {
            return this.mongodb.getUser();
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_USER);
    }

    public String getMongoPass() {
        if (this.args.local) {
            return this.mongodb.getPass();
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_PASS);
    }

    public String getMongoAuth() {
        if (this.args.local) {
            return this.mongodb.getAuth();
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_AUTH);
    }

    public String getPackages() {
        if (this.args.local) {
            return this.app.getPackages();
        }
        return System.getenv(Constant.ENV_LIGHT_APP_PACKAGE);
    }

    public String getMySQLHost() {
        if (this.args.local) {
            return this.mysql.getHost();
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_HOST);
    }

    public String getMySQLPort() {
        if (this.args.local) {
            return String.valueOf(this.mysql.getPort());
        }
        return System.getenv(Constant.ENV_LIGHT_MYSQL_PORT);
    }

}
