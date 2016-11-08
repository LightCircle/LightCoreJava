package cn.alphabets.light.cache;

import cn.alphabets.light.Config;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CacheManagerTest
 */
public class CacheManagerTest {

    @Before
    public void setUp() {
        Config.instance().args.local = true;
    }

    @Test
    public void test() {
        CacheManager.INSTANCE.setUp(Config.Constant.SYSTEM_DB);

        Assert.assertTrue(CacheManager.INSTANCE.getI18ns().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getTenants().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getValidators().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getStructures().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getBoards().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getRoutes().size() > 0);
    }
}
