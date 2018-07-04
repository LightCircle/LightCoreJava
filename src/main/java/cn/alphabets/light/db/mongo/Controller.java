package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.DataRiderException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.client.gridfs.model.GridFSFile;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ctrl使用handler初始化
 * - handler包含domain，code，table等信息
 * - handler的params里可以获取操作数据库的条件及数据
 * <p>
 * Ctrl的方法有一下几种类型的返回值
 * - get 返回Singular对象
 * - add 返回Singular对象，插入多条时返回件数，插入单条时返回插入的数据
 * - update 返回Singular对象，更新多条时返回件数，更新单条时返回更新的数据
 * - list 返回Plural对象
 * - count 返回Long
 */
public class Controller {

    private static final io.vertx.core.logging.Logger logger = LoggerFactory.getLogger(Controller.class);

    private Context handler;
    private Params params;
    private Model model;
    private String uid;

    public Controller(Context handler) {
        this(handler, null);
    }

    public Controller(Context handler, Params params) {
        this.handler = handler;
        this.uid = handler.uid();
        this.params = params;

        if (params == null) {
            this.params = handler.params;
        }
        this.model = new Model(handler.domain(), handler.getCode(), this.params.getTable(), this.params.getClazz());
    }


    public <T extends ModCommon> Plural<T> list() {

        if (!params.getCondition().containsKey("valid")) {
            params.getCondition().put("valid", Constant.VALID);
        }

        logger.debug("[LIST] DB params : " + params.toString());

        List<Document> items = this.model.list(
                params.getCondition(),
                params.getSelect(),
                params.getSort(),
                params.getSkip(),
                params.getLimit());

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Plural<>(this.count(), items
                .stream()
                .map(document -> (T) ModCommon.fromDocument(document, type))
                .collect(Collectors.toList()));
    }


    public <T extends ModCommon> Singular<T> get() {
        logger.debug("[GET] DB params : " + params.toString());

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Get condition can not be empty.");
        }

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Singular(ModCommon.fromDocument(this.model.get(condition, params.getSelect()), type));
    }


    public <T extends ModCommon> Singular<T> add() {
        logger.debug("[ADD] DB params : " + params.toString());

        // 支持多条数据的插入，遍历列表设定共同项目，并转换类型
        if (params.getDatas() != null) {
            List<Document> documents = params.getDatas();

            documents.forEach(document -> {
                document.put("createAt", new Date());
                document.put("createBy", this.uid);
                document.put("updateAt", new Date());
                document.put("updateBy", this.uid);
                document.put("valid", Constant.VALID);
                document.put("_id", new ObjectId());
            });

            List<Document> confirmed = Entity.fromDocument(
                    documents,
                    params.getClazz(),
                    this.handler.tz()
            ).stream().map(Entity::toDocument).collect(Collectors.toList());

            this.model.add(confirmed);

            // 多条数据时，返回插入的数据件数
            return new Singular<>(this.model.add(confirmed));
        }

        // 插入一条数据
        Document document = params.getData();
        document.put("createAt", new Date());
        document.put("createBy", this.uid);
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        document.put("valid", Constant.VALID);
        document.put("_id", new ObjectId());

        Document confirmed = Entity.fromDocument(
                document,
                params.getClazz(),
                this.handler.tz()).toDocument();

        confirmed = this.model.add(confirmed);
        return new Singular(ModCommon.fromDocument(confirmed, Entity.getEntityType(this.params.getTable())));
    }


    public <T extends ModCommon> Singular<T> update() {
        logger.debug("[UPDATE] DB params : " + params.toString());

        Document document = params.getData();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        Document confirmed = Entity.fromDocument(
                document,
                params.getClazz(),
                this.handler.tz()).toDocument(true);

        // Entity 转换成 Document时 null 值丢失，做一次浅拷贝解决不能更新 null 的问题
        for (Field field : params.getClazz().getDeclaredFields()) {
            String key = field.getName();
            JsonProperty property = field.getDeclaredAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class);
            if (property != null) {
                key = property.value();
            }

            boolean hasAttr = document.keySet().contains(key);
            if (hasAttr && document.get(key) == null) {
                confirmed.put(key, null);
            }
        }

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Update condition can not be empty.");
        }

        Long modifiedCount = this.model.update(condition, confirmed);
        if (modifiedCount > 1) {
            return new Singular<>(modifiedCount);
        }

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Singular(ModCommon.fromDocument(this.model.get(condition, params.getSelect()), type));
    }


    public Long remove() {
        logger.debug("[REMOVE] DB params : " + params.toString());

        Document document = new Document();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        document.put("valid", Constant.INVALID);

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Remove condition can not be empty.");
        }

        return this.model.update(condition, document);
    }

    public Long count() {
        return this.model.count(this.params.getCondition());
    }


    public ByteArrayOutputStream readStreamFromGrid() {
        return this.model.readStreamFromGrid(params.getCondition().getObjectId("_id"));
    }


    public ByteArrayOutputStream readStreamFromGrid(long offset, long length) {

        try {
            return this.model.readStreamFromGrid(params.getCondition().getObjectId("_id"), offset, length);
        } catch (IOException e) {
            throw DataRiderException.GridFSError("Read file error", e);
        }
    }


    public ModFile readStreamFromGrid(OutputStream outputStream) {
        return this.model.readStreamFromGrid(params.getCondition().getObjectId("_id"), outputStream);
    }


    public GridFSFile writeFileToGrid(RequestFile file) {
        return this.model.writeFileToGrid(file);
    }


    public void deleteFromGrid() {
        this.model.deleteFromGrid(params.getCondition().getObjectId("_id"));
    }

}
