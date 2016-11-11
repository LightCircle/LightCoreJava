package cn.alphabets.light.config;

import cn.alphabets.light.Constant;
import cn.alphabets.light.cache.CacheManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Config
 * Created by luohao on 2016/10/28.
 */
public enum ConfigManager {

    INSTANCE;

    private Map<String, Object> map;

    public ConfigManager setUp() {
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }

        CacheManager.INSTANCE.getConfiguration().forEach(document -> {
            String key = document.getType() + "." + document.getKey();
            map.put(key, document.getValue());
        });

        return this;
    }

    public List<String> getIgnoreTimeout() {
        return this.getArray(Constant.CFK_REQUEST_IGNORE_TIMEOUT);
    }

    public Long getAppTimeout() {
        return this.getLong(Constant.CFK_REQUEST_TIMEOUT);
    }

    public Long getAppSessionTimeout() {
        return this.getLong(Constant.CFK_SESSION_TIMEOUT);
    }

    public List<String> getIgnoreAuth() {
        return this.getArray(Constant.CFK_IGNORE_AUTH);
    }

    public String getString(String key) {
        return String.valueOf(map.get(key));
    }

    public long getLong(String key) {
        return Long.valueOf(this.getString(key));
    }

    public long getInt(String key) {
        return Integer.valueOf(this.getString(key));
    }

    public boolean getBoolean(String key) {
        return Boolean.valueOf(this.getString(key));
    }

    @SuppressWarnings("unchecked")
    public List<String> getArray(String key) {
        return (List<String>) map.get(key);
    }

}
