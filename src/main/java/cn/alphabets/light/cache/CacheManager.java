package cn.alphabets.light.cache;

import cn.alphabets.light.Config;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.model.*;

import java.util.List;

/**
 * CacheManager
 * Created by luohao on 16/10/20.
 */
public enum CacheManager {

    INSTANCE;

    private List<ModI18n> i18ns;
    private List<ModTenant> tenants;
    private List<ModValidator> validators;
    private List<ModStructure> structures;
    private List<ModBoard> boards;
    private List<ModRoute> routes;

    /**
     * 初始化
     * @param domain domain
     */
    public void setUp(String domain) {

        String code = Config.Constant.SYSTEM_DB_PREFIX;

        //validator
        validators = new Model(domain, code, "validator").list(ModValidator.class);

        //i18n
        i18ns = new Model(domain, code, "i18n").list(ModI18n.class);

        //structure
        structures = new Model(domain, code, "structure").list(ModStructure.class);

        //board
        boards = new Model(domain, code, "board").list(ModBoard.class);

        //route
        routes = new Model(domain, code, "route").list(ModRoute.class);

        //tenant
        tenants = new Model(domain, code, "tenant").list(ModTenant.class);
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
}
