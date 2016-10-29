package cn.alphabets.light.cache;

import cn.alphabets.light.db.mongo.DBConnection;
import cn.alphabets.light.model.*;
import com.mongodb.Block;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
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
     *
     * @param db 数据库连接
     */
    public void setUp(DBConnection db) {
        //validator
        validators = load(db, "light.validators", ModValidator.class);

        //i18n
        i18ns = load(db, "light.i18ns", ModI18n.class);

        //structure
        structures = load(db, "light.structures", ModStructure.class);

        //board
        boards = load(db, "light.boards", ModBoard.class);

        //route
        routes = load(db, "light.routes", ModRoute.class);

        //tenant
        tenants = load(db, "tenants", ModTenant.class);
    }

    private <T> List<T> load(DBConnection db, String collName, Class clz) {
        List<T> result = new ArrayList<T>();
        db.getCollection(collName)
                .find(Document.parse("{valid:1}"))
                .projection(Projections.exclude("createAt", "updateAt", "valid", "createBy", "updateBy"))
                .forEach((Block<? super Document>) document -> {
                    result.add((T) ModBase.fromDoc(document, clz));
                });
        return result;
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
