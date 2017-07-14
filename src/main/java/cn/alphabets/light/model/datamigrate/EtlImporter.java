package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.validator.MPath;
import cn.alphabets.light.validator.Rule;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Etl importer
 * <p>
 * options: {
 * primitive 存放源数据的临时表
 * processed 存放加工好的数据collection
 * type 类型 excel csv
 * }
 */
public class EtlImporter {

    private static final Logger logger = LoggerFactory.getLogger(EtlImporter.class);
    private static final String PREFIX = "tmp.";

    private List<Document> log;
    private int total = 0;
    private int success = 0;

    private Context handler;
    private ModEtl define;
    private String clazz;
    private List<ModEtl.Mappings> mappings;
    private Model primitive;
    private Model processed;
    private Model target;

    public EtlImporter(Context handler, ModEtl define) {

        this.handler = handler;
        this.define = define;
        this.mappings = define.getMappings();
        this.log = new ArrayList<>();
        this.clazz = define.getClass_();
        if (this.clazz != null) {
            this.clazz = Environment.instance().getPackages() + ".controller." + WordUtils.capitalize(this.clazz);
        }

        // 临时表（原生数据）
        String primitive = define.getPrimitive();
        if (primitive == null) {
            primitive = PREFIX + Helper.randomGUID4();
        }
        this.primitive = new Model(handler.domain(), handler.code(), primitive);

        // 临时表（加工后数据）
        String processed = define.getProcessed();
        if (processed == null) {
            processed = PREFIX + Helper.randomGUID4();
        }
        this.processed = new Model(handler.domain(), handler.code(), processed);

        // 最终表
        this.target = new Model(handler.domain(), handler.code(), define.getSchema());
    }

    public Document exec() throws IOException {
        this.initialize();
        this.extract();
        this.transform();
        this.load();
        this.end();

        Document result = new Document();
        result.put("total", this.total);
        result.put("success", this.success);
        if (this.log.size() > 0) {
            result.put("error", this.log);
        }

        return result;
    }

    private void initialize() {

        logger.debug("Start initialization");

        this.primitive.dropCollection();
        this.processed.dropCollection();

        logger.debug("Try to call the user's initialization method");
        Common.invokeInitialize(this.handler, this.clazz, this.primitive);
    }

    private void extract() throws IOException {

        if (this.define.getType().equals("excel")) {

            logger.debug("Load data from excel file");

            // 读取Excel
            InputStream excel = new FileInputStream(this.handler.params.getFiles().get(0).getFilePath());
            List<Document> data = new Excel().parse(excel, this.mappings);

            // 尝试调用用户的Ctrl
            Common.invokeBefore(this.handler, this.clazz, data);

            // 添加的临时表
            data.forEach(item -> this.primitive.add(item));
            this.total = data.size();
        }

        // TODO:
        if (this.define.getType().equals("csv")) {
            logger.debug("Load data from csv file");
        }
    }

    private void transform() {

        logger.debug("Start converting data");

        int index = 1;
        for (Document document : this.primitive.getIterable()) {

            boolean hasError = this.parse(document, index++);
            if (hasError) {
                if (this.define.getAllowError() && this.define.getAllowErrorMax() > 0) {
                    if (this.log.size() < this.define.getAllowErrorMax()) {
                        continue;
                    }
                }
                break;
            }

            this.processed.add(document);
        }
    }

    private boolean parse(final Document document, int index) {

        this.mappings.forEach(mapping -> {

            Object value = MPath.detectValue(Common.key(mapping), document);
            if (value == null) {
                return;
            }

            // sanitize处理
            value = Rule.format(value, mapping.getSanitize());
            document.put(Common.key(mapping), value);

            // 数据保存到handler里，供后续功能参照使用
            handler.params.data(document);

            // 获取关联内容（关联数据直接替换了data内的值）
            Common.fetchLinkData(handler, mapping);
        });

        // 尝试调用开发者自定义的处理方法
        logger.debug("Try to call the user's parse method");
        Common.invokeParse(handler, this.clazz, document);

        // 数据校验
        List<Document> error = Rule.isValid(handler, this.define.getName());
        if (error != null) {
            error.forEach(item -> item.put("row", index + 1));
            this.log.addAll(error);
            return true;
        }

        // 尝试调用开发者自定义的数据校验
        logger.debug("Try to call the user's valid method");
        error = Common.invokeValid(handler, this.clazz, document);
        if (error != null) {
            this.log.addAll(error);
            return true;
        }

        document.remove("_original");
        return false;
    }

    private void load() {

        logger.debug("Save the data to the final table");

        // 有校验错误, 且 allowError = false 则不更新最终数据库
        if (!this.define.getAllowError() && this.log.size() > 0) {
            return;
        }

        for (Document document : this.processed.getIterable()) {

            // 尝试调用用户的方法
            logger.debug("Try to call the user's after method");
            Common.invokeAfter(handler, this.clazz, document);

            if (this.define.getAllowUpdate()) {
                // 更新

                Document condition = new Document("valid", 1);
                this.define.getUniqueKey().forEach(uk -> condition.put((String) uk, document.get(uk)));

                if (this.target.count(condition) > 0) {
                    this.target.update(condition, document);
                } else {
                    this.target.add(document);
                }
            } else {

                // 添加
                this.target.add(document);
            }

            // 计数
            this.success = this.success + 1;
        }
    }

    private void end() {

        logger.debug("End processing");

        this.primitive.dropCollection();
        this.processed.dropCollection();
    }
}
