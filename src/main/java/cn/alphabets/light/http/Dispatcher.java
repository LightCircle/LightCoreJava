package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.I18N;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModRoute;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.exception.LightException;
import cn.alphabets.light.http.exception.MethodNotFoundException;
import cn.alphabets.light.http.exception.ProcessingException;
import cn.alphabets.light.model.Error;
import cn.alphabets.light.model.datarider.DBParams;
import cn.alphabets.light.model.datarider.DataRider;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;


/**
 * Dispatcher
 * Created by luohao on 16/10/22.
 */
public class Dispatcher {

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private final List<ModBoard> boards;
    private final List<ModRoute> routes;
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

                Object data;

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
                } catch (InvocationTargetException e) {
                    throw new ProcessingException(e.getTargetException());
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new ProcessingException(e);
                }
                //if controller method return type is void ,do nothing
                if (!method.getReturnType().equals(Void.TYPE)) {
                    new Result(data).send(ctx);
                }
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
            if (!Constant.KIND_BOARD_SYSTEM_DATA_API.equals(board.getKind()) && !Constant.KIND_BOARD_DATA_API.equals(board.getKind())) {
                return;
            }

            io.vertx.ext.web.Route r = router.route(board.getApi());
            r.failureHandler(getFailureHandler());
            r.blockingHandler(ctx -> {

                Object data;
                Context handler = new Context(ctx);

                String className = board.getClass_(), actionName = board.getAction();
                if (className != null && actionName != null) {

                    // Try lookup controller class
                    Method method = resolve(className, actionName);

                    // Try lookup light model class
                    if (method == null) {
                        method = resolve(className, actionName, Constant.DEFAULT_PACKAGE_NAME + ".model");
                    }

                    if (method != null) {
                        try {
                            data = method.invoke(method.getDeclaringClass().newInstance(), handler);
                        } catch (InvocationTargetException e) {
                            throw new ProcessingException(e.getTargetException());
                        } catch (IllegalAccessException | InstantiationException e) {
                            throw new ProcessingException(e);
                        }

                        //if controller method return type is void ,do nothing
                        if (!method.getReturnType().equals(Void.TYPE)) {
                            new Result(data).send(ctx);
                        }
                        return;
                    }
                }

                // Try lookup rider class
                data = DataRider.ride(board).call(new DBParams(handler, true));

                new Result(data).send(ctx);
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

                final Context handler = new Context(ctx);

                // Try lookup controller class
                Object customized;
                try {
                    customized = invoke(route, handler);
                } catch (InvocationTargetException e) {
                    throw new ProcessingException(e.getTargetException());
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new ProcessingException(e);
                }

                // Define multiple template functions
                Helper.StringFunction i = new Helper.StringFunction("i", (args) -> {
                    String lang = handler.getLang(), key = (String) args.get(0);
                    return I18N.i(lang, key);
                });

                Helper.MapFunction catalog = new Helper.MapFunction("catalog", (args) -> {
                    String lang = handler.getLang(), type = (String) args.get(0);
                    return I18N.catalog(lang, type);
                });

                Helper.StringFunction dynamic = new Helper.StringFunction("dynamic", (args) -> {
                    String url = (String) args.get(0);
                    String stamp = this.conf.getString("app.stamp");
                    String prefix = this.conf.getString("app.static");
                    String connector = url.contains("?") ? "&" : "?";
                    return String.format("%s%s%sstamp=%s", prefix, url, connector, stamp);
                });

                // Create a template parameter
                Map<String, Object> model = new ConcurrentHashMap<String, Object>() {{
                    put("req", handler.req());
                    put("handler", handler);
                    put("conf", Environment.instance());
                    put("csrftoken", ctx.get(CSRFHandler.DEFAULT_HEADER_NAME));
                }};
                if (handler.user() != null) {
                    model.put("user", handler.user());
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
                String name = "views/" + template;
                String html = Helper.loadTemplate(name, model, Arrays.asList(dynamic, i, catalog));
                ctx.response().putHeader(CONTENT_TYPE, TEXT_HTML).end(html);

            }, false);
        });
    }

    private Object invoke(ModRoute route, Context handler) throws IllegalAccessException, InstantiationException, InvocationTargetException {

        String className = route.getClass_(), actionName = route.getAction();
        if (className == null || actionName == null) {
            return null;
        }

        Method method = resolve(className, actionName);
        if (method == null) {
            return null;
        }

        return method.invoke(method.getDeclaringClass().newInstance(), handler);
    }

    private Method resolve(String className, String methodName) {
        return this.resolve(className, methodName, Environment.instance().getPackages() + ".controller");
    }

    private Method resolve(String className, String methodName, String packages) {

        String fullName = packages + "." + WordUtils.capitalize(className);
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
            if (ctx.response().ended()) {
                return;
            }

            Throwable error = ctx.failure();
            if (error == null && !ctx.response().ended()) {
                ctx.response().end();
            }

            logger.error("Error occurred : ", error);


            processException(ctx, error);

        };
    }

    private LinkedHashMap<Class, BiConsumer<RoutingContext, Throwable>> errorProcessMap = new LinkedHashMap<Class, BiConsumer<RoutingContext, Throwable>>() {{

        put(MethodNotFoundException.class, (ctx, e) -> {
            Error error = new Error("100", e.getMessage());
            ctx.response()
                    .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                    .setStatusCode(Constant.GLOBAL_ERROR_STATUS_CODE)
                    .end(new Result(error).json());
        });
        put(IllegalStateException.class, (ctx, e) -> {

            Error error = new Error("101", e.getMessage());
            ctx.response()
                    .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                    .setStatusCode(Constant.GLOBAL_ERROR_STATUS_CODE)
                    .end(new Result(error).json());
        });

        put(LightException.class, (ctx, e) -> {
            Result result = new Result(e);
            ctx.response()
                    .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                    .setStatusCode(Constant.GLOBAL_ERROR_STATUS_CODE)
                    .end(result.json());
        });
        put(DataRiderException.class, (ctx, e) -> {
            Error error = new Error("103", e.getMessage());
            ctx.response()
                    .setStatusCode(Constant.GLOBAL_ERROR_STATUS_CODE)
                    .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(new Result(error).json());
        });
        put(ProcessingException.class, (ctx, e) -> {

            Throwable cause = e.getCause();
            if (cause == null) {
                errorProcessMap.get(Throwable.class).accept(ctx, null);
                return;
            }
            processException(ctx, cause);
        });
        //default
        put(Throwable.class, (ctx, e) -> {
            Error error = new Error("000", e != null ? e.getMessage() : "Unknown error.");
            ctx.response()
                    .setStatusCode(Constant.GLOBAL_ERROR_STATUS_CODE)
                    .putHeader(CONTENT_TYPE, "application/json; charset=utf-8")
                    .end(new Result(error).json());
        });
    }};

    private void processException(RoutingContext ctx, Throwable error) {

        errorProcessMap.keySet().forEach(aClass -> {
            if (aClass.isAssignableFrom(error.getClass())) {
                if (ctx.response().ended()) {
                    return;
                }
                errorProcessMap.get(aClass).accept(ctx, error);
            }
        });

    }

}