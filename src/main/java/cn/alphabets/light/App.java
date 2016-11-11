package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.http.Dispatcher;
import cn.alphabets.light.http.TimeoutHandlerImpl;
import cn.alphabets.light.http.session.AuthHandlerImpl;
import cn.alphabets.light.http.session.MongoSessionStoreImpl;
import cn.alphabets.light.http.session.SessionHandlerImpl;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by luohao on 16/10/20.
 */
public class App {


    private static final Logger log = LoggerFactory.getLogger(App.class);

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

        CacheManager.INSTANCE.setUp(Environment.instance().getAppName());
        ConfigManager.INSTANCE.setUp();

        //初始化基础数据
        CacheManager.INSTANCE.setUp(options.getAppDomain());

        //初始化timeout
        router.route().handler(new TimeoutHandlerImpl(ConfigManager.INSTANCE.getAppTimeout() * 1000));

        //打印每个请求
        router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));

        //处理静态资源
        router.route("/static/*").handler(StaticHandler.create().setWebRoot("static"));

        //cookie处理
        router.route().handler(CookieHandler.create());

        //session处理, 超时30天
        long sessionTimeoute = 1000L * 60 * 60 * ConfigManager.INSTANCE.getAppSessionTimeout();
        router.route().handler(SessionHandlerImpl
                .create(new MongoSessionStoreImpl(mongo, vertx))
                .setNagHttps(false)
                .setSessionTimeout(sessionTimeoute));

        //request body处理
        router.route().handler(BodyHandler.create());

        //未登录请求过滤
        if (!options.isDev()) {
            router.route().handler(new AuthHandlerImpl(mongo));
        }


        //响应头带上'x-response-time' 用来标示服务器响应时间
        if (options.isDev()) {
            router.route().handler(ResponseTimeHandler.create());
        }

        Dispatcher dispatcher = new Dispatcher();

        //数据型接口路由
        // TODO: 2016/10/28 完成数据型接口路由

        //处理型接口路由
        dispatcher.routeProcessAPI(mongo, router, options);

        //画面路径路由
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
