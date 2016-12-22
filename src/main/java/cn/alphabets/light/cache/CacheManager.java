package cn.alphabets.light.cache;

import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.*;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

/**
 * CacheManager
 * Created by luohao on 16/10/20.
 */
public enum CacheManager {

    INSTANCE;

    private List<ModConfiguration> configuration;
    private List<ModI18n> i18ns;
    private List<ModTenant> tenants;
    private List<ModValidator> validators;
    private List<ModStructure> structures;
    private List<ModBoard> boards;
    private List<ModRoute> routes;
    private List<ModFunction> functions;

    /**
     * 初始化
     *
     * @param domain domain
     */
    public void setUp(String domain) {

        String code = Constant.SYSTEM_DB_PREFIX;
        Document valid = Document.parse("{valid:1}");
        List<String> select;

        // configuration
        select = Arrays.asList("type", "key", "value", "valueType");
        configuration = new Model(domain, code, Constant.SYSTEM_DB_CONFIG)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // validator
        select = Arrays.asList("group", "name", "rule", "key", "option", "message", "sanitize", "class", "action", "condition");
        validators = new Model(domain, code, Constant.SYSTEM_DB_VALIDATOR)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // i18n
        select = Arrays.asList("type", "lang", "key");
        i18ns = new Model(domain, code, Constant.SYSTEM_DB_I18N)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // structure
        select = Arrays.asList("public", "lock", "type", "kind", "tenant", "version", "schema", "items", "extend", "tenant");
        structures = new Model(domain, code, Constant.SYSTEM_DB_STRUCTURE)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // board
        select = Arrays.asList("schema", "api", "type", "kind", "path", "class", "action", "filters", "selects", "sorts", "reserved", "script");
        boards = new Model(domain, code, Constant.SYSTEM_DB_BOARD)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // route
        select = Arrays.asList("template", "url", "class", "action");
        routes = new Model(domain, code, Constant.SYSTEM_DB_ROUTE)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // function
        select = Arrays.asList("order", "description", "menu", "reserved", "status", "type", "url", "icon", "kind", "parent");
        functions = new Model(domain, code, Constant.SYSTEM_DB_FUNCTION)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);

        // tenant
        // tenant table name has no prefix,so code here pass null
        select = Arrays.asList("code", "name");
        tenants = new Model(domain, null, Constant.SYSTEM_DB_TENANT)
                .list(valid, select, null, 0, Constant.MAX_LIMIT);
    }

    public List<ModConfiguration> getConfiguration() {
        return configuration;
    }

    public List<ModI18n> getI18ns() {
        return i18ns;
    }

    public List<ModTenant> getTenants() {
        return tenants;
    }

    public List<ModValidator> getValidators() {
        return validators;
    }

    public List<ModStructure> getStructures() {
        return structures;
    }

    public List<ModBoard> getBoards() {
        return boards;
    }

    public List<ModRoute> getRoutes() {
        return routes;
    }

    public List<ModFunction> getFunctions() {
        return functions;
    }
}
