package cn.alphabets.light.db.mysql;

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
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.bson.Document;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller
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
        this.model = new Model(handler.domain(), handler.getCode());
    }

    public <T extends ModCommon> Plural<T> list() {

        if (!params.getCondition().containsKey("valid")) {
            params.getCondition().put("valid", Constant.VALID);
        }

        logger.debug("[LIST] DB params : " + params.toString());

        List<Document> items = this.model.list(params.getScript(), params.getCondition());

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        Plural<T> result = new Plural<>((long) items.size(), items
                .stream()
                .map(document -> (T) ModCommon.fromDocument(document, type))
                .collect(Collectors.toList()));


        // 如果设定了limit值，并且获取的件数等于limit值（可能有更多的值）时，获取件数
        if (params.getLimit() > 0 && items.size() >= (params.getSkip() + params.getLimit())) {
            result.totalItems = this.countList();
        }

        return result;
    }

    private long countList() {

        // 使用List脚本的条件，获取数据的件数（在原有script基础上删除LIMIT和OFFSET语句，然后检索件数）
        String sql = params.getScript();
        sql = sql.replaceAll("LIMIT[ ]*\\d*", "");
        sql = sql.replaceAll("OFFSET[ ]*\\d*", "");
        sql = String.format("SELECT COUNT(1) AS COUNT FROM (%s) AS T", sql);

        params.limit(0);
        params.script(sql);
        return this.count();
    }

    public <T extends ModCommon> Singular<T> get() {
        logger.debug("[GET] DB params : " + params.toString());

        Document document = this.model.get(params.getScript(), params.getCondition());

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Singular(ModCommon.fromDocument(document, type));
    }

    public <T extends ModCommon> Singular<T> add() {
        logger.debug("[ADD] DB params : " + params.toString());

        Document document = params.getData();
        document.put("createAt", new Date());
        document.put("createBy", this.uid);
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);
        document.put("valid", Constant.VALID);

        Document confirmed = Entity.fromDocument(
                document,
                params.getClazz(),
                this.handler.tz()).toDocument();

        Class<ModCommon> type = Entity.getEntityType(this.params.getTable());
        return new Singular(ModCommon.fromDocument(this.model.add(params.getScript(), confirmed), type));
    }

    public <T extends ModCommon> Singular<T> update() {
        logger.debug("[UPDATE] DB params : " + params.toString());

        Document document = params.getData();
        document.put("updateAt", new Date());
        document.put("updateBy", this.uid);

        Document confirmed = Entity.fromDocument(
                document,
                params.getClazz(),
                this.handler.tz()).toDocument();

        Document condition = params.getCondition();
        if (condition == null || condition.size() == 0) {
            throw DataRiderException.ParameterUnsatisfied("Update condition can not be empty.");
        }

        return new Singular(this.model.update(params.getScript(), confirmed, condition));
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

        return this.model.update(params.getScript(), document, condition);
    }

    public Long count() {
        return this.model.count(params.getScript(), params.getCondition());
    }

    public Singular<ModFile> writeFile(RequestFile file) {

        String insert = String.format(
                "INSERT INTO `%s`.`file` (" +
                        "`_id`, `valid`, `createAt`, `createBy`, `data`" +
                        ") VALUES (" +
                        "<%%= data._id %%>, 1, <%%= data.createAt %%>, <%%= data.createBy %%>, ?)",
                handler.domain());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(new FileInputStream(new java.io.File(file.getFilePath())), baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Document document = new Document();
        document.put("createAt", new Date());
        document.put("createBy", this.uid);
        document.put("valid", Constant.VALID);
        document.put("data", baos.toByteArray());

        document = this.model.writeFile(insert, document);
        return new Singular<>(ModFile.fromDocument(document, ModFile.class));
    }

    public ByteArrayOutputStream readFile() {

        String select = String.format(
                "SELECT `_id`, `data` FROM `%s`.`file` WHERE `_id` = <%%= condition._id %%> AND `valid` = 1",
                handler.domain());

        byte[] bytes = this.model.readFile(select, params.getCondition());
        ByteArrayOutputStream stream = new ByteArrayOutputStream(bytes.length);
        stream.write(bytes, 0, bytes.length);
        return stream;
    }
}
