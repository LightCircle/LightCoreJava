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
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Context
 * Created by luohao on 16/10/22.
 */
public class Context {

    private static final Logger log = LoggerFactory.getLogger(Context.class);

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

        // query
        if (ctx.request().params().size() > 0) {
            parameter.putAll(Helper.unParam(ctx.request().uri()));
        }

        // form
        if (ctx.getBodyAsString().length() > 0) {
            parameter.putAll(Document.parse(ctx.getBodyAsString()));
        }

        // TODO: url path params

        // file
        if (ctx.fileUploads().size() > 0) {
            Set<FileUpload> uploads = ctx.fileUploads();
            List<Document> files = uploads.stream().map((x) -> {
                Document file = new Document(Constant.PARAM_FILE_NAME, x.fileName());
                file.put(Constant.PARAM_FILE_TYPE, x.contentType());
                file.put(Constant.PARAM_FILE_PHYSICAL, x.uploadedFileName());
                return file;
            }).collect(Collectors.toList());
            parameter.put(Constant.PARAM_FILES, files);
        }

        log.debug(parameter.toJson());
        this.params = new Params(parameter);
    }

    public HttpServerResponse res() {
        return this.ctx.response();
    }

    public HttpServerRequest req() {
        return this.ctx.request();
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

        return user.get_id().toString();
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public static class Params {

        @SuppressWarnings("unchecked")
        public Params(Document json) {
            this.json = json;
            this.condition = (Document) json.get(Constant.PARAM_CONDITION);
            this.id = json.getString(Constant.PARAM_ID);
            this.data = json.get(Constant.PARAM_DATA);
            this.skip = 0;
            this.limit = Constant.DEFAULT_LIMIT;
            this.files = (List<Document>)json.get(Constant.PARAM_FILES);

            String skip = json.getString(Constant.PARAM_SKIP);
            if (StringUtils.isNotEmpty(skip)) {
                this.skip = Integer.parseInt(skip);
            }

            String limit = json.getString(Constant.PARAM_LIMIT);
            if (StringUtils.isNotEmpty(limit)) {
                this.limit = Integer.parseInt(limit);
            }

            // TODO
            //select;
            //sort;
        }

        public String getString(String key) {
            return this.json.getString(key);
        }

        public void set(String key, Object val) {
            this.json.put(key, val);
        }

        public Document getCondition() {
            if (this.condition == null) {
                this.condition = new Document();
            }
            return this.condition;
        }

        public void setCondition(Document condition) {
            this.condition = condition;
        }

        public Object getData() {
            return data;
        }

        public void setData(ModCommon data) {
            this.data = data.toDocument();
        }

        public void setDataList(List<? extends ModCommon> data) {
            this.data = data;
        }

        public void setDataJson(Document data) {
            this.data = data;
        }

        public void setDataJsonList(List<Document> data) {
            this.data = data;
        }

        public Object getId() {
            return id;
        }

        public void setId(Object id) {
            this.id = id;
        }

        public List<String> getSelect() {
            return select;
        }

        public void setSelect(List<String> select) {
            this.select = select;
        }

        public List<String> getSort() {
            return sort;
        }

        public void setSort(List<String> sort) {
            this.sort = sort;
        }

        public List<Document> getFiles() {
            return files;
        }

        public void setFiles(List<Document> files) {
            this.files = files;
        }

        public int getSkip() {
            return skip;
        }

        public void setSkip(int skip) {
            this.skip = skip;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        private Document json;
        private Document condition;
        private Object data;
        private Object id;
        private List<String> select;
        private List<String> sort;
        private List<Document> files;
        private int skip;
        private int limit;
    }

}
