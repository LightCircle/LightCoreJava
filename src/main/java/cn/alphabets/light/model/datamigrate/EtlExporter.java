package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.entity.ModStructure;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.model.Entity;
import cn.alphabets.light.model.File;
import cn.alphabets.light.model.ModCommon;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.datarider.Rider;
import cn.alphabets.light.validator.MPath;
import cn.alphabets.light.validator.Rule;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.text.WordUtils;
import org.bson.Document;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ETL Exporter
 * Created by lilin on 2017/7/11.
 */
public class EtlExporter {

    private static final Logger logger = LoggerFactory.getLogger(EtlExporter.class);

    private int total = 0;
    private List<Document> data;
    private String file;

    private Context handler;
    private ModEtl define;
    private String clazz;
    private List<ModEtl.Mappings> mappings;


    public EtlExporter(Context handler, ModEtl define) {

        this.handler = handler;
        this.define = define;
        this.mappings = define.getMappings();
        this.clazz = define.getClass_();
        if (this.clazz != null) {
            this.clazz = Environment.instance().getPackages() + ".controller." + WordUtils.capitalize(this.clazz);
        }
    }

    public Document exec() throws IOException, BadRequestException {
        this.extract();
        this.transform();
        this.load();

        Document result = new Document();
        result.put("total", this.total);
        result.put("_id", this.file);

        return result;
    }

    private void extract() {

        Document condition = new Document("valid", 1);

        ModStructure struct = CacheManager.INSTANCE.getStructures()
                .stream()
                .filter(s -> s.getSchema().equals(define.getSchema()))
                .findFirst()
                .orElse(null);

        if (struct.getParent() != null && struct.getParent().length() > 0) {
            condition.put("type", define.getSchema());
        }

        condition.putAll(this.handler.params.getCondition());

        Plural<ModCommon> plural = (Plural<ModCommon>) Rider.call(
                handler,
                Entity.getEntityType(struct.getSchema()),
                Constant.API_TYPE_NAME_LIST,
                new Params().condition(condition)
        );

        this.data = plural.items.stream().map(Entity::toDocument).collect(Collectors.toList());
    }

    private void transform() {
        logger.debug("Start converting data");
        this.data.forEach(this::parse);
    }

    private void parse(final Document document) {

        this.mappings.forEach(mapping -> {

            // 数据保存到handler里，供后续功能参照使用
            handler.params.data(document);

            // 获取关联内容（关联数据直接替换了data内的值）
            Common.fetchLinkData(handler, mapping);

            // sanitize处理
            Object value = MPath.detectValue(Common.key(mapping), document);
            value = Rule.format(value, mapping.getSanitize());
            document.put(Common.key(mapping), value);
        });
    }

    private void load() throws IOException, BadRequestException {
        logger.debug("Start converting data");

        // 尝试调用自定义方法
        Common.invokeDump(handler, this.clazz, this.data);

        this.total = this.data.size();

        // 只获取 col 被指定的项目, 并且以 col 的值排序
        List<ModEtl.Mappings> mappings = this.define.getMappings()
                .stream()
                .sorted(Comparator.comparingInt(o -> o.getCol().intValue()))
                .collect(Collectors.toList());

        List<List<String>> document = new ArrayList<>();

        // 添加标题栏
        List<String> title = mappings.stream()
                .map(mapping -> String.valueOf(mapping.getTitle()))
                .collect(Collectors.toList());
        document.add(title);

        this.data.forEach(item -> {
            List<String> row = mappings.stream()
                    .map(m -> String.valueOf(item.get(Common.key(m)) == null ? "" : item.get(Common.key(m))))
                    .collect(Collectors.toList());

            document.add(row);
        });

        String fileName = Helper.randomGUID12();
        new Excel().dump(new FileOutputStream(fileName), document);

        // 写到 gridfs 里
        RequestFile file = new RequestFile();
        file.put(Constant.PARAM_FILE_NAME, this.define.getSchema() + ".xlsx");
        file.put(Constant.PARAM_FILE_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        file.put(Constant.PARAM_FILE_PHYSICAL, fileName);
        handler.params.data(new Document("kind", "file"));
        handler.params.file(file);

        Plural<ModFile> files = new File().add(handler);
        this.file = files.items.get(0).get_id().toHexString();
    }
}
