package cn.alphabets.light.cache;

import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mongo.Controller;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.*;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.model.Plural;
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

        Params params = new Params();
        Context handler = new Context(params, domain, Constant.SYSTEM_DB_PREFIX, null);

        // configuration
        params.setTable(Constant.SYSTEM_DB_CONFIG);
        params.setClazz(ModConfiguration.class);
        Plural<ModConfiguration> configuration = new Controller(handler, params).list();
        this.configuration = configuration.items;

        // validator
        params.setTable(Constant.SYSTEM_DB_VALIDATOR);
        params.setClazz(ModValidator.class);
        Plural<ModValidator> validators = new Controller(handler, params).list();
        this.validators = validators.items;

        // i18n
        params.setTable(Constant.SYSTEM_DB_I18N);
        params.setClazz(ModI18n.class);
        Plural<ModI18n> i18ns = new Controller(handler, params).list();
        this.i18ns = i18ns.items;

        // structure
        params.setTable(Constant.SYSTEM_DB_STRUCTURE);
        params.setClazz(ModStructure.class);
        Plural<ModStructure> structures = new Controller(handler, params).list();
        this.structures = structures.items;

        // board
        params.setTable(Constant.SYSTEM_DB_BOARD);
        params.setClazz(ModBoard.class);
        Plural<ModBoard> boards = new Controller(handler, params).list();
        this.boards = boards.items;

        // route
        params.setTable(Constant.SYSTEM_DB_ROUTE);
        params.setClazz(ModValidator.class);
        Plural<ModRoute> routes = new Controller(handler, params).list();
        this.routes = routes.items;

        // function
        params.setTable(Constant.SYSTEM_DB_FUNCTION);
        params.setClazz(ModFunction.class);
        Plural<ModFunction> functions = new Controller(handler, params).list();
        this.functions = functions.items;

        // job
        params.setTable(Constant.SYSTEM_DB_JOB);
        params.setClazz(ModJob.class);
        Plural<ModJob> jobs = new Controller(handler, params).list();
        this.jobs = jobs.items;

        // tenant
        params.setTable(Constant.SYSTEM_DB_TENANT);
        params.setClazz(ModTenant.class);
        Plural<ModTenant> tenants = new Controller(handler, params).list();
        this.tenants = tenants.items;
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
