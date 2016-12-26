package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by luohao on 2016/12/26.
 */
public class IncrementTest {

    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
    }

    @Test
    public void testIncrease() {

        String type = new ObjectId().toHexString();

        for (int i = 0; i < 10; i++) {
            long result = Increment.increase(Environment.instance().getAppName(), Constant.DEFAULT_TENANT, type);
            Assert.assertEquals(i + 1, result);
        }

    }
}
