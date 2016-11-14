package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.entity.User;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * DataRiderTest
 * Created by lilin on 2016/11/13.
 */
public class DataRiderTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testList() {

        Plural<User> result = new DataRider(User.class).list(handler);
        List<User> u = result.getItems();

        System.out.println(u.size());
    }

    @Test
    public void testGet() {

        User u = new DataRider(User.class).get(handler);

        System.out.println(u.getName());
    }

//    @Test
    public void testAdd() {

        String result = new DataRider(User.class).add(handler);

        System.out.println(result);
    }
}
