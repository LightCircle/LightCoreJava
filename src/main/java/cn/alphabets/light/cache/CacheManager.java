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
    private List<ModJob> jobs;

    /**
     * 初始化
     *
     * @param domain domain
     */
    public void setUp(String domain) {

        String code = Constant.SYSTEM_DB_PREFIX;
        Document valid = Document.parse("{valid:1}");

        // configuration
        configuration = new Model(domain, code, Constant.SYSTEM_DB_CONFIG).list(valid);

        // validator
        validators = new Model(domain, code, Constant.SYSTEM_DB_VALIDATOR).list(valid);

        // i18n
        i18ns = new Model(domain, code, Constant.SYSTEM_DB_I18N).list(valid);

        // structure
        structures = new Model(domain, code, Constant.SYSTEM_DB_STRUCTURE).list(valid);

        // board
        boards = new Model(domain, code, Constant.SYSTEM_DB_BOARD).list(valid);

        // route
        routes = new Model(domain, code, Constant.SYSTEM_DB_ROUTE).list(valid);

        // function
        functions = new Model(domain, code, Constant.SYSTEM_DB_FUNCTION).list(valid);

        // job
        jobs = new Model(domain, code, Constant.SYSTEM_DB_JOB).list(valid);

        // tenant
        tenants = new Model(domain, code, Constant.SYSTEM_DB_TENANT).list(valid);
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

    public List<ModJob> getJobs() {
        return jobs;
    }
}
