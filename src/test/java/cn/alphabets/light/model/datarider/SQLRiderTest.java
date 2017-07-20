package cn.alphabets.light.model.datarider;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModBoard;
import cn.alphabets.light.entity.ModTest;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Singular;
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

    //@Before
    public void setUp() {
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
        ConfigManager.INSTANCE.setUp();
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    //@Test
    public void testCall() {

        ModBoard board = CacheManager.INSTANCE.getBoards()
                .stream()
                .filter(item -> item.getApi().equals("/api/test/list"))
                .collect(Collectors.toList())
                .get(0);

        handler.params.condition(new Document("name", "test"));
        SQLRider.call(handler, Rider.getEntityType(board.getClass_(), board.getKind()), board.getAction());
    }

    //@Test
    public void testAdd() {

        Document data = new Document();
        data.put("name", "lalala");

        handler.params.data(data);
        SQLRider.add(handler, ModTest.class);
    }

    //@Test
    public void testUpdate() {

        Document data = new Document();
        data.put("age", "40");

        Document condition = new Document();
        condition.put("age", 0);

        handler.params.data(data).condition(condition);
        SQLRider.update(handler, ModTest.class);
    }

    //@Test
    public void testRemove() {
        handler.params.id("2");
        SQLRider.remove(handler, ModTest.class);
    }

    @Test
    public void testGet() {
        handler.params.id("2");
        Singular<ModTest> test = SQLRider.get(handler, ModTest.class);
        System.out.println(test.item);
    }

    //@Test
    public void testCount() {

        Document condition = new Document();
        condition.put("age", 40);

        handler.params.condition(condition);
        Long test = SQLRider.count(handler, ModTest.class);
    }
}
