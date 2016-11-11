package cn.alphabets.light.http;

import cn.alphabets.light.AppOptions;
import cn.alphabets.light.Config;
import cn.alphabets.light.Helper;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.http.exception.MethodNotFoundException;
import cn.alphabets.light.http.exception.ProcessingException;
import cn.alphabets.light.model.Board;
import cn.alphabets.light.model.Route;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;


/**
 * Created by luohao on 16/10/22.
 */
public class Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static ConcurrentHashMap<String, Method> methodMap;
    private boolean isDev;

    /*
    处理型API
     */
    public void routeProcessAPI(DBConnection db, Router router, AppOptions options) {
        this.setup(options);
        CacheManager.INSTANCE.getBoards().forEach(board -> {
            if (board.getKind() == 1) {
                router.route(board.getApi())
                        .blockingHandler(ctx -> {
                            Method method = null;//resolve(board);
                            Context context = new Context(ctx, db);
                            if (method == null) {
                                throw new MethodNotFoundException("Dispatch Method Not Found , Board Info : " + board.toJson());
                            }
                            try {
                                method.invoke(method.getDeclaringClass().newInstance(), context);
                            } catch (Exception e) {
                                throw new ProcessingException(e);
                            }
                        }, false)
                        .failureHandler(getDefaultDispatcherFailureHandler());
            }
        });
    }


    /*
    处理型API
     */
    public void routeDataAPI(DBConnection db, Router router, AppOptions options) {
    }

    /*
    画面路径
     */
    public void routeView(DBConnection db, Router router, AppOptions options) {
        this.setup(options);
        CacheManager.INSTANCE.getRoutes().forEach(route -> {

            router.route(route.getUrl())
                    .blockingHandler(ctx -> {
                        Context context = new Context(ctx, db);

                        //如果定义了action，先调用action方法
                        Method method = resolve(route);
                        if (method != null) {
                            try {
                                method.invoke(method.getDeclaringClass().newInstance(), context);
                            } catch (Exception e) {
                                throw new ProcessingException(e);
                            }
                        }

                        //有的设置是index.html 有的设置直接是index 所以需要补全
                        String fileName = route.getTemplate();
                        if (!StringUtils.endsWith(fileName, ".html")) {
                            fileName += ".html";
                        }

                        // TODO: 实现 dynamic 和 i 方法
                        Helper.TemplateFunction dynamic = new Helper.TemplateFunction("dynamic",
                                (x) -> "static" + x.get(0)
                        );
                        Helper.TemplateFunction i = new Helper.TemplateFunction("i", (x) -> x.get(0) + " : i");

                        // TODO: 设定变量
                        Map<String, Object> model = new ConcurrentHashMap<String, Object>() {{
                            put("req", Boolean.TRUE);
                            put("handler", context);
                            put("user", "");
                            put("conf", Config.instance());
                            put("environ", "");
                        }};

                        //然后执行渲染
                        String name = "view/" + fileName;
                        String html = Helper.loadTemplate(name, model, Arrays.asList(dynamic, i));
                        ctx.response().putHeader(CONTENT_TYPE, TEXT_HTML).end(html);
                    }, false)
                    .failureHandler(getDefaultDispatcherFailureHandler());
        });
    }

    private void setup(AppOptions options) {
        isDev = options.isDev();
        if (methodMap == null) {
            methodMap = new ConcurrentHashMap<>();
            Reflections reflections = new Reflections(options.getPackageNmae() + ".controller", new SubTypesScanner(false));
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

            if (isDev) {
                StringWriter stringWriter = new StringWriter();
                error.printStackTrace(new PrintWriter(stringWriter));
                ctx.response().setStatusCode(status.code()).end(stringWriter.toString());
            } else {
                ctx.response().setStatusCode(status.code()).end(status.reasonPhrase());
            }
        };
    }

}
