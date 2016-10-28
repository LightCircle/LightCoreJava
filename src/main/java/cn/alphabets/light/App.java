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
        Logger log = LoggerFactory.getLogger(App.class);

        //cookie处理
        router.route().handler(CookieHandler.create());

        //session处理
        router.route().handler(SessionHandlerImpl
                .create(new MongoSessionStoreImpl(mongo, vertx))
                .setNagHttps(false)
                .setSessionTimeout(1000L * 60 * 10));

        //request body处理
        router.route().handler(BodyHandler.create());

        //响应头带上'x-response-time' 用来标示服务器响应时间
        if (options.isDev()) {
            router.route().handler(ResponseTimeHandler.create());
        }

        Dispatcher dispatcher = new Dispatcher();

        //处理数据型和处理型接口路由
        dispatcher.routeProcessAPI(mongo, router, options);

        //处理画面路径路由
        dispatcher.routeView(mongo, router, options);

        //启动web server
        server.requestHandler(router::accept).listen(options.getAppPort());

        //将所有路由打印出来方便开发
        log.info("========route list========");
        StringBuilder stringBuilder = new StringBuilder().append(System.lineSeparator());
        router.getRoutes().forEach(r -> {
            if (StringUtils.isNotEmpty(r.getPath())) {
                stringBuilder.append(r.getPath()).append(System.lineSeparator());
            }
        });
        log.info(stringBuilder.toString());
        log.info("==========================");
    }
}
