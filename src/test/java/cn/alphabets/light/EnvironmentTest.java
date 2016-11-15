package cn.alphabets.light;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Config Test
 * Created by lilin on 2016/11/3.
 */
public class EnvironmentTest {

    @Test
    public void testInstance() {

        Helper.setEnv(new HashMap<String, String>(){{
            put(Constant.ENV_LIGHT_APP_NAME, "DOMAIN");
            put(Constant.ENV_LIGHT_APP_PORT, "8000");
            put(Constant.ENV_LIGHT_MONGO_HOST, "localhost");
        }});

        Environment.clean();
        Environment env = Environment.instance();
        Assert.assertEquals(env.getAppName(), "DOMAIN");
        Assert.assertEquals(env.getAppPort(), 8000);
        Assert.assertEquals(env.getMongoHost(), "localhost");

        String path = System.getProperty("user.dir") + "/src/test/resources/config.yml";
        Environment.clean();
        env = Environment.instance(path);
        env.args.local = true;

        Assert.assertEquals(env.getAppName(), "LightDB");
        Assert.assertEquals(env.getAppPort(), 7001);
        Assert.assertEquals(env.getMongoHost(), "127.0.0.1");

        Assert.assertEquals(env.getMySQLHost(), "db.alphabets.cn");
        Assert.assertEquals(env.getMySQLPort(), "55017");
    }

    @Test
    public void testInitArgs() {

        Environment env = Environment.instance();
        String[] args = new String[]{"-local", "-dump", "-push", "-restore"};
        env.args.initArgs(args);

        Assert.assertTrue(env.args.local);
        Assert.assertTrue(env.args.dump);
        Assert.assertTrue(env.args.push);
        Assert.assertTrue(env.args.restore);
    }

}
