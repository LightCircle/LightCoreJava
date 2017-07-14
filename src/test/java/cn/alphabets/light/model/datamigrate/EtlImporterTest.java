package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Singular;
import cn.alphabets.light.model.datarider.Rider;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * EtlImporterTest
 * Created by lilin on 2017/7/12.
 */
public class EtlImporterTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testExec() throws IOException {

        Params params = new Params().condition(new Document("name", "i18n-imp"));
        Singular<ModEtl> define = Rider.get(handler, ModEtl.class, params);

        List<RequestFile> files = new ArrayList<>();
        files.add(new RequestFile(
                "/Users/lilin/Desktop/ExcelTest.xlsx",
                "",
                "",
                true));

        params = new Params(new Document(), files);
        Context handler = new Context(params, Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX, null);

        Document result = new EtlImporter(handler, define.item).exec();
        System.out.println(result);
    }
}
