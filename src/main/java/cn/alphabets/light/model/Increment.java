package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.db.mongo.Model;

/**
 * Created by luohao on 2016/12/26.
 */
public class Increment {
    public static long increase(String domain, String code, String type) {
        Model model = new Model(domain, code, Constant.SYSTEM_DB_COUNTER);
        return model.increase(type);
    }

    public static long increase(String type) {
        Model model = new Model(Environment.instance().getAppName(), Constant.DEFAULT_TENANT, Constant.SYSTEM_DB_COUNTER);
        return model.increase(type);
    }
}
