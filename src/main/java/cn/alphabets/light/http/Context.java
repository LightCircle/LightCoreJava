package cn.alphabets.light.http;

import cn.alphabets.light.ConfigFile;
import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.model.Json;
import cn.alphabets.light.model.ModBase;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.lang.reflect.Type;
import java.util.List;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

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

        Json parameter = new Json();

        // query
        if (ctx.request().params().size() > 0) {
            parameter.putAll(Helper.unParam(ctx.request().uri()));
        }

        // form
        if (ctx.getBodyAsString().length() > 0) {
            parameter.putAll(Json.parse(ctx.getBodyAsString()));
        }

        // TODO: url path params

        // TODO: file

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
        String json = ctx.session().get(Constant.SK_USER);
        if (StringUtils.isEmpty(json)) {
            return null;
        }

        try {
            Class type = Class.forName(Environment.instance().getPackages() + ".entity.User");
            return ModBase.fromJson(json, type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException();
        }
    }

    public void setUser(ModBase user) {
        String json = user.toJson();
        ctx.session().put(Constant.SK_USER, json);
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

        ModBase user = (ModBase) this.user();
        if (user == null) {
            return null;
        }

        return user.get_id().toString();
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    private String domain;
    private String code;
    private String uid;

    public static class Params {

        private Json json;
        public Params(Json json) {
            this.json = json;
            this.condition = (Json) json.get("condition");
            this.id = json.getString("id");
            this.data = json.get("data");
            this.skip = 0;
            this.limit = Constant.DEFAULT_LIMIT;

            String skip = json.getString("skip");
            if (StringUtils.isNotEmpty(skip)) {
                this.skip = Integer.parseInt(skip);
            }

            String limit = json.getString("limit");
            if (StringUtils.isNotEmpty(limit)) {
                this.limit = Integer.parseInt(limit);
            }

            // TODO
            //select;
            //sort;
            //files;
        }

        public String getString(String key) {
            return this.json.getString(key);
        }

        public Json getCondition() {
            if (this.condition == null) {
                this.condition = new Json();
            }
            return this.condition;
        }

        public void setCondition(Json condition) {
            this.condition = condition;
        }

        public Object getData() {
            return data;
        }

        public void setData(ModBase data) {
            this.data = data.toDoc();
        }

        public void setDataList(List<ModBase> data) {
            this.data = data;
        }

        public void setDataJson(Json data) {
            this.data = data;
        }

        public void setDataJsonList(List<Json> data) {
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

        public List<Object> getFiles() {
            return files;
        }

        public void setFiles(List<Object> files) {
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

        private Json condition;
        private Object data;
        private Object id;
        private List<String> select;
        private List<String> sort;
        private List<Object> files;
        private int skip;
        private int limit;
    }

}
