package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModUser;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.exception.LightException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.security.Crypto;
import org.bson.Document;

/**
 * User
 * Created by lilin on 2016/11/15.
 */
public class User {

    public static void verify(Context handler) throws LightException {

//        handler.params.setCondition(new Document("id", handler.params.getId()));
//        handler.params.setId(null);
//        ModUser user = new DataRider(ModUser.class).get(handler);
//
//        if (user == null) {
//            throw new BadRequestException("User Not Exist.");
//        }
//
//        String password = handler.params.getString("password");
//        String hmackey = handler.params.getString("hmackey");
//
//        if (hmackey == null) {
//            hmackey = ConfigManager.INSTANCE.getString("app.hmackey");
//        }
//
//        if (!user.getPassword().equals(Crypto.sha256(password, hmackey))) {
//            throw new BadRequestException("Password Not Correct.");
//        }
//
//        handler.session().put(Constant.SK_USER, user);
//        handler.session().put(Constant.SK_CODE, handler.getCode());
//        handler.session().put(Constant.SK_DOMAIN, handler.getDomain());
    }

}
