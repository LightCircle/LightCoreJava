package demoapp.controller;

import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.http.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Session;

/**
 * Created by luohao on 16/10/22.
 */
public class Account {
    public void change_password(Context ctx) {
        Session session = ctx.session();
        Integer cnt = session.get("hitcount");
        cnt = (cnt == null ? 0 : cnt) + 1;
        session.put("hitcount", cnt);
        ctx.res().end(cnt + "!");
    }
}
