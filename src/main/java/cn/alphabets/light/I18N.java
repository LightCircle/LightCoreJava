package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModI18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * I18N
 * Created by lilin on 2016/11/13.
 */
public class I18N {

    private static final Logger logger = LogManager.getLogger(I18N.class);

    public static String i(String lang, String message) {

        int index = message.indexOf(".");
        if (index < 0) {
            logger.warn("There is no type specified in the message prefix");
            return message;
        }

        String type = message.substring(0, index);
        String key = message.substring(index + 1);

        List<ModI18n> i18ns = CacheManager.INSTANCE.getI18ns();
        for (ModI18n i18n : i18ns) {
            if (type.equals(i18n.getType()) && key.equals(i18n.getKey())) {
                return (String) ((Map) i18n.getLang()).get(lang);
            }
        }

        return message;
    }
}
