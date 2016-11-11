package cn.alphabets.light.cache;

import cn.alphabets.light.Config;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.model.*;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

/**
 * CacheManager
 * Created by luohao on 16/10/20.
 */
public enum CacheManager {

    INSTANCE;

    private List<Configuration> configuration;
    private List<I18n> i18ns;
    private List<Tenant> tenants;
    private List<Validator> validators;
    private List<Structure> structures;
    private List<Board> boards;
    private List<Route> routes;

    /**
     * 初始化
     * @param domain domain
     */
    public void setUp(String domain) {

        String code = Config.CONSTANT.SYSTEM_DB_PREFIX;
        Document valid = Document.parse("{valid:1}");
        List<String> select;

        // configuration
        select = Arrays.asList("type","key","value","valueType");
        configuration = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_CONFIG).list(valid, select);

        // validator
        select = Arrays.asList("group","name","rule","key","option","message","sanitize","class","action","condition");
        validators = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_VALIDATOR).list(valid, select);

        // i18n
        select = Arrays.asList("type","lang","key");
        i18ns = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_I18N).list(valid, select);

        // structure
        select = Arrays.asList("public","lock","type","kind","tenant","version","schema","items","extend","tenant");
        structures = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_STRUCTURE).list(valid, select);

        // board
        select = Arrays.asList("schema","api","type","kind","path","class","action","filters","selects","sorts","reserved","script");
        boards = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_BOARD).list(valid, select);

        // route
        select = Arrays.asList("template","url","class","action");
        routes = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_ROUTE).list(valid, select);

        // tenant
        select = Arrays.asList("code","name");
        tenants = new Model(domain, code, Config.CONSTANT.SYSTEM_DB_TENANT).list(valid, select);
    }

    public List<Configuration> getConfiguration() {
        return configuration;
    }

    public List<I18n> getI18ns() {
        return i18ns;
    }

    public List<Tenant> getTenants() {
        return tenants;
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public List<Structure> getStructures() {
        return structures;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
