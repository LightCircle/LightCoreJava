package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mongo.DBConnection;
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

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

/**
 * Created by luohao on 16/10/22.
 */
public class Context {

    private static final Logger log = LoggerFactory.getLogger(Context.class);

    private RoutingContext ctx;
    private DBConnection db;

    public Context(RoutingContext ctx, DBConnection db) {
        this.ctx = ctx;
        this.db = db;
    }

    public HttpServerResponse res() {
        return ctx.response();
    }

    public HttpServerRequest req() {
        return ctx.request();
    }

    public DBConnection db() {
        return this.db;
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

}
