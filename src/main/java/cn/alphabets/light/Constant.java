package cn.alphabets.light;

/**
 * Constant
 * Created by luohao on 2016/10/28.
 */
public class Constant {

    public static final String CFK_REQUEST_TIMEOUT = "app.timeout";             // 请求超时设定, CFK 打头为APP设定的 key
    public static final String CFK_REQUEST_IGNORE_TIMEOUT = "ignore.timeout";   // 请求超时例外设定
    public static final String CFK_SESSION_TIMEOUT = "app.sessionTimeout";      // session有效期设定
    public static final String CFK_IGNORE_AUTH = "ignore.auth";                 // 不需要登录能访问url

    public static final String SK_USER = "_sk_user";                            //当前用户 session key, SK 打头为Session中的key

    public static final String ENV_LIGHT_MONGO_HOST = "LIGHTDB_HOST";
    public static final String ENV_LIGHT_MONGO_PORT = "LIGHTDB_PORT";
    public static final String ENV_LIGHT_MONGO_USER = "LIGHTDB_USER";
    public static final String ENV_LIGHT_MONGO_PASS = "LIGHTDB_PASS";
    public static final String ENV_LIGHT_MONGO_AUTH = "LIGHTDB_AUTH";
    public static final String ENV_LIGHT_APP_NAME = "APPNAME";
    public static final String ENV_LIGHT_APP_PORT = "PORT";
    public static final String ENV_LIGHT_APP_PACKAGE = "PACKAGE";

    public static final String ENV_LIGHT_MYSQL_HOST = "LIGHTMYSQL_HOST";
    public static final String ENV_LIGHT_MYSQL_PORT = "LIGHTMYSQL_PORT";

    public static final String SYSTEM_DB = "LightDB";
    public static final String SYSTEM_DB_PREFIX = "light";
    public static final String SYSTEM_DB_CONFIG = "configuration";
    public static final String SYSTEM_DB_VALIDATOR = "validator";
    public static final String SYSTEM_DB_I18N = "i18n";
    public static final String SYSTEM_DB_STRUCTURE = "structure";
    public static final String SYSTEM_DB_BOARD = "board";
    public static final String SYSTEM_DB_ROUTE = "route";
    public static final String SYSTEM_DB_TENANT = "tenant";

    public static final String DEFAULT_PACKAGE_NAME = "cn.alphabets.light";
    public static final Integer DEFAULT_LIMIT = 100;    // 一次允许获取的数据库记录数
    public static final Integer MAX_LIMIT = -1;         // 数据库记录的最大值数
    public static final Integer VALID = 1;              // 有效数据库记录
    public static final Integer INVALID = 0;            // 无效数据库记录

}
