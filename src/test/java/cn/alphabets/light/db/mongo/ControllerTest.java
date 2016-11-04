package cn.alphabets.light.db.mongo;

import cn.alphabets.light.model.ModUser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * ControllerTest
 */
public class ControllerTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void test() {
        Controller ctrl = new Controller("user");

        List<ModUser> u = ctrl.list(ModUser.class);
    }
}
