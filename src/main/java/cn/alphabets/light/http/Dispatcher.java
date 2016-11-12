package cn.alphabets.light.http;

import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.I18N;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.http.exception.MethodNotFoundException;
import cn.alphabets.light.http.exception.ProcessingException;
import cn.alphabets.light.entity.Board;
import cn.alphabets.light.entity.Route;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static ConcurrentHashMap<String, Method> methodMap;

    private final List<Board> boards;
    private final List<Route> routes;
    private ConfigManager conf;

    public Dispatcher() {
        this.boards = CacheManager.INSTANCE.getBoards();
        this.routes = CacheManager.INSTANCE.getRoutes();
        this.conf = ConfigManager.INSTANCE;
    }

    /*
    处理型API
     */
    public void routeProcessAPI(Router router) {
        this.setup();

        this.boards.forEach(board -> {
            if (board.getKind() == 1) {

                io.vertx.ext.web.Route r = router.route(board.getApi());
                r.blockingHandler(ctx -> {
                    Method method = null;//resolve(board);
                    Context context = new Context(ctx);
                    if (method == null) {
                        throw new MethodNotFoundException("Dispatch Method Not Found , Board Info : " + board.toJson());
                    }
                    try {
                        method.invoke(method.getDeclaringClass().newInstance(), context);
                    } catch (Exception e) {
                        throw new ProcessingException(e);
                    }
                }, false);
                r.failureHandler(getDefaultDispatcherFailureHandler());
            }
        });
    }


    /*
    处理型API
     */
    public void routeDataAPI(Router router) {
    }

    /**
     * Route the screen path
     *
     * @param router vert.x router
     */
    public void routeView(Router router) {

        this.routes.forEach(route -> {

            io.vertx.ext.web.Route r = router.route(route.getUrl());
            r.failureHandler(getDefaultDispatcherFailureHandler());
            r.blockingHandler(ctx -> {

                final Context context = new Context(ctx);

                // Try lookup controllers class
                Method method = resolve(route);
                final Object customized = invoke(method, context);

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

    private Object invoke(Method method, Context handler) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(method.getDeclaringClass().newInstance(), handler);
        } catch (Exception e) {
            throw new ProcessingException(e);
        }
    }

    private void setup() {

        if (methodMap == null) {
            methodMap = new ConcurrentHashMap<>();
            Reflections reflections = new Reflections(Environment.instance().getPackages() + ".controller", new SubTypesScanner(false));
            Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);
            allClasses.forEach(aClass -> {
                Set<Method> getters = ReflectionUtils.getAllMethods(aClass);
                getters.forEach(method -> {
                    String key = aClass.getSimpleName() + "-" + method.getName();
                    methodMap.put(key, method);
                });
            });
        }
    }

    private Method resolve(Board board) {
        String className = StringUtils.capitalize(board.getClass_());
        String methodKey = className + "-" + board.getAction();
        return methodMap.get(methodKey);
    }

    private Method resolve(Route route) {
        String className = StringUtils.capitalize(route.getClass_());
        String methodKey = className + "-" + route.getAction();
        return methodMap.get(methodKey);
    }

    protected Handler<RoutingContext> getDefaultDispatcherFailureHandler() {
        return ctx -> {
            Throwable error = ctx.failure();
            log.error("Request Error:", error);

            HttpResponseStatus status = NOT_FOUND;
            if (error instanceof MethodNotFoundException) {
                status = NOT_FOUND;
            } else if (error instanceof ProcessingException) {
                status = INTERNAL_SERVER_ERROR;
            } else if (error instanceof IllegalStateException) {
                status = INTERNAL_SERVER_ERROR;
            } else {
                status = INTERNAL_SERVER_ERROR;
            }

            //请求已经结束
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
