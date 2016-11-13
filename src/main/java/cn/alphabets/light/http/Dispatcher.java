package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.I18N;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.Board;
import cn.alphabets.light.entity.Route;
import cn.alphabets.light.http.exception.MethodNotFoundException;
import cn.alphabets.light.http.exception.ProcessingException;
import cn.alphabets.light.http.exception.RenderException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;


/**
 * Dispatcher
 * Created by luohao on 16/10/22.
 */
public class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final List<Board> boards;
    private final List<Route> routes;
    private final Map<String, Method> methods;
    private final ConfigManager conf;

    public Dispatcher() {
        this.boards = CacheManager.INSTANCE.getBoards();
        this.routes = CacheManager.INSTANCE.getRoutes();
        this.conf = ConfigManager.INSTANCE;
        this.methods = new ConcurrentHashMap<>();
    }

    /**
     * Route the process type api
     *
     * @param router vert.x router
     */
    public void routeProcessAPI(Router router) {

        this.boards.forEach(board -> {
            if (!Constant.KIND_BOARD_PROCESS_API.equals(board.getKind())) {
                return;
            }

            io.vertx.ext.web.Route r = router.route(board.getApi());
            r.failureHandler(getFailureHandler());
            r.blockingHandler(ctx -> {

                Object data = null;

                String className = board.getClass_(), actionName = board.getAction();
                if (className == null || actionName == null) {
                    throw new MethodNotFoundException("Dispatch method not found.");
                }

                Method method = resolve(className, actionName);
                if (method == null) {
                    throw new MethodNotFoundException("Dispatch method not found.");
                }

                try {
                    data = method.invoke(method.getDeclaringClass().newInstance(), new Context(ctx));
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new ProcessingException(e);
                }

                new Result(data).send(ctx);
            }, false);
        });
    }

    /**
     * Route the data type api
     *
     * @param router vert.x router
     */
    public void routeDataAPI(Router router) {
        this.boards.forEach(board -> {
            if (!Constant.KIND_BOARD_DATA_API.equals(board.getKind())) {
                return;
            }

            io.vertx.ext.web.Route r = router.route(board.getApi());
            r.failureHandler(getFailureHandler());
            r.blockingHandler(ctx -> {

                Object data = null;

                // Try lookup controller class
                String className = board.getClass_(), actionName = board.getAction();
                if (className != null && actionName != null) {

                    Method method = resolve(className, actionName);
                    if (method != null) {
                        try {
                            data = method.invoke(method.getDeclaringClass().newInstance(), new Context(ctx));
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                            throw new ProcessingException(e);
                        }

                        new Result(data).send(ctx);
                    }
                }

                // TODO: Try lookup light model class

                // TODO: Try lookup data rider class
            }, false);
        });
    }

    /**
     * Route the screen path
     *
     * @param router vert.x router
     */
    public void routeView(Router router) {

        this.routes.forEach(route -> {

            io.vertx.ext.web.Route r = router.route(route.getUrl());
            r.failureHandler(getFailureHandler());
            r.blockingHandler(ctx -> {

                final Context context = new Context(ctx);

                // Try lookup controller class
                final Object customized = invoke(route, context);

                // Define multiple template functions
                Helper.TemplateFunction i = new Helper.TemplateFunction("i", (args) -> I18N.i((String) args.get(0)));
                Helper.TemplateFunction dynamic = new Helper.TemplateFunction("dynamic", (args) -> {
                    String url = (String) args.get(0);
                    String stamp = this.conf.getString("app.stamp");
                    String prefix = this.conf.getString("app.static");
                    String connector = url.contains("?") ? "&" : "?";
                    return String.format("%s%s%sstamp=%s", prefix, url, connector, stamp);
                });

                // Create a template parameter
                Map<String, Object> model = new ConcurrentHashMap<String, Object>() {{
                    put("req", context.req());
                    put("handler", context);
                    put("conf", Environment.instance());

                }};
                if (context.user() != null) {
                    model.put("user", context.user());
                }
                if (customized != null) {
                    model.put("data", customized);
                }

                // Some settings are 'index.html' some settings directly 'index'. So need to fill
                String template = route.getTemplate();
                if (!StringUtils.endsWith(template, ".html")) {
                    template += ".html";
                }

                // Execute the rendering
                String name = "view/" + template;
                String html = Helper.loadTemplate(name, model, Arrays.asList(dynamic, i));
                ctx.response().putHeader(CONTENT_TYPE, TEXT_HTML).end(html);

            }, false);
        });
    }

    private Object invoke(Route route, Context handler) {

        String className = route.getClass_(), actionName = route.getAction();
        if (className == null || actionName == null) {
            return null;
        }

        Method method = resolve(className, actionName);
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(method.getDeclaringClass().newInstance(), handler);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RenderException(e);
        }
    }

    private Method resolve(String className, String methodName) {

        String fullName = Environment.instance().getPackages() + ".controller." + WordUtils.capitalize(className);
        String key = String.format("%s#%s", fullName, methodName);

        if (!methods.containsKey(key)) {
            try {
                this.methods.put(key, Class.forName(fullName).getMethod(methodName, Context.class));
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                return null;
            }
        }

        return methods.get(key);
    }

    private Handler<RoutingContext> getFailureHandler() {
        return ctx -> {
            Throwable error = ctx.failure();
            logger.error(error);

            HttpResponseStatus status = INTERNAL_SERVER_ERROR;
            if (error instanceof MethodNotFoundException) {
                status = NOT_FOUND;
            }

            if (error instanceof ProcessingException) {
                status = INTERNAL_SERVER_ERROR;
            }

            if (error instanceof IllegalStateException) {
                status = INTERNAL_SERVER_ERROR;
            }

            if (ctx.response().ended()) {
                return;
            }

            if (Environment.instance().app.isDev()) {
                StringWriter stringWriter = new StringWriter();
                error.printStackTrace(new PrintWriter(stringWriter));
                ctx.response().setStatusCode(status.code()).end(stringWriter.toString());
            } else {
                ctx.response().setStatusCode(status.code()).end(status.reasonPhrase());
            }
        };
    }
}
