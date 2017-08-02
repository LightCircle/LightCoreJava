package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.db.mysql.Connection;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModCode;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.model.File;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import cn.alphabets.light.model.datarider.MongoRider;
import cn.alphabets.light.model.datarider.Rider;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.alphabets.light.Constant.DEFAULT_JOB_USER_ID;
import static cn.alphabets.light.Constant.SYSTEM_DB_PREFIX;

/**
 * Push
 * Created by lilin on 2016/12/27.
 */
class Push {

    private static final Logger logger = LoggerFactory.getLogger(Push.class);
    private static final String JAR_NAME = "/app.jar";

    public static void main(String[] args) {

        Environment env = Environment.initialize(args);
        CacheManager.INSTANCE.setUp(Environment.instance().getAppName());
        ConfigManager.INSTANCE.setUp();

        if (env.isRDB()) {
            try {
                Connection.instance(env);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        List<String> list = Arrays.asList(args);
        Push push = new Push();
        String workdir = String.format("/data/%s", Environment.instance().getAppName());

        if (list.contains("-build")) {
            push.pullSource(workdir);
            push.buildJava(workdir);
            push.exec(workdir);
        }

        if (list.contains("-pull")) {
            push.pullJar(workdir);
        }
    }

    /**
     * Perform the upload operation
     */
    void exec() {
        this.exec(System.getProperty("user.dir"));
    }

    void exec(String home) {

        String jarFile = String.format("%s/target%s", home, JAR_NAME);
        logger.debug(jarFile);

        if (!new java.io.File(jarFile).exists()) {
            logger.error("The jar file does not exist.\nRun the mvn package command to generate the jar file");
            throw new RuntimeException("The jar file does not exist.");
        }

        List<RequestFile> file = new ArrayList<>();
        file.add(new RequestFile(jarFile, "application/zip", JAR_NAME, true));
        Params params = new Params(new Document(), file).data(new Document("kind", "file"));

        // Upload the jar file
        Context handler = new Context(params, Environment.instance().getAppName(), SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);
        Plural<ModFile> result;
        try {
            result = new File().add(handler);
        } catch (BadRequestException e) {
            throw new RuntimeException("Error uploading file");
        }

        // Delete the old jar file
        Rider.remove(handler, ModCode.class, new Params().condition(new Document("name", JAR_NAME)));

        // Add a new jar file
        Document data = new Document("app", Environment.instance().getAppName())
                .append("name", JAR_NAME)
                .append("type", "binary")
                .append("lang", "java")
                .append("md5", Helper.fileMD5(jarFile))
                .append("source", result.items.get(0).get_id().toHexString());
        Rider.add(handler, ModCode.class, new Params().data(data));

        logger.info("Uploaded successfully");
    }

    /**
     * Download the jar file to the specified path
     *
     * @param path path
     */
    void pullJar(String path) {

        String appName = Environment.instance().getAppName();
        Params defaults = new Params(new org.bson.Document());
        Context handler = new Context(defaults, appName, SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);

        // get code data
        Params params = new Params().condition(new Document("name", JAR_NAME));
        Singular<ModCode> code = Rider.get(handler, ModCode.class, params);

        // get file data
        handler.params.id(new ObjectId(code.item.getSource()));
        Singular<ModFile> file = Rider.get(handler, ModFile.class);
        file.item.setPath(path + JAR_NAME);

        // save file
        new File().saveFile(handler, file.item);
    }

    // 从MongoDB下载代码
    void pullSource(String path) {

        String appName = Environment.instance().getAppName();
        Params defaults = new Params(new org.bson.Document());
        Context handler = new Context(defaults, appName, SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);

        Document condition = new Document("lang", "java");
        Document select = new Document("name", 1).append("type", 1).append("source", 1);
        Params params = new Params().condition(condition).select(select);

        // find code data （需要从MongoDB下载，所以没有使用Rider）
        Plural<ModCode> codes = this.getCodeList(handler, params);
        codes.items.forEach(code -> {

            String fullName = String.format("%s%s", path, code.getName());
            Path folder = Paths.get(fullName).getParent();

            try {
                // create folder
                if (!new java.io.File(folder.toString()).exists()) {
                    Files.createDirectories(folder);
                }

                // get source data
                if (code.getType().equals("code")) {
                    Writer bw = new OutputStreamWriter(new FileOutputStream(fullName), StandardCharsets.UTF_8);
                    bw.write(code.getSource());
                    bw.flush();
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // get file data
            Singular<ModFile> file = this.getFile(handler, new Params().id(new ObjectId(code.getSource())));
            file.item.setPath(fullName);
            new File().saveFileFromMongo(handler, file.item);
        });
    }

    // 使用mvn编译java代码，并打包成jar文件
    void buildJava(String path) {

        String mvn = "/opt/apache-maven-3.3.9/bin/mvn";
        String params = "-Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8 package -Djar.finalName=app -Dproject.build.sourceEncoding=UTF-8";
        String cmd = String.format("%s -f %s/pom.xml %s ", mvn, path, params);

        ByteArrayOutputStream success = new ByteArrayOutputStream();
        ByteArrayOutputStream error = new ByteArrayOutputStream();

        try {
            Process mvnProcess = Runtime.getRuntime().exec(cmd);
            Thread errorThread = new Thread(() -> {
                try {
                    IOUtils.copy(mvnProcess.getErrorStream(), error);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            Thread successThread = new Thread(() -> {
                try {
                    IOUtils.copy(mvnProcess.getInputStream(), success);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            errorThread.start();
            successThread.start();
            mvnProcess.waitFor();

            logger.debug(new String(error.toByteArray()));
            logger.debug(new String(success.toByteArray()));

        } catch (IOException | InterruptedException e) {
            logger.error("Compile failed.");
            throw new RuntimeException(e);
        }
    }

    private Plural<ModCode> getCodeList(Context handler, Params params) {
        ModBoard board = Rider.getBoard(ModCode.class, "list");
        return (Plural<ModCode>)new MongoRider().call(handler, ModCode.class, board, params);
    }

    private Singular<ModCode> getCode(Context handler, Params params) {
        ModBoard board = Rider.getBoard(ModCode.class, "get");
        return (Singular<ModCode>)new MongoRider().call(handler, ModCode.class, board, params);
    }

    private Singular<ModFile> getFile(Context handler, Params params) {
        ModBoard board = Rider.getBoard(ModFile.class, "get");
        return (Singular<ModFile>)new MongoRider().call(handler, ModFile.class, board, params);
    }
}
