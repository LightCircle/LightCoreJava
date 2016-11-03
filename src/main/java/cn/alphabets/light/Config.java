package cn.alphabets.light;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * User application configuration file
 */
public class Config {

    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config instance;
    public ConfigApp app;
    public ConfigMongoDB mongodb;
    public ConfigMySQL mysql;
    public Args args;
    public Environment env;

    private Config() {
        env = new Environment();
        args = new Args();
    }

    public static Config instance() {
        if (instance == null) {
            instance = new Yaml().loadAs(ClassLoader.getSystemResourceAsStream("config.yml"), Config.class);
        }
        return instance;
    }

    public static Config instance(String file) {
        if (instance == null) {
            try {
                instance = new Yaml().loadAs(new FileInputStream(new File(file)), Config.class);
            } catch (FileNotFoundException e) {
                logger.error("The Config file not found.", e);
            }
        }
        return instance;
    }

    public static final class Constant {
        private Constant() {
        }

        public static final String ENV_LIGHT_MONGO_HOST = "LIGHTDB_HOST";
        public static final String ENV_LIGHT_MONGO_PORT = "LIGHTDB_PORT";
        public static final String ENV_LIGHT_MONGO_USER = "LIGHTDB_USER";
        public static final String ENV_LIGHT_MONGO_PASS = "LIGHTDB_PASS";
        public static final String ENV_LIGHT_MONGO_AUTH = "LIGHTDB_AUTH";
        public static final String ENV_LIGHT_APP_NAME = "APPNAME";
        public static final String ENV_LIGHT_APP_PORT = "PORT";
    }

    public static class ConfigApp {
        public boolean dev;
        public int port;
        public String domain;
        public boolean master;
        public boolean local;
    }

    public static class ConfigMongoDB {
        public String host;
        public int port;
        public String user;
        public String pass;
        public String auth;
    }

    public static class ConfigMySQL {
        public String host;
        public int port;
        public String user;
        public String pass;
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

    /**
     * System environment variable
     */
    public static class Environment {

        public String appName() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.app.domain;
            }
            return System.getenv(Constant.ENV_LIGHT_APP_NAME);
        }

        public int appPort() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.app.port;
            }
            return Integer.parseInt(System.getenv(Constant.ENV_LIGHT_APP_PORT));
        }

        public String mongoHost() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.mongodb.host;
            }
            return System.getenv(Constant.ENV_LIGHT_MONGO_HOST);
        }

        public int mongoPort() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.mongodb.port;
            }
            return Integer.parseInt(System.getenv(Constant.ENV_LIGHT_MONGO_PORT));
        }

        public String mongoUser() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.mongodb.user;
            }
            return System.getenv(Constant.ENV_LIGHT_MONGO_USER);
        }

        public String mongoPass() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.mongodb.pass;
            }
            return System.getenv(Constant.ENV_LIGHT_MONGO_PASS);
        }

        public String mongoAuth() {
            Config config = Config.instance();
            if (config.args.local) {
                return config.mongodb.auth;
            }
            return System.getenv(Constant.ENV_LIGHT_MONGO_AUTH);
        }
    }
}
