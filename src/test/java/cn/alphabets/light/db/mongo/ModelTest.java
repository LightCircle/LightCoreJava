package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.entity.ModI18n;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ModelTest
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTest {

    private static Document document;

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

    @Test
    public void testGet() {

        Model model = new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX, "i18n");

        ModI18n i18n = model.get(new Document("key", "i18n"));

        Assert.assertNotNull(i18n);
    }

    @Test
    public void test_AA_WriteStreamToGrid() throws IOException {

        Model model = new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("tmp/logo.png");

        document = model.writeStreamToGrid("test", inputStream, "image/png");
        Assert.assertNotNull(document);
        Assert.assertTrue(document.getLong("length") > 0);
    }

    @Test
    public void test_AB_ReadStreamFromGrid() throws IOException {

        Model model = new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);

        FileOutputStream outputStream = new FileOutputStream(document.getString("name"));
        Document output = model.readStreamFromGrid(document.getObjectId("fileId"), outputStream);
        Assert.assertNotNull(output);
        outputStream.close();

        // Delete the temporary file
        boolean delete = new File(document.getString("name")).delete();
        Assert.assertTrue(delete);
    }

    @Test
    public void test_AC_DeleteFromGrid() throws IOException {
        new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX).deleteFromGrid(document.getObjectId("fileId"));
    }

    @Test
    public void test_BA_writeFileToGrid() throws IOException {

        Model model = new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);

        document = model.writeFileToGrid("pom.xml");
        Assert.assertNotNull(document);
        Assert.assertTrue(document.getLong("length") > 0);
    }

    @Test
    public void test_BB_DeleteFromGrid() throws IOException {
        new Model(Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX).deleteFromGrid(document.getObjectId("fileId"));
    }
}
