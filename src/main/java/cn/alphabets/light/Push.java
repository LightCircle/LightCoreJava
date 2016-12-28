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
import cn.alphabets.light.model.datarider.DBParams;
import cn.alphabets.light.model.datarider.DataRider;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.maven.model.Model;
import org.bson.Document;
import org.bson.types.ObjectId;

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

    /**
     * Perform the upload operation
     */
    void exec() {

        // Confirm the pom file
        String home = System.getProperty("user.dir");
        String pomFile = home + "/pom.xml";
        if (!new java.io.File(pomFile).exists()) {
            logger.error("The pom file does not exist.");
            return;
        }

        // Confirm the jar file
        Model pom = Helper.getPOM(pomFile);
        String jarFile = String.format("%s/target/%s-%s.jar", home, pom.getArtifactId(), pom.getVersion());
        String jarFileName = String.format("%s-%s.jar", pom.getArtifactId(), pom.getVersion());
        if (!new java.io.File(jarFile).exists()) {
            logger.error("The jar file does not exist.\nRun the mvn package command to generate the jar file");
            return;
        }

        List<RequestFile> file = new ArrayList<>();
        file.add(new RequestFile(jarFile, "application/zip", jarFileName, true));
        Params params = new Params(new org.bson.Document(), file);

        // Upload the jar file
        Environment env = Environment.instance();
        Context handler = new Context(params, env.getAppName(), SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);
        Plural<ModFile> result;
        try {
            result = new File().add(handler);
        } catch (BadRequestException e) {
            throw new RuntimeException("Error uploading file");
        }

        // Delete the old jar file
        DBParams condition = new DBParams(handler);
        condition.getCondition().put("name", "app.jar");
        DataRider.ride(ModCode.class).remove(condition);

        // Add a new jar file
        DBParams data = new DBParams(handler);
        data.getData().put("app", env.getAppName());
        data.getData().put("name", "app.jar");
        data.getData().put("type", "binary");
        data.getData().put("source", result.getItems().get(0).get_id().toHexString());
        DataRider.ride(ModCode.class).add(data);

        logger.debug(jarFile);
        logger.info("Uploaded successfully");
    }

    /**
     * Download the jar file to the specified path
     * @param path path
     */
    void pull(String path) {

        String appName = Environment.instance().getAppName();
        Params defaults = new Params(new org.bson.Document());
        Context handler = new Context(defaults, appName, SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);

        // get code data
        DBParams codeParams = new DBParams(handler).condition(new Document("name", "app.jar"));
        ModCode code = DataRider.ride(ModCode.class).get(codeParams);

        // get file data
        DBParams fileParams = new DBParams(handler).condition(new Document("_id", new ObjectId(code.getSource())));
        ModFile file = DataRider.ride(ModFile.class).get(fileParams);
        file.setPath(path);

        // save file
        new File().saveFile(handler, file);
    }

    public static void main(String[] args) {
        Environment.initialize(args);
        CacheManager.INSTANCE.setUp(Environment.instance().getAppName());
        ConfigManager.INSTANCE.setUp();

        new Push().pull(args[0]);
    }
}
