package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.http.RequestFile;
import cn.alphabets.light.mock.MockRoutingContext;
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

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
    }

    @Test
    public void testExec() throws IOException {

        Document options = new Document();
        options.put("type", "excel");
        options.put("class", "etl");
        options.put("mappings", ExcelTest.getMockMappings());

        List<RequestFile> files = new ArrayList<>();
        files.add(new RequestFile(
                "src/test/resources/ExcelTest.xlsx",
                "",
                "",
                true));

        Document json = new Document();
        Context handler = new Context(new Params(json, files), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX, null);

        new EtlImporter(handler, options).exec();

    }
}
