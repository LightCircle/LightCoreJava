package cn.alphabets.light;

import org.junit.Assert;
import org.junit.Test;

/**
 * Config Test
 * Created by lilin on 2016/11/3.
 */
public class environmentTest {

    @Test
    public void testInstance() {

        Environment environment = Environment.instance();
        environment.args.local = true;

        Assert.assertEquals(environment.getAppName(), "LightDB");
        Assert.assertEquals(environment.getAppPort(), 7000);
        Assert.assertEquals(environment.getMongoHost(), "127.0.0.1");
    }

}
