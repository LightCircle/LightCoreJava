package cn.alphabets.light.config;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.db.mongo.DBConnection;
import com.mongodb.Block;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by luohao on 2016/10/28.
 */
public enum ConfigManagerImpl implements ConfigManager {


    INSTANCE;

    /**
     * 示例数据
     * {
     * "_id" : ObjectId("555300b548d0703220aae81c"),
     * "type" : "db",
     * "key" : "port",
     * "value" : "57017",
     * "valueType" : "number",
     * "displayType" : 0,
     * "description" : "数据库端口",
     * "createAt" : ISODate("2015-05-13T07:43:49.477Z"),
     * "updateAt" : ISODate("2015-07-01T06:16:02.903Z"),
     * "valid" : 1,
     * "createBy" : "000000000000000000000000",
     * "updateBy" : "55719d694a7c9bc10d714ad7"
     * }
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> map;

    @Override
    public void setUp(DBConnection db) {
        if (map == null) {
            map = new ConcurrentHashMap();
        }

        CacheManager.INSTANCE.getConfiguration().forEach(document -> {
            String type = document.getType();
            String key = document.getKey();
            String valueType = document.getValueType();

            ConcurrentHashMap<String, Object> typeData = map.get(type);
            if (typeData == null) {
                typeData = new ConcurrentHashMap<String, Object>();
            }
            map.put(type, typeData);

            switch (valueType) {
                case "string":
                    typeData.put(key, document.getValue());
                    break;
                case "number":
                    typeData.put(key, Long.parseLong(String.valueOf(document.getValue())));
                    break;
                case "array":
                    typeData.put(key, (List<String>) document.getValue());
                    break;
                case "boolean":
                    typeData.put(key, Boolean.valueOf(String.valueOf(document.getValue())));
                    break;
            }

        });
    }

    @Override
    public String getString(String group, String key) {
        return (String) map.get(group).get(key);
    }

    @Override
    public long getLong(String group, String key) {
        return (long) map.get(group).get(key);

    }

    @Override
    public boolean getBoolean(String group, String key) {
        return (boolean) map.get(group).get(key);
    }

    @Override
    public List<String> getArray(String group, String key) {
        return (List<String>) map.get(group).get(key);
    }

    @Override
    public String getString(String[] groupAndkey) {
        return getString(groupAndkey[0], groupAndkey[1]);
    }

    @Override
    public long getLong(String[] groupAndkey) {
        return getLong(groupAndkey[0], groupAndkey[1]);
    }

    @Override
    public boolean getBoolean(String[] groupAndkey) {
        return getBoolean(groupAndkey[0], groupAndkey[1]);
    }

    @Override
    public List<String> getArray(String[] groupAndkey) {
        return getArray(groupAndkey[0], groupAndkey[1]);
    }
}
