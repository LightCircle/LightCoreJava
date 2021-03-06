package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Helper;
import cn.alphabets.light.config.ConfigManager;
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
public interface AuthHandler extends Handler<RoutingContext> {

    static AuthHandler create() {
        return new AuthHandlerImpl();
    }

    class AuthHandlerImpl implements AuthHandler {

        private Pattern ignore;
        private HttpResponseStatus errorStatus = UNAUTHORIZED;

        AuthHandlerImpl() {
            List<String> paths = ConfigManager.INSTANCE.getIgnoreAuth();
            if (CollectionUtils.isNotEmpty(paths)) {
                String regex = StringUtils.join(paths, "|");
                ignore = Pattern.compile(regex.replaceAll("(?<!\\.)\\*", ".*"));
            }
        }

        @Override
        public void handle(RoutingContext ctx) {

            String path = ctx.request().path();

            // The current access path in the rules list, do not check the user
            if (ignore != null && ignore.matcher(path).matches()) {
                ctx.next();
                return;
            }

            // No login
            if (ctx.session().get(Constant.SK_USER) == null) {

                if (Helper.isBrowser(ctx.request())) {
                    Result.redirect(ctx, ConfigManager.INSTANCE.getString("app.home"));
                } else {
                    // TODO: move err to Result class
                    ctx.response().setStatusCode(errorStatus.code()).end(errorStatus.reasonPhrase());
                }
            } else {
                ctx.next();
            }
        }
    }
}