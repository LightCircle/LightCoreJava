package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.Helper;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.model.ModCommon;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.atteo.evo.inflector.English;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Model
 *
 * Model接受的参数为原生的Document对象
 * 返回值会根据指定的类型转换成ModCommon实例
 *
 * 额外的，Model提供一个document方法，他不对结果做转换直接返回Document对象，主要由平台内部使用
 */
public class Model {

    private static final Logger logger = LoggerFactory.getLogger(Model.class);

    private MongoDatabase db;
    private MongoCollection<Document> collection;
    private String name;
    private Class<? extends ModCommon> clazz;

    private Model() {
    }

    public Model(String domain, String code) {
        this(domain, code, null);
    }

    public Model(String domain, String code, String table) {
        this(domain, code, table, null);
    }

    public Model(String domain, String code, String table, Class<? extends ModCommon> clazz) {

        MongoClient client = Connection.instance(Environment.instance());

        this.db = client.getDatabase(domain);
        this.name = table;
        this.clazz = clazz;

        if (table != null) {
            table = English.plural(table.toLowerCase());
            if (!Constant.SYSTEM_DB.equals(domain) && !StringUtils.isEmpty(code)) {
                table = code + '.' + table;
            }

            this.collection = this.db.getCollection(table);
        }
    }

    public <T extends ModCommon> List<T> list() {
        return this.list(null);
    }

    public <T extends ModCommon> List<T> list(Bson condition) {
        return this.list(condition, null);
    }

    public <T extends ModCommon> List<T> list(Bson condition, List<String> fieldNames) {
        return this.list(condition, fieldNames, null);
    }

    public <T extends ModCommon> List<T> list(Bson condition, List<String> fieldNames, Bson sort) {
        return this.list(condition, fieldNames, sort, 0);
    }

    public <T extends ModCommon> List<T> list(Bson condition, List<String> fieldNames, Bson sort, int skip) {
        return this.list(condition, fieldNames, sort, skip, Constant.DEFAULT_LIMIT);
    }

    public <T extends ModCommon> List<T> list(Bson condition, List<String> fieldNames, Bson sort, int skip, int limit) {
        return this.list(condition, fieldNames == null ? null : Projections.include(fieldNames), sort, skip, limit);
    }


    /**
     * 检索数据
     *
     * @param condition 条件
     * @param select    选择项目
     * @param sort      排序
     * @param skip      开始位置
     * @param limit     获取件数
     * @param <T>       类型
     * @return 数据列表
     */
    @SuppressWarnings("unchecked")
    public <T extends ModCommon> List<T> list(Bson condition, Bson select, Bson sort, int skip, int limit) {

        // set fetch options
        FindIterable<Document> find = this.collection
                .find(condition)
                .projection(select)
                .sort(sort)
                .skip(skip)
                .limit(limit);

        // fetch and convert
        List<T> result = new ArrayList<>();
        find.forEach((Block<? super Document>) document -> result.add(
                (T) ModCommon.fromDocument(document, this.getModelType())
        ));

        return result;
    }

    /**
     * 检索数据，类似于list方法，不使用Entity类型转换，直接返回原生的Document对象。
     * core内部操作原生数据使用
     *
     * @param condition  条件
     * @param fieldNames 字段名称
     * @return 检索结果
     */
    public List<Document> document(Document condition, List<String> fieldNames) {

        FindIterable<Document> find = this.collection.find(condition);
        FindIterable<Document> projection = find.projection(Projections.include(fieldNames));

        List<Document> result = new ArrayList<>();
        projection.forEach((Block<? super Document>) result::add);
        return result;
    }

    public <T extends ModCommon> T get(Document condition) {
        return this.get(condition, (List<String>) null);
    }

    public <T extends ModCommon> T get(Document condition, List<String> fieldNames) {
        fieldNames = fieldNames == null ? Collections.emptyList() : fieldNames;
        return this.get(condition, Projections.include(fieldNames));
    }

    /**
     * 获取单条数据
     *
     * @param condition 条件
     * @param select    选择项目
     * @param <T>       类型
     * @return 数据对象
     */
    @SuppressWarnings("unchecked")
    public <T extends ModCommon> T get(Document condition, Bson select) {

        // default value
        condition = condition == null ? new Document() : condition;

        // set fetch condition
        FindIterable<Document> find = this.collection.find(condition).projection(select);

        return (T) ModCommon.fromDocument(find.first(), this.getModelType());
    }

    public long remove(Document condition) {
        return this.update(condition, new Document("valid", Constant.INVALID));
    }

    public long delete(Document condition) {
        return this.collection.deleteMany(condition).getDeletedCount();
    }

    public long update(Document condition, Document data) {
        return this.collection.updateMany(condition, new Document("$set", data)).getModifiedCount();
    }

    public long count(Document condition) {
        return this.collection.count(condition);
    }

    public long add(List<Document> document) {
        this.collection.insertMany(document);
        return document.size();
    }

    @SuppressWarnings("unchecked")
    public <T extends ModCommon> T add(Document document) {
        this.collection.insertOne(document);
        return (T) ModCommon.fromDocument(document, this.getModelType());
    }

    public long increase(String type) {
        Document document = this.collection.findOneAndUpdate(
                Filters.eq("type", type),
                Updates.inc("sequence", 1L),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        return document.getLong("sequence");
    }

    /**
     * Write the file to GridFS
     *
     * @param path full path
     * @return file meta info
     */
    public GridFSFile writeFileToGrid(String path) {

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        FileInputStream stream;
        String contentType;
        try {
            stream = new FileInputStream(file);
            contentType = Helper.getContentType(stream);    // get content type
            stream.getChannel().position(0);                // Resets the file stream position
        } catch (IOException e) {
            logger.error(e);
            return null;
        }

        return this.writeStreamToGrid(file.getName(), stream, contentType);
    }


    public GridFSFile writeFileToGrid(Document meta) {

        String name = meta.getString(Constant.PARAM_FILE_NAME);
        String type = meta.getString(Constant.PARAM_FILE_TYPE);

        try {
            FileInputStream stream = new FileInputStream(new File(meta.getString(Constant.PARAM_FILE_PHYSICAL)));
            return this.writeStreamToGrid(name, stream, type);
        } catch (FileNotFoundException e) {
            logger.error(e);
            return null;
        }
    }

    /**
     * Writes a stream to GridFS
     *
     * @param name        file name
     * @param stream      stream
     * @param contentType file content-type
     * @return file meta info
     */
    public GridFSFile writeStreamToGrid(String name, InputStream stream, String contentType) {

        // create a gridFSBucket using the default bucket name "fs"
        GridFSBucket gridFSBucket = GridFSBuckets.create(this.db);

        // create some custom options
        Document meta = new Document("contentType", contentType);
        GridFSUploadOptions options = new GridFSUploadOptions().metadata(meta);

        // upload stream
        ObjectId fileId = gridFSBucket.uploadFromStream(name, stream, options);
        GridFSFile fs = gridFSBucket.find(new Document("_id", fileId)).first();
        return fs;
    }

    /**
     * Reads the stream from GridFS
     *
     * @param fileId       file id
     * @param outputStream stream
     * @return file meta info
     */
    public ModFile readStreamFromGrid(String fileId, OutputStream outputStream) {
        return this.readStreamFromGrid(new ObjectId(fileId), outputStream);
    }


    public ByteArrayOutputStream readStreamFromGrid(ObjectId fileId) {

        GridFSBucket gridFSBucket = GridFSBuckets.create(this.db);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        gridFSBucket.downloadToStream(fileId, outputStream);
        return outputStream;
    }

    public ByteArrayOutputStream readStreamFromGrid(ObjectId fileId, long offset, long length) throws IOException {

        GridFSBucket gridFSBucket = GridFSBuckets.create(this.db);
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(fileId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (IOUtils.copyLarge(gridFSDownloadStream, outputStream, offset, length) != length) {
            gridFSDownloadStream.close();
            throw new EOFException(String.format("GridFSDownloadStream unsatisfied offset : %d ,length : %d", offset, length));
        }
        gridFSDownloadStream.close();

        return outputStream;
    }

    public ModFile readStreamFromGrid(ObjectId fileId, OutputStream outputStream) {

        GridFSBucket gridFSBucket = GridFSBuckets.create(this.db);

        gridFSBucket.downloadToStream(fileId, outputStream);
        GridFSFile fs = gridFSBucket.find(new Document("_id", fileId)).first();

        ModFile file = new ModFile();
        file.setName(fs.getFilename());
        file.setLength(fs.getLength());
        file.setContentType(fs.getMetadata().getString("contentType"));
        file.setFileId(fileId);

        return file;
    }

    public void deleteFromGrid(ObjectId fileId) {
        GridFSBuckets.create(this.db).delete(fileId);
    }


    /**
     * 获取Entity类的类型，通过反射生成具体表名对应的类型
     * - 系统表的Entity在 cn.alphabets.light.entity 包下
     * - 而用户表的Entity在 用户包名.entity 下
     *
     * @param structure 表名称
     * @return 类型
     */
    public static Class getModelType(String structure) {
        String className = Constant.MODEL_PREFIX + WordUtils.capitalize(structure);

        // 如果前缀是系统表，那么包名称使用 cn.alphabets.light，否则使用用户定义的包名
        String packageName = system.contains(structure)
                ? Constant.DEFAULT_PACKAGE_NAME + ".entity"
                : Environment.instance().getPackages() + ".entity";

        try {
            return Class.forName(packageName + "." + className);
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(Constant.DEFAULT_PACKAGE_NAME + ".entity." + className);
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    private Class getModelType() {

        if (this.clazz != null) {
            return this.clazz;
        }

        return getModelType(this.name);
    }

    public static List<String> system = Arrays.asList(
            Constant.SYSTEM_DB_BOARD,
            Constant.SYSTEM_DB_CONFIG,
            Constant.SYSTEM_DB_VALIDATOR,
            Constant.SYSTEM_DB_I18N,
            Constant.SYSTEM_DB_STRUCTURE,
            Constant.SYSTEM_DB_BOARD,
            Constant.SYSTEM_DB_ROUTE,
            Constant.SYSTEM_DB_TENANT,
            Constant.SYSTEM_DB_FILE,
            Constant.SYSTEM_DB_ETL,
            Constant.SYSTEM_DB_SETTING,
            Constant.SYSTEM_DB_FUNCTION,
            Constant.SYSTEM_DB_CODE
    );
}
