package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Helper;
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

    public <T> T user(Class<T> clz) {
        String json = ctx.session().get(Constant.SK_USER);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        return ModBase.fromJson(json, clz);
    }

    public void setUser(ModBase user) {
        String json = user.toJson();
        ctx.session().put(Constant.SK_USER, json);
    }

    public void end(Document bson) {
        String json = bson.toJson(new JsonWriterSettings(JsonMode.SHELL, true));
        /**
         * 执行以下替换操作：
         * ObjectId("57c52f87fb35fd050073f9c4") -> "57c52f87fb35fd050073f9c4"
         * ISODate("2016-08-30T07:02:31.391Z") -> "2016-08-30T07:02:31.391Z"
         */

        json = json.replaceAll("ObjectId\\((\\\"\\w{24}\\\")\\)|ISODate\\((\\\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z\\\")\\)", "$1$2");
        res().putHeader(CONTENT_TYPE, "application/json").end(json);
    }


    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String uid() {
        if (this.uid != null) {
            return this.uid;
        }

        // TODO: get uid from session

        return this.uid;
    }

    private String domain;
    private String code;
    private String uid;

    public static class Params {
        public Params(Json json) {
            this.condition = (Json)json.get("condition");
            this.data = (Json)json.get("data");
            this.skip = json.containsKey("skip") ? json.getInteger("skip") : 0;
            this.limit = json.containsKey("limit") ? json.getInteger("limit") : Constant.DEFAULT_LIMIT;
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

        public Json getData() {
            return data;
        }

        public void setData(Json data) {
            this.data = data;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
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
        private Json data; // TODO: support data list
        private String id;
        private List<String> select;
        private List<String> sort;
        private List<Object> files; // TODO
        private int skip;
        private int limit;
    }


}
