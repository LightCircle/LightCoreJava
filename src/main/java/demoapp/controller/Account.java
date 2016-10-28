package demoapp.controller;

import cn.alphabets.light.http.Context;
import cn.alphabets.light.model.ModUser;
import io.vertx.ext.web.Session;

import java.util.Date;

/**
 * Created by luohao on 16/10/22.
 */
public class Account {
    public void change_password(Context ctx) {
        ModUser user = ctx.user(ModUser.class);
        if (user == null) {
            user = new ModUser();
            user.setId("test");
            user.setName("罗浩");
            user.setCreateAt(new Date());
            ctx.setUser(user);
        }

        ctx.res().end(user.getName());
    }

    public void register(Context ctx) {
        Session session = ctx.session();
        Integer cnt = session.get("hitcount");
        cnt = (cnt == null ? 0 : cnt) + 1;
        session.put("hitcount", cnt);
        ctx.res().end(cnt + "!");
    }

    public void logout(Context ctx) {
        int a = 10 / 0;
        ctx.res().end("!");
    }
}
