package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * I18NTest
 * Created by lilin on 2016/11/16.
 */
public class I18NTest {

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
    }

    @Test
    public void testI() {

        String lang = I18N.i("en", "common.button.save");
        Assert.assertEquals("Save", lang);

        lang = I18N.i("zh", "Hello");
        Assert.assertEquals("Hello", lang);
    }

    @Test
    public void testCatalog() {

        Map<String, String> lang = I18N.catalog("en", "common");
        Assert.assertTrue(lang.size() > 0);
    }
}
