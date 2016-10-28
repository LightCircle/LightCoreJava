package cn.alphabets.light.config;

import cn.alphabets.light.db.mongo.DBConnection;

import java.util.List;

/**
 * Created by luohao on 2016/10/28.
 */
public interface ConfigManager {


    String DEFAULT_CONFIG_COLLECTION_NAME = "light.configurations";

    /**
     * 初始化
     *
     * @param db 数据库连接
     */
    void setUp(DBConnection db);

    /**
     * 获取单例
     *
     * @return
     */
    static ConfigManager getInstance() {
        return ConfigManagerImpl.INSTANCE;
    }

    /**
     * 获取字符串类型的设定
     *
     * @param group 设定分类
     * @param key   设定key
     * @return 设定值
     */
    String getString(String group, String key);

    String getString(String[] groupAndkey);

    /**
     * 获取整形类型的设定
     *
     * @param group 设定分类
     * @param key   设定key
     * @return 设定值
     */
    long getLong(String group, String key);

    long getLong(String[] groupAndkey);

    /**
     * 获取布尔值类型的设定
     *
     * @param group 设定分类
     * @param key   设定key
     * @return 设定值
     */
    boolean getBoolean(String group, String key);

    boolean getBoolean(String[] groupAndkey);

    /**
     * 获取布尔值类型的设定
     *
     * @param group 设定分类
     * @param key   设定key
     * @return 设定值
     */
    List<String> getArray(String group, String key);

    List<String> getArray(String[] groupAndkey);
}
