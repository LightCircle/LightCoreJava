package cn.alphabets.light.http;

import cn.alphabets.light.AppOptions;
import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.exception.DispatcheException;
import cn.alphabets.light.model.ModBoard;
import cn.alphabets.light.model.ModRoute;
import com.mongodb.Block;
import com.sun.tools.javac.util.Assert;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.TemplateEngine;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static cn.alphabets.light.model.ModBase.fromDoc;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;
import static io.vertx.core.http.HttpHeaders.TEXT_HTML;


/**
 * Created by luohao on 16/10/22.
 */
public class Dispatcher {

    final Logger log = LoggerFactory.getLogger(Dispatcher.class);

    private static ConcurrentHashMap<String, Method> methodMap;
    private boolean isDev;
    private static TemplateEngine templateEngine;

    /*
    处理型API
     */
    public void routeProcessAPI(DBConnection db, Router router, AppOptions options) {
        this.setup(options);
        db.getCollection("light.boards")
                .find(Document.parse("{kind:1,valid:1}"))
                .forEach((Block<? super Document>) document -> {

                    ModBoard board = fromDoc(document, ModBoard.class);
                    Assert.checkNonNull(board);

                    router.route(board.getApi())
                            .blockingHandler(ctx -> {
                                Method method = resolve(board);
                                Context context = new Context(ctx, db);
                                if (method == null) {
                                    throw new DispatcheException("can not dispatch");
                                }
                                try {
                                    method.invoke(method.getDeclaringClass().newInstance(), context);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }, false)
                            .failureHandler(getDefaultDispatcherFailureHandler());

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
        db.getCollection("light.routes").find(Document.parse("{valid:1}")).forEach((Block<? super Document>) document -> {

            ModRoute route = fromDoc(document, ModRoute.class);

            router.route(route.getUrl())
                    .blockingHandler(ctx -> {
                        Context context = new Context(ctx, db);

                        //如果定义了action，先调用action方法
                        Method method = resolve(route);
                        if (method != null) {
                            try {
                                method.invoke(method.getDeclaringClass().newInstance(), context);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }

                        //然后执行渲染
                        templateEngine.render(ctx, "view/" + route.getTemplate(), ar -> {
                            if (ar.succeeded()) {
                                ctx.response().putHeader(CONTENT_TYPE, TEXT_HTML).end(ar.result());
                            } else {
                                throw new RuntimeException(ar.cause());
                            }
                        });
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

        if (templateEngine == null) {
            templateEngine = ThymeleafTemplateEngine.create();
        }
    }

    private Method resolve(ModBoard board) {
        String className = StringUtils.capitalize(board.getClass_());
        String methodKey = className + "-" + board.getAction();
        return methodMap.get(methodKey);
    }

    private Method resolve(ModRoute route) {
        String className = StringUtils.capitalize(route.getClass_());
        String methodKey = className + "-" + route.getAction();
        return methodMap.get(methodKey);
    }

    protected Handler<RoutingContext> getDefaultDispatcherFailureHandler() {
        return ctx -> {
            Throwable error = ctx.failure();
            error.printStackTrace();

            HttpResponseStatus status = NOT_FOUND;
            if (error instanceof DispatcheException) {
                status = NOT_FOUND;
            } else if (error instanceof RuntimeException) {
                status = INTERNAL_SERVER_ERROR;
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
