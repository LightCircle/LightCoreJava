package cn.alphabets.light.db.mongo;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Result;
import cn.alphabets.light.model.User;
import org.junit.Before;
import org.junit.Test;


/**
 * ControllerTest
 */
public class ControllerTest {

    private Environment env;

    @Before
    public void setUp() {
        Environment.clean();
        env = Environment.instance();
        env.args.local = true;
    }

    @Test
    public void testList() {

        Context handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
        Controller ctrl = new Controller(handler, "user");

        Result<User> result = ctrl.list();
        User a = result.getItems().get(0);

        System.out.println(result.getTotalItems());
        System.out.println(a.toJson());
    }
}
