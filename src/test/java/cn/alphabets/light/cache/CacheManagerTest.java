package cn.alphabets.light.cache;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CacheManagerTest
 */
public class CacheManagerTest {

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
    }

    //@Test
    public void test() {
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);

        Assert.assertTrue(CacheManager.INSTANCE.getI18ns().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getTenants().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getValidators().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getStructures().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getBoards().size() > 0);
        Assert.assertTrue(CacheManager.INSTANCE.getRoutes().size() > 0);
    }

    @Test
    public void testLoadFromFile() {
        CacheManager.INSTANCE.loadFromFile();
    }
}
