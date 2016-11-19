package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * FileTest
 * Created by lilin on 2016/11/18.
 */
public class FileTest {

    Context handler;

//    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

//    @Test
    public void testAdd() {

        List<Document> files = new ArrayList<>();

        Document file = new Document();
        file.put(Constant.PARAM_FILE_NAME, "pom.xml");
        file.put(Constant.PARAM_FILE_TYPE, "application/xml");
        file.put(Constant.PARAM_FILE_PHYSICAL, "pom.xml");

        files.add(file);
        handler.params.setFiles(files);
        new File().add(handler);
    }

}
