package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Config;
import cn.alphabets.light.model.ModI18n;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * ModelTest
 */
public class ModelTest {

    @Before
    public void setUp() {
        Config.instance().args.local = true;
    }

    @Test
    public void testGetBy() {

        Model model = new Model(Config.Constant.SYSTEM_DB, Config.Constant.SYSTEM_DB_PREFIX, "i18n");

        List<ModI18n> i18n = model.list(ModI18n.class);

        Assert.assertTrue(i18n.size() > 0);
    }

    @Test
    public void testGet() {

        Model model = new Model(Config.Constant.SYSTEM_DB, Config.Constant.SYSTEM_DB_PREFIX, "i18n");

        ModI18n i18n = model.get(ModI18n.class);

        Assert.assertNotNull(i18n);
    }
}
