package cn.alphabets.light.http;

import cn.alphabets.light.Constant;
import cn.alphabets.light.config.ConfigManager;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by luohao on 2016/10/28.
 */
public class TimeoutHandlerImpl implements TimeoutHandler {

    final Logger log = LoggerFactory.getLogger(TimeoutHandler.class);

    private long timeout;
    private int errorCode;
    private Pattern ignore;


    public TimeoutHandlerImpl(long timeout) {
        this(timeout, DEFAULT_ERRORCODE);
    }


    public TimeoutHandlerImpl() {
        this(DEFAULT_TIMEOUT, DEFAULT_ERRORCODE);
    }

    public TimeoutHandlerImpl(long timeout, int errorCode) {
        this.timeout = timeout;
        this.errorCode = errorCode;

        if (timeout <= 0) {
            throw new IllegalArgumentException("time out can not be less than 0");
        }

        List<String> paths = ConfigManager.getInstance().getArray(Constant.CFK_REQUEST_IGNORE_TIMEOUT);
        if (CollectionUtils.isNotEmpty(paths)) {
            String regex = StringUtils.join(ConfigManager.getInstance().getArray(Constant.CFK_REQUEST_IGNORE_TIMEOUT).toArray(), "|");
            ignore = Pattern.compile(regex.replace("*", ".*"));
        }


    }


    @Override
    public void handle(RoutingContext ctx) {

        String path = ctx.request().path();

        if (ignore != null && !ignore.matcher(path).matches()) {
            // We send a error response after timeout
            long tid = ctx.vertx().setTimer(timeout, t -> {
                log.error("Request Timeout : " + path);
                ctx.fail(errorCode);
            });
            ctx.addBodyEndHandler(v -> ctx.vertx().cancelTimer(tid));
        }

        ctx.next();
    }
}