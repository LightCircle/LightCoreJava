package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.entity.ModI18n;
import org.bson.Document;
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
        Environment.clean();
        Environment.instance().args.local = true;
    }

    @Test
    public void testGetBy() {

        Model model = new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX, "i18n");

        List<ModI18n> i18n = model.list();

        Assert.assertTrue(i18n.size() > 0);
    }

//    @Test
    public void testGet() {

        Model model = new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX, "i18n");

        ModI18n i18n = model.get(new Document("key", "i18n"));

        Assert.assertNotNull(i18n);
    }
}
