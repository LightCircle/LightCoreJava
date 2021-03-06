//package cn.alphabets.light.db.mongo;
//
//import cn.alphabets.light.Constant;
//import cn.alphabets.light.Environment;
//import cn.alphabets.light.entity.ModUser;
//import cn.alphabets.light.http.Context;
//import cn.alphabets.light.mock.MockRoutingContext;
//import cn.alphabets.light.model.Plural;
//import org.bson.Document;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//
//
///**
// * ControllerTest
// */
//public class ControllerTest {
//
//    private Context handler;
//
//    @Before
//    public void setUp() {
//        Environment.clean();
//        Environment.instance().args.local = true;
//        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
//    }
//
//    @Test
//    public void testList() {
//
//        Controller ctrl = new Controller(handler, "user");
//
//        Plural result = ctrl.list();
//
//        System.out.println(result.getTotalItems());
//    }
//
//    @Test
//    public void testAdd() {
//
//        ModUser user = new ModUser();
//        user.setName("test user");
//
//        Document json = new Document();
//        json.putAll(user.toDocument());
//
//        handler.params.setDataJson(json);
//        Controller ctrl = new Controller(handler, "user");
//
//        // add test user
//        List<ModUser> users = ctrl.add();
//        Assert.assertNotNull(users);
//        Assert.assertTrue(users.size() > 0);
//
//        // delete test user
//        handler.params.setId(users.get(0).get_id());
//        Long count = ctrl.delete();
//        Assert.assertTrue(1L == count);
//    }
//}
