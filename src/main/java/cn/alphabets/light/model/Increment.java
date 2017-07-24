package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.db.mongo.Model;

/**
 * Increment
 * Created by luohao on 2016/12/26.
 */
public class Increment {
    public static long increase(String domain, String code, String type) {
        if (Environment.instance().isRDB()) {
            return new cn.alphabets.light.db.mysql.Model(domain, code).increase(type);
        }

        Model model = new Model(domain, code, Constant.SYSTEM_DB_COUNTER);
        return model.increase(type);
    }

    public static long increase(String type) {

        String domain = Environment.instance().getAppName();
        String code  = Constant.DEFAULT_TENANT;

        return increase(domain, code, type);
    }
}
