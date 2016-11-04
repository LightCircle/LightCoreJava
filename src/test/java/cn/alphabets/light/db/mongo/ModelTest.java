package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * ModelTest
 */
public class ModelTest {

    @Before
    public void setUp() {
        Config.instance().args.local = true;
    }

    @Test
    public void test() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        Model m = new Model(Config.Constant.SYSTEM_DB, Config.Constant.SYSTEM_DB_PREFIX, "users", null);
        m.getBy((err, lookup) -> {
            Assert.assertTrue(lookup.size() > 0);
            latch.countDown();
        });

        latch.await();
    }

}
