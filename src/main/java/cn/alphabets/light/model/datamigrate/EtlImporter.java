package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.exception.LightException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.validator.MPath;
import cn.alphabets.light.validator.Rule;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private List<String> log;
    private int total = 0;
    private int success = 0;

    private String type;
    private String clazz;
    private List<ModEtl.Mappings> mappings;
    private boolean allowError;
    private int allowErrorMax;
    private boolean allowUpdate;


    private Context handler;
    private Model primitive;
    private Model processed;
    private Model target;

    public EtlImporter(Context handler, Document options) {

        this.type = options.getString("type");
        this.clazz = options.getString("class");
        this.allowError = options.getBoolean("allowError");
        this.allowErrorMax = options.getInteger("allowErrorMax");
        this.allowUpdate = options.getBoolean("allowUpdate");
        this.mappings = (List<ModEtl.Mappings>) options.get("mappings");

        if (this.clazz != null) {
            this.clazz = Environment.instance().getPackages() + ".controller." + WordUtils.capitalize(this.clazz);
        }

        this.handler = handler;

        String primitive = options.getString("primitive");
        if (primitive == null) {
            primitive = PREFIX + Helper.randomGUID4();
        }
        this.primitive = new Model(handler.domain(), handler.code(), primitive);

        String processed = options.getString("processed");
        if (processed == null) {
            processed = PREFIX + Helper.randomGUID4();
        }
        this.processed = new Model(handler.domain(), handler.code(), processed);

//        this.target = new Model(handler.domain(), handler.code(), Constant.SYSTEM_DB_CONFIG);
    }

    public void exec() throws IOException {
        this.initialize();
        this.extract();
        this.transform();
    }

    private void initialize() {
        this.primitive.dropCollection();
        this.processed.dropCollection();
        Common.invokeInitialize(this.handler, this.clazz, this.primitive);
    }

    private void extract() throws IOException {

        if (this.type.equals("excel")) {

            // 读取Excel
            InputStream excel = new FileInputStream(this.handler.params.getFiles().get(0).getFilePath());
            List<Document> data = new Excel().parse(excel, this.mappings);

            // 尝试调用用户的Ctrl
            List<Document> result = Common.invokeBefore(this.handler, this.clazz, data);
            if (result != null) {
                data = result;
            }

            // 添加的临时表
            data.forEach(item -> this.primitive.add(item));
        }

        if (this.type.equals("csv")) {
            // TODO:
        }
    }

    private void transform() {

        for (Document document : this.primitive.getIterable()) {

            boolean hasError = this.parse(document);
            if (hasError) {
                if (this.allowError && this.allowErrorMax > 0) {
                    if (this.log.size() < this.allowErrorMax) {
                        continue;
                    }
                }
                break;
            }

            this.processed.add(document);
        }
    }

    private boolean parse(Document document) {

        this.mappings.forEach(mapping -> {

            if (mapping.getSanitize() != null) {
                Object value = MPath.detectValue(Common.key(mapping), document);
                if (value == null) {
                    return;
                }

                // sanitize处理
                value = Rule.format(value, (Document) mapping.getSanitize());
                document.put(Common.key(mapping), value);

                // 数据保存到handler里，供后续功能参照使用
                handler.params.data(document);

                // 获取关联内容
            }

        });

        // 尝试调用开发者自定义的处理方法

        // 数据校验

        // 尝试调用开发者自定义的数据校验

        return false;
    }

    private void load() {
    }

    private void end() {
    }

}
