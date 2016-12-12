//package cn.alphabets.light.model;
//
//import cn.alphabets.light.Constant;
//import cn.alphabets.light.Environment;
//import cn.alphabets.light.entity.ModFile;
//import cn.alphabets.light.http.Context;
//import cn.alphabets.light.mock.MockRoutingContext;
//import org.bson.Document;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * FileTest
// * Created by lilin on 2016/11/18.
// */
//public class FileTest {
//
//    private Context handler;
//    private static String id;
//
//    @Before
//    public void setUp() {
//        Environment.clean();
//        Environment.instance().args.local = true;
//        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
//    }
//
//    @Test
//    public void testAdd() {
//
//        List<Document> files = new ArrayList<>();
//
//        Document file = new Document();
//        file.put(Constant.PARAM_FILE_NAME, "pom.xml");
//        file.put(Constant.PARAM_FILE_TYPE, "application/xml");
//        file.put(Constant.PARAM_FILE_PHYSICAL, "pom.xml");
//
//        files.add(file);
//        handler.params.setFiles(files);
//
//        Plural<ModFile> result = new File().add(handler);
//        Assert.assertTrue(result.getTotalItems() == 1);
//
//        id = result.getItems().get(0).get_id().toHexString();
//    }
//
//    @Test
//    public void testRemove() {
//        handler.params.setId(id);
//        new File().delete(handler);
//    }
//
//}
