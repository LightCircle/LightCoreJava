package cn.alphabets.light.db.mysql;

import cn.alphabets.light.Environment;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

/**
 * ModelTest
 * Created by lilin on 2017/7/16.
 */
public class ModelTest {

    @Before
    public void setUp() {
        Environment.clean();
        Environment.instance().args.local = true;
    }

    @Test
    public void test() throws SQLException {
        Model m = new Model("", "");
        List<Document> d = m.list("SELECT * FROM test where _id='<%= _id %>'", new Document("_id", "1"));

        System.out.println(d);
    }

}
