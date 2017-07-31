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


    //using 'SLF4JLogDelegateFactory' for logging
    static {
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, "io.vertx.core.logging.SLF4JLogDelegateFactory");
        System.setProperty("vertx.disableFileCaching", "true");
    }

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);
    private static Environment instance;
    public final Args args = new Args();

    // 配置文件config.yml内容
    public ConfigFile.ConfigApp app;
    public ConfigFile.ConfigMongoDB mongodb;
    public ConfigFile.ConfigMySQL mysql;
    public String lang;
    public String[] ignore;
    public ConfigFile.ConfigBinary binary;

    private Environment() {
    }

    public static Environment initialize(String[] args) {
        Environment environment = new Environment();
        environment.args.initArgs(args);
        if (environment.args.local) {
            instance = new Yaml().loadAs(ClassLoader.getSystemResourceAsStream("config.yml"), Environment.class);
            instance.args.initArgs(args);
        } else {
            instance = environment;
        }
        return instance;
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

    public boolean isRDB() {
        if (this.args.local) {
            return this.mysql != null;
        }
        return System.getenv(Constant.ENV_LIGHT_MYSQL_HOST) != null;
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
            this.generate = list.contains("-generate");
        }

        public boolean local;
        public boolean push;
        public boolean dump;
        public boolean restore;
        public boolean generate;
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
        String port = System.getenv(Constant.ENV_LIGHT_APP_PORT);
        if (port == null) {
            return 7000;
        }
        return Integer.parseInt(port);
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
        String port = System.getenv(Constant.ENV_LIGHT_MONGO_PORT);
        if (port == null) {
            return "57017";
        }
        return port;
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
        String auth = System.getenv(Constant.ENV_LIGHT_MONGO_AUTH);
        if (auth == null) {
            return "SCRAM-SHA-1";
        }
        return auth;
    }

    public String getPackages() {
        if (this.args.local) {
            return this.app.getPackages();
        }
        return System.getenv(Constant.ENV_LIGHT_APP_PACKAGE);
    }

    public boolean isMaster() {
        if (this.args.local) {
            return this.app.isMaster();
        }

        String master = System.getenv(Constant.ENV_LIGHT_APP_MASTER);
        if (master == null) {
            return false;
        }
        return "true".equals(master.toLowerCase());
    }

    public boolean isLocal() {
        if (this.args.local) {
            return this.app.isLocal();
        }

        String local = System.getenv(Constant.ENV_LIGHT_APP_LOCAL);
        if (local == null) {
            return false;
        }
        return "true".equals(local.toLowerCase());
    }

    public String getMySQLHost() {
        if (this.args.local) {
            return this.mysql.getHost();
        }
        return System.getenv(Constant.ENV_LIGHT_MONGO_HOST);
    }

    public int getMySQLPort() {
        if (this.args.local) {
            return this.mysql.getPort();
        }
        return Integer.parseInt(System.getenv(Constant.ENV_LIGHT_MYSQL_PORT));
    }


    public String getMySQLUser() {
        if (this.args.local) {
            return this.mysql.getUser();
        }
        return System.getenv(Constant.ENV_LIGHT_MYSQL_USER);
    }

    public String getMySQLPass() {
        if (this.args.local) {
            return this.mysql.getPass();
        }
        return System.getenv(Constant.ENV_LIGHT_MYSQL_PASS);
    }

}
