package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.model.ModCommon;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Context
 * Created by luohao on 16/10/22.
 */
public class Context {

    private static final Logger logger = LoggerFactory.getLogger(Context.class);

    private RoutingContext ctx;

    public final Params params;

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
            parameter.putAll(Document.parse(ctx.getBodyAsString()));
        }

        // file
        if (ctx.fileUploads().size() > 0) {
            Set<FileUpload> uploads = ctx.fileUploads();
            List<RequestFile> files = uploads.stream().map((x) -> {
                RequestFile file = new RequestFile(Constant.PARAM_FILE_NAME, x.fileName());
                file.put(Constant.PARAM_FILE_TYPE, x.contentType());
                file.put(Constant.PARAM_FILE_PHYSICAL, x.uploadedFileName());
                return file;
            }).collect(Collectors.toList());
            this.params = new Params(parameter, files);
        } else {
            this.params = new Params(parameter);
        }

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
        return ctx.session().get(Constant.SK_USER);
    }

    public void setUser(ModCommon user) {
        ctx.session().put(Constant.SK_USER, user);
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

    public TimeZone getTimeZone() {
        //TODO get timezone from config
        return TimeZone.getTimeZone("GMT+8");
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

    private String domain;
    private String code;
    private String uid;


    public static class RequestFile extends Document {
        public RequestFile() {
        }

        public RequestFile(String key, Object value) {
            super(key, value);
        }

        public RequestFile(Map<String, Object> map) {
            super(map);
        }

        public String getFilePath() {
            return this.getString(Constant.PARAM_FILE_PHYSICAL);
        }

        public String getContentType() {
            return this.getString(Constant.PARAM_FILE_TYPE);
        }

        public String getFileName() {
            return this.getString(Constant.PARAM_FILE_NAME);
        }
    }

    public static class Params {
        private Document json;
        private List<RequestFile> files;

        public Params(Document json) {
            this(json, null);
        }

        public Params(Document json, List<RequestFile> files) {
            this.json = json;
            this.files = files;
        }

        public String getString(String key) {
            return this.json.getString(key);
        }

        public void set(String key, Object val) {
            this.json.put(key, val);
        }

        public List<RequestFile> getFiles() {
            return files;
        }


        public Document getJson() {
            return json;
        }

        @Override
        public String toString() {
            return "\n{" +
                    "\n\tparams = " + json.toJson() +
                    "\n\tfiles = " + (files == null ? "null" : "\n\t\t" + files.stream().map(file -> file.toString()).collect(Collectors.joining("\n\t\t"))) +
                    "\n}";
        }

    }

}
