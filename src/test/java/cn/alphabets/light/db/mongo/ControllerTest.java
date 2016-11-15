package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Json;
import cn.alphabets.light.entity.ModUser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * ControllerTest
 */
public class ControllerTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testList() {

        Controller ctrl = new Controller(handler, "user");

        Plural result = ctrl.list();

        System.out.println(result.getTotalItems());
    }

    @Test
    public void testAdd() {

        ModUser user = new ModUser();
        user.setName("test user");

        Json json = new Json();
        json.putAll(user.toDoc());

        handler.params.setDataJson(json);
        Controller ctrl = new Controller(handler, "user");

        // add test user
        String id = ctrl.add();
        Assert.assertNotNull(id);

        // delete test user
        handler.params.setId(id);
        Long count = ctrl.delete();
        Assert.assertTrue(1L == count);
    }
}
