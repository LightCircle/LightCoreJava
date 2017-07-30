package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModCode;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.model.File;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import cn.alphabets.light.model.datarider.Rider;
import com.sun.tools.doclint.Env;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Model;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
        Params params = new Params(new org.bson.Document(), file);

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
        params = new Params().id(new ObjectId(code.item.getSource()));
        Singular<ModFile> file = Rider.get(handler, ModFile.class, params);
        file.item.setPath(path + JAR_NAME);

        // save file
        new File().saveFile(handler, file.item);
    }

    // 从MongoDB下载代码
    void pullSource() {

        String appName = Environment.instance().getAppName();
        Params defaults = new Params(new org.bson.Document());
        Context handler = new Context(defaults, appName, SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);

        // get code data
        Document condition = new Document("lang", "java");
        Document select = new Document("name", 1).append("type", 1).append("source", 1);
        Params params = new Params().condition(condition).select(select);

        Plural<ModCode> codes = Rider.list(handler, ModCode.class, params);
        codes.items.forEach(code -> {

            String fullName = String.format("/data/%s%s", appName, code.getName());
            Path folder = Paths.get(fullName).getParent();

            try {
                // create folder
                if (!new java.io.File(folder.toString()).exists()) {
                    Files.createDirectories(folder);
                }

                // get source data
                if (code.getType().equals("code")) {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(fullName));
                    bw.write(code.getSource());
                    bw.flush();
                    return;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // get file data
            Singular<ModFile> file = Rider.get(handler, ModFile.class, new Params().id(new ObjectId(code.getSource())));
            file.item.setPath(fullName);
            new File().saveFile(handler, file.item);
        });
    }

    // 使用mvn编译java代码，并打包成jar文件
    void buildJava() {

        String mvn = "/opt/apache-maven-3.3.9/bin/mvn";
        String params = "-Dmaven.compiler.source=1.8 -Dmaven.compiler.target=1.8 package -Djar.finalName=app";
        String cmd = String.format("%s -f /data/%s/pom.xml %s ", mvn, Environment.instance().getAppName(), params);

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

    public static void main(String[] args) {

        Environment.initialize(args);
        CacheManager.INSTANCE.setUp(Environment.instance().getAppName());
        ConfigManager.INSTANCE.setUp();

//        new Push().pullSource();
//        new Push().buildJava();
//        new Push().exec(String.format("/data/%s", Environment.instance().getAppName()));
//        new Push().pullJar("/data");
    }
}
