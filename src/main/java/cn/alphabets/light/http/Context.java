package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModUser;
import cn.alphabets.light.model.ModCommon;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Context
 * Created by luohao on 16/10/22.
 */
public class Context {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    public final Params params;

    protected RoutingContext ctx;
    protected String domain;
    protected String code;
    protected String uid;

    public Context(Params params, String domain, String code, String uid) {
        this.params = params;
        this.domain = domain;
        this.code = code;
        this.uid = uid;
    }

    public Context(RoutingContext ctx) {
        this(ctx, null, null);
    }

    public Context(RoutingContext ctx, String domain, String code) {
        this.ctx = ctx;
        this.domain = domain;
        this.code = code;

        Document parameter = new Document();

        // path params
        if (ctx.pathParams().size() > 0) {
            parameter.putAll(ctx.pathParams());
        }

        // query
        if (ctx.request().uri().indexOf('?') > 0) {
            parameter.putAll(Helper.unParam(ctx.request().uri()));
        }

        // form
        if (ctx.getBodyAsString().length() > 0) {

            // xml格式的数据
            if (ctx.request().getHeader("content-type").equals("application/xml")) {
                parameter.put("xml", ctx.getBodyAsString());
            } else {
                parameter.putAll(Document.parse(ctx.getBodyAsString()));
            }
        }

        // file
        List<RequestFile> files = null;
        if (ctx.fileUploads().size() > 0) {
            files = ctx.fileUploads().stream().map(item -> {
                RequestFile file = new RequestFile(Constant.PARAM_FILE_NAME, item.fileName());
                file.put(Constant.PARAM_FILE_TYPE, item.contentType());
                file.put(Constant.PARAM_FILE_PHYSICAL, item.uploadedFileName());
                return file;
            }).collect(Collectors.toList());
        }

        this.params = new Params(parameter, files);

        logger.debug("req params : " + this.params.toString());
    }

    public HttpServerResponse res() {
        return this.ctx.response();
    }

    public HttpServerRequest req() {
        return this.ctx.request();
    }

    public RoutingContext ctx() {
        return this.ctx;
    }

    public Session session() {
        return ctx.session();
    }

    public Object user() {
        if (ctx == null) {
            return null;
        }
        return ctx.session().get(Constant.SK_USER);
    }

    public void setUser(ModCommon user) {
        ctx.session().put(Constant.SK_USER, user);
    }

    public String domain() {
        return getDomain();
    }

    public String getDomain() {
        if (this.domain != null) {
            return this.domain;
        }

        String sessionDomain = ctx.session().get(Constant.SK_DOMAIN);
        if (sessionDomain != null) {
            return sessionDomain;
        }

        return Environment.instance().getAppName();
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String code() {
        return getCode();
    }

    public String getCode() {
        if (this.code != null) {
            return this.code;
        }

        String sessionCode = ctx.session().get(Constant.SK_CODE);
        if (sessionCode != null) {
            return sessionCode;
        }

        return Constant.DEFAULT_TENANT;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String uid() {
        if (this.uid != null) {
            return this.uid;
        }

        ModCommon user = (ModCommon) this.user();
        if (user == null) {
            return null;
        }

        if (user.get_id() == null) {
            return null;
        }

        return user.get_id().toHexString();
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public TimeZone tz() {
        return this.getTimeZone();
    }

    public TimeZone getTimeZone() {

        try {
            ModUser user = (ModUser) this.user();
            if (user != null && StringUtils.isNotEmpty(user.getTimezone())) {
                return TimeZone.getTimeZone(user.getTimezone());
            }
        } catch (Exception e) {
            logger.warn("Error get user timezone , use system config timezone instead:", e);
        }
        String conftz = ConfigManager.INSTANCE.getString(Constant.CFK_TIMEZONE);
        if (conftz != null) {
            return TimeZone.getTimeZone(conftz);
        }
        return TimeZone.getDefault();
    }

    public String getLang() {

        // The cookie takes precedence
        Cookie uaLang = this.ctx.getCookie(Constant.COOKIE_KEY_LANG);
        if (uaLang != null) {
            return uaLang.getValue();
        }

        // Ping available languages
        uaLang = this.ctx.getCookie((Constant.COOKIE_KEY_ACCEPT_LANGUAGE));
        if (uaLang != null) {
            return uaLang.getValue().split(",")[0];
        }

        return "zh";
    }


}
