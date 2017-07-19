package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.datarider.Rider;
import cn.alphabets.light.model.datarider.SQLRider;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;


/**
 * SQLRiderTest
 * Created by lilin on 2016/11/13.
 */
public class SQLRiderTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testCall() {

        ModBoard board = CacheManager.INSTANCE.getBoards()
                .stream()
                .filter(item -> item.getApi().equals("/api/test/list"))
                .collect(Collectors.toList())
                .get(0);

        handler.params.condition(new Document("name", "test"));
        SQLRider.call(handler, Rider.getEntityType(board.getClass_(), board.getKind()), board.getAction());
    }
}
