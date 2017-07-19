package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Plural;
import org.junit.Before;
import org.junit.Test;


/**
 * ControllerTest
 */
public class ControllerTest {

    private Context handler;

//    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

//    @Test
    public void testList() {

        Params params = new Params();
        params.setScript("select * from test");
        params.setTable("test");

        Controller ctrl = new Controller(handler, params);

        Plural result = ctrl.list();
        System.out.println(result.items.size());
    }

}
