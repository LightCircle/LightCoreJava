package cn.alphabets.light.model.datarider;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModTest;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * DataRiderTest
 * Created by lilin on 2016/11/13.
 */
public class RiderTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
        ConfigManager.INSTANCE.setUp();
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testList() {
        handler.params.condition(new Document("name", "lalala2"));
        Rider.list(handler, ModTest.class);
    }
}
