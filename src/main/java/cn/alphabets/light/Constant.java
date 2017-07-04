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
    public static final String CFK_IGNORE_CSRF = "ignore.csrf";                 // 不验证csrf
    public static final String CFK_TIMEZONE = "app.timezone";

    public static final String SK_USER = "_sk_user";                            // 当前用户 session key, SK 打头为Session中的key
    public static final String SK_DOMAIN = "cn.alphabets.light.sk_domain";      // domain key
    public static final String SK_CODE = "cn.alphabets.light.sk_code";          // tenant key

    public static final String ENV_LIGHT_MONGO_HOST = "LIGHTDB_HOST";
    public static final String ENV_LIGHT_MONGO_PORT = "LIGHTDB_PORT";
    public static final String ENV_LIGHT_MONGO_USER = "LIGHTDB_USER";
    public static final String ENV_LIGHT_MONGO_PASS = "LIGHTDB_PASS";
    public static final String ENV_LIGHT_MONGO_AUTH = "LIGHTDB_AUTH";
    public static final String ENV_LIGHT_APP_NAME = "APPNAME";
    public static final String ENV_LIGHT_APP_PORT = "PORT";
    public static final String ENV_LIGHT_APP_PACKAGE = "PACKAGE";
    public static final String ENV_LIGHT_APP_MASTER = "MASTER";

    public static final String ENV_LIGHT_MYSQL_HOST = "LIGHTMYSQL_HOST";
    public static final String ENV_LIGHT_MYSQL_PORT = "LIGHTMYSQL_PORT";

    public static final String SYSTEM_DB = "light";
    public static final String SYSTEM_DB_PREFIX = "light";
    public static final String SYSTEM_DB_CONFIG = "configuration";
    public static final String SYSTEM_DB_VALIDATOR = "validator";
    public static final String SYSTEM_DB_I18N = "i18n";
    public static final String SYSTEM_DB_STRUCTURE = "structure";
    public static final String SYSTEM_DB_BOARD = "board";
    public static final String SYSTEM_DB_ROUTE = "route";
    public static final String SYSTEM_DB_TENANT = "tenant";
    public static final String SYSTEM_DB_FUNCTION = "function";
    public static final String SYSTEM_DB_FILE = "file";
    public static final String SYSTEM_DB_JOB = "job";
    public static final String SYSTEM_DB_COUNTER = "counter";
    public static final String SYSTEM_DB_ETL = "etl";
    public static final String SYSTEM_DB_SETTING = "setting";
    public static final String SYSTEM_DB_CODE = "code";


    public static final String MODEL_PREFIX = "Mod";
    public static final String DEFAULT_TENANT = "default";
    public static final String DEFAULT_PACKAGE_NAME = "cn.alphabets.light";
    public static final String DEFAULT_JOB_USER_ID = "000000000000000000000000";
    public static final String DEFAULT_JOB_USER_LANG = "zh";
    public static final Integer DEFAULT_LIMIT = 0;                              // 一次允许获取的数据库记录数
    public static final Integer VALID = 1;                                      // 有效数据库记录
    public static final Integer INVALID = 0;                                    // 无效数据库记录

    public static final Long KIND_BOARD_USER_DATA = 0L;                         // 用户定义数据型API
    public static final Long KIND_BOARD_USER_LOGIC = 1L;                        // 用户定义逻辑型API
    public static final Long KIND_BOARD_SYSTEM_DATA = 2L;                       // 系统定义数据型API
    public static final Long KIND_BOARD_SYSTEM_LOGIC = 3L;                      // 系统定义逻辑型API

    public static final String COOKIE_KEY_LANG = "light.lang";
    public static final String COOKIE_KEY_ACCEPT_LANGUAGE = "Accept-Language";

    public static final String PARAM_FILE_KEEP = "keep_physical_file";
    public static final String PARAM_FILE_NAME = "filename";
    public static final String PARAM_FILE_TYPE = "content_type";
    public static final String PARAM_FILE_PHYSICAL = "file";
    public static final String PARAM_CONDITION = "condition";
    public static final String PARAM_FREE = "free";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_SELECT = "select";
    public static final String PARAM_ID = "id";
    public static final String PARAM_DATA = "data";
    public static final String PARAM_SKIP = "skip";
    public static final String PARAM_LIMIT = "limit";

    public static final int GLOBAL_ERROR_STATUS_CODE = 520;
}
