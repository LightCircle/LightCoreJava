package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModFile;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Singular;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * ControllerTest
 */
public class ControllerTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
        ConfigManager.INSTANCE.setUp();
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    //    @Test
    public void testList() {

        Params params = new Params();
        params.script("select * from test");
        params.setTable("test");

        Controller ctrl = new Controller(handler, params);

        Plural result = ctrl.list();
        System.out.println(result.items.size());
    }

    //@Test
    public void testWriteFile() {

        Controller ctrl = new Controller(handler, new Params());

        RequestFile file = new RequestFile(
                "/data/app.jar",
                "application/zip",
                "/app.jar",
                true);
        Singular<ModFile> result = ctrl.writeFile(file);

        System.out.println(result.item);
    }

    //@Test
    public void testReadFile() throws IOException {

        Controller ctrl = new Controller(handler, new Params().id("597df22c431e06b18ed8e6cc"));
        ByteArrayOutputStream stream = ctrl.readFile();

        try (OutputStream outputStream = new FileOutputStream("/data/new.jar")) {
            stream.writeTo(outputStream);
        } finally {
            stream.close();
        }
    }
}
