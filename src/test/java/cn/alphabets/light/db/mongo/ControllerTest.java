package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Plural;
import cn.alphabets.light.model.Json;
import cn.alphabets.light.entity.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * ControllerTest
 */
public class ControllerTest {

    private Environment env;
    private Context handler;

    @Before
    public void setUp() {
        Environment.clean();
        env = Environment.instance();
        env.args.local = true;
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testList() {

        Controller ctrl = new Controller(handler, "user");

        Plural result = ctrl.list();

        System.out.println(result.getTotalItems());
    }

//    @Test
    public void testAdd() {

        User user = new User();
        user.setName("test user");

        Json j = new Json();
        j.putAll(user.toDoc());

        handler.params.setData(j);
        Controller ctrl = new Controller(handler, "user");

        String id = ctrl.add();
        Assert.assertNotNull(id);

        System.out.println(id);
    }
}
