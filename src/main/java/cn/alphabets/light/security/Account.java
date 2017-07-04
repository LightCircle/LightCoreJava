package cn.alphabets.light.security;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModUser;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.session.MongoSessionStoreImpl;
import cn.alphabets.light.http.session.SessionHandlerImpl;
import cn.alphabets.light.model.Singular;
import cn.alphabets.light.model.datarider.Rider;

/**
 * Account
 * Created by lilin on 2017/7/4.
 */
public class Account {

    public static Singular<ModUser> simpleLogin(Context handler) throws Exception {

        handler.setDomain(Environment.instance().getAppName());
        if (handler.params.get("code") == null) {
            handler.setCode(Constant.DEFAULT_TENANT);
        } else {
            handler.setCode(handler.params.get("code"));
        }

        Singular<ModUser> user = Rider.get(handler, ModUser.class);
        if (user.item == null) {
            throw new BadRequestException("User Not Exist.");
        }

        String password = handler.params.get("password");
        String hmackey = handler.params.get("hmackey");
        if (hmackey == null) {
            hmackey = ConfigManager.INSTANCE.getString("app.hmackey");
        }

        if (!user.item.getPassword().equals(Crypto.sha256(password, hmackey))) {
            throw new BadRequestException("Password Not Correct.");
        }

        // 重新生成session
        Environment env = Environment.instance();
        long sessionTimeout = 1000L * 60 * 60 * ConfigManager.INSTANCE.getAppSessionTimeout();
        handler.ctx().currentRoute().handler(SessionHandlerImpl
                .create(new MongoSessionStoreImpl(env.getAppName(), handler.ctx().vertx()))
                .setNagHttps(false)
                .setSessionTimeout(sessionTimeout));

        user.item.setPassword("");
        handler.session().put(Constant.SK_USER, user.item);
        handler.session().put(Constant.SK_DOMAIN, handler.domain());
        handler.session().put(Constant.SK_CODE, handler.getCode());

        // TODO: Authority logic
        return user;
    }

}
