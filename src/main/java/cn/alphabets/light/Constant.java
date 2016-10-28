package cn.alphabets.light;

/**
 * Created by luohao on 2016/10/28.
 */
public class Constant {

    /**
     * CFK 打头为APP设定的 key
     */

    /**
     * 请求超时设定
     */
    public static final String[] CFK_REQUEST_TIMEOUT = new String[]{"app", "timeout"};

    /**
     * 请求超时例外设定
     */
    public static final String[] CFK_REQUEST_IGNORE_TIMEOUT = new String[]{"ignore", "timeout"};

    /**
     * session有效期设定
     */
    public static final String[] CFK_SESSION_TIMEOUT = new String[]{"app", "sessionTimeout"};

    /**
     * 不需要登录能访问url
     */
    public static final String[] CFK_IGNORE_AUTH = new String[]{"ignore", "auth"};

    /**
     * SK 打头为Session中的key
     */

    /**
     * 当前用户 session key
     */
    public static final String SK_USER = "_sk_user";
}
