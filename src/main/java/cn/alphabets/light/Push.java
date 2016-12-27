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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    void exec() {

        // Confirm the pom file
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        String home;
        try {
            home = URLDecoder.decode(path, "UTF-8").replace("/target/classes/", "");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unable to read POM file.");
        }

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

        Environment env = Environment.instance();
        CacheManager.INSTANCE.setUp(env.getAppName());
        ConfigManager.INSTANCE.setUp();

        List<RequestFile> file = new ArrayList<>();
        file.add(new RequestFile(jarFile, "application/zip", jarFileName, true));
        Params params = new Params(new org.bson.Document(), file);

        // Upload the jar file
        Context handler = new Context(params, env.getAppName(), SYSTEM_DB_PREFIX, DEFAULT_JOB_USER_ID);
        Plural<ModFile> result;
        try {
            result = new File().add(handler);
        } catch (BadRequestException e) {
            throw new RuntimeException("Error uploading file");
        }

        // Delete the old jar file
        DBParams condition = new DBParams(handler);
        condition.getCondition().put("name", jarFileName);
        DataRider.ride(ModCode.class).remove(condition);

        // Add a new jar file
        DBParams data = new DBParams(handler);
        data.getData().put("app", env.getAppName());
        data.getData().put("name", jarFileName);
        data.getData().put("type", "binary");
        data.getData().put("source", result.getItems().get(0).get_id().toHexString());
        DataRider.ride(ModCode.class).add(data);

        logger.debug(jarFile);
        logger.info("Uploaded successfully");
    }
}
