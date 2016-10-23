package cn.alphabets.light.http;

import cn.alphabets.light.db.mongo.DBConnection;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;

/**
 * Created by luohao on 16/10/22.
 */
public class Context {
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

    public Session session() {
        return ctx.session();
    }
}
