package cn.alphabets.light;

import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.http.Dispatcher;
import cn.alphabets.light.http.session.MongoSessionStoreImpl;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.SessionHandler;

/**
 * Created by luohao on 16/10/20.
 */
public class App {

    private Vertx vertx;
    private HttpServer server;
    private Router router;
    private DBConnection mongo;
    private AppOptions options;

    public App(AppOptions opts) {

        /*
          程序设定
         */
        options = opts;

        /*
          运行环境
         */
        vertx = Vertx.vertx(new VertxOptions());

        /*
          路由
         */
        router = Router.router(vertx);

        /*
          web服务
         */
        server = vertx.createHttpServer();

        /*
          数据库连接
         */
        mongo = new DBConnection(options);
    }


    public void start() {

        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(new MongoSessionStoreImpl(mongo,vertx)).setNagHttps(false).setSessionTimeout(1000L * 60 * 60 * 24 * 30));
        router.route().handler(BodyHandler.create());
        if (options.isDev()) {
            router.route().handler(ResponseTimeHandler.create());
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.routeProcessAPI(mongo, router, options);
        dispatcher.routeView(mongo, router, options);

        server.requestHandler(router::accept).listen(options.getAppPort());
        router.getRoutes().forEach(r -> System.out.println(r.getPath()));
    }

}
