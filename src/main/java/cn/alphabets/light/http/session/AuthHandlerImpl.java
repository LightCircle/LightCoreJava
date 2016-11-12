package cn.alphabets.light.http.session;

import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.entity.User;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;

/**
 * AuthHandlerImpl
 * Created by luohao on 2016/10/28.
 */
public class AuthHandlerImpl implements Handler<RoutingContext> {
    private Pattern ignore;
    HttpResponseStatus errorStatus = UNAUTHORIZED;

    public AuthHandlerImpl() {
        List<String> paths = ConfigManager.INSTANCE.getIgnoreAuth();
        if (CollectionUtils.isNotEmpty(paths)) {
            String regex = StringUtils.join(paths, "|");
            ignore = Pattern.compile(regex.replace("*", ".*"));
        }
    }

    @Override
    public void handle(RoutingContext ctx) {

        String path = ctx.request().path();


        //设定规则，切当前访问路径在规则列表，则不检查user
        if (ignore != null && ignore.matcher(path).matches()) {
            ctx.next();
            return;
        }

        Context context = new Context(ctx);
        User user = (User)context.user();

        //没有登录
        if (user == null) {
            ctx.response().setStatusCode(errorStatus.code()).end(errorStatus.reasonPhrase());
        } else {
            ctx.next();
        }


    }
}
