package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModI18n;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

        for (ModI18n i18n : CacheManager.INSTANCE.getI18ns()) {
            if (type.equals(i18n.getType()) && key.equals(i18n.getKey())) {
                return (String) ((Map) i18n.getLang()).get(lang);
            }
        }

        return message;
    }

    public static Map<String, String> catalog(String lang, String type) {

        Map<String, String> catalog = new ConcurrentHashMap<>();

        for (ModI18n i18n : CacheManager.INSTANCE.getI18ns()) {
            if (type == null) {
                catalog.put(i18n.getKey(), (String) ((Map) i18n.getLang()).get(lang));
            } else {
                if (type.equals(i18n.getType())) {
                    catalog.put(i18n.getKey(), (String) ((Map) i18n.getLang()).get(lang));
                }
            }
        }

        return catalog;
    }
}
