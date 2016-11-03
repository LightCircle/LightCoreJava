package cn.alphabets.light;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Config Test
 * Created by lilin on 2016/11/3.
 */
public class ConfigTest {

    @Test
    public void test() {

        Config config = Config.instance();

        Assert.assertEquals(config.app.domain, "LightDB");
        Assert.assertEquals(config.app.port, 7000);

        Optional<Config.ConfigMongoDB> host = Optional.ofNullable(config.mongodb);
        host.ifPresent(mongo -> Assert.assertEquals(mongo.host, "db.alphabets.cn"));
    }

}
