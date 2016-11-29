package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.http.AuthHandler;
import cn.alphabets.light.http.CSRFHandler;
import cn.alphabets.light.http.Dispatcher;
import cn.alphabets.light.http.TimeoutHandler;
import cn.alphabets.light.http.session.MongoSessionStoreImpl;
import cn.alphabets.light.http.session.SessionHandlerImpl;
import cn.alphabets.light.model.Generator;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;


/**
 * App
 * Created by luohao on 16/10/20.
 */
public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private Vertx vertx;
    private HttpServer server;
    private Router router;

    public App() {
        vertx = Vertx.vertx(new VertxOptions());
        router = Router.router(vertx);
        server = vertx.createHttpServer();
    }

    public void start() {

        Environment env = Environment.instance();
        CacheManager.INSTANCE.setUp(env.getAppName());
        ConfigManager.INSTANCE.setUp();

        // set timeout
        router.route().handler(TimeoutHandler.create(ConfigManager.INSTANCE.getAppTimeout() * 1000));

        // Print each request
        router.route().handler(LoggerHandler.create(LoggerFormat.SHORT));

        // Handle static resources
        router.route("/static/*").handler(StaticHandler.create().setWebRoot("static"));

        // Handle favicon, the "favicon.ico" file must be under static folder
        router.route().handler(FaviconHandler.create("static/favicon.ico"));

        // Handle cookie
        router.route().handler(CookieHandler.create());

        // Handle session, overtime 30 days
        long sessionTimeout = 1000L * 60 * 60 * ConfigManager.INSTANCE.getAppSessionTimeout();
        router.route().handler(SessionHandlerImpl
                .create(new MongoSessionStoreImpl(env.getAppName(), vertx))
                .setNagHttps(false)
                .setSessionTimeout(sessionTimeout));

        // Handle CSRF token, overtime = session timeout
        router.route().handler(CSRFHandler
                .create(ConfigManager.INSTANCE.getString("app.hmackey"))
                .setTimeout(sessionTimeout));

        // Handle body
        router.route().handler(BodyHandler.create());

        // Handle login
        if (!Helper.isJUnitTest()) {
            router.route().handler(AuthHandler.create());
        }

        // use http header 'x-response-time' to indicate the server response time
        router.route().handler(ResponseTimeHandler.create());

        // Route the Web request
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.routeProcessAPI(router);
        dispatcher.routeDataAPI(router);
        dispatcher.routeView(router);

        // Start the web server
        server.requestHandler(router::accept).listen(env.getAppPort());
        logger.info(String.format("Running on http://%s:%s/", "127.0.0.1", env.getAppPort()));
    }

    public void generate() {
        Environment env = Environment.instance();
        CacheManager.INSTANCE.setUp(env.getAppName());
        String pkg = Environment.instance().getPackages() + ".entity";
        new Generator(pkg).generate();
        logger.info("Generation done");
    }
}
