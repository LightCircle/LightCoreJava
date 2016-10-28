package cn.alphabets.light;

import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.http.Dispatcher;
import cn.alphabets.light.http.session.MongoSessionStoreImpl;
import cn.alphabets.light.http.session.SessionHandlerImpl;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.SessionHandler;
import org.apache.commons.lang3.StringUtils;

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
          设定使用log4j2
         */
        System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME
                , "io.vertx.core.logging.SLF4JLogDelegateFactory");

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
        Logger logger = LoggerFactory.getLogger(App.class);
        router.route().handler(CookieHandler.create());

        router.route().handler(SessionHandlerImpl
                .create(new MongoSessionStoreImpl(mongo, vertx))
                .setNagHttps(false)
                .setSessionTimeout(1000L * 60 * 10));
        router.route().handler(BodyHandler.create());
        if (options.isDev()) {
            router.route().handler(ResponseTimeHandler.create());
        }

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.routeProcessAPI(mongo, router, options);
        dispatcher.routeView(mongo, router, options);

        server.requestHandler(router::accept).listen(options.getAppPort());
        logger.info("========route list========");
        StringBuilder stringBuilder = new StringBuilder().append(System.lineSeparator());
        router.getRoutes().forEach(r -> {
            if (StringUtils.isNotEmpty(r.getPath())) {
                stringBuilder.append(r.getPath()).append(System.lineSeparator());
            }
        });
        logger.info(stringBuilder.toString());
        logger.info("==========================");
    }
}
