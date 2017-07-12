package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.db.mongo.Model;
import cn.alphabets.light.http.Context;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.Document;

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

    private Model primitive;
    private Model processed;
    private Model target;

    public EtlImporter(Context handler, Document options) {

        this.type = options.getString("type");

        options.getString("primitive");

        this.primitive = new Model(handler.domain(), handler.code(), Constant.SYSTEM_DB_CONFIG);
        this.processed = new Model(handler.domain(), handler.code(), Constant.SYSTEM_DB_CONFIG);
        this.target = new Model(handler.domain(), handler.code(), Constant.SYSTEM_DB_CONFIG);
    }

    public void exec() {
    }

    private void initialize() {
    }

    private void extract() {

        if (this.type.equals("excel")) {

        }

        if (this.type.equals("csv")) {
            // TODO:
        }
    }

    private void transform() {
    }

    private void load() {
    }

    private void end() {
    }
}
