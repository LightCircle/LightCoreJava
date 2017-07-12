package cn.alphabets.light.validator;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * RuleTest
 * Created by lilin on 2017/7/11.
 */
public class MPathTest {

    @Test
    public void testDetectValue() {

        Object result;
        Document c = new Document("c", "hello");
        Document b = new Document("b", c);
        Document a = new Document("a", b);

        // 文档类型的数据
        result = MPath.detectValue("null", a);
        Assert.assertNull(result);

        result = MPath.detectValue("a", a);
        Assert.assertEquals(result, b);

        result = MPath.detectValue("a.b", a);
        Assert.assertEquals(result, c);

        result = MPath.detectValue("a.b.c", a);
        Assert.assertEquals(result, "hello");

        // 列表嵌套文档的数据
        List<Document> list = new ArrayList<>();
        list.add(a);
        list.add(a);

        result = MPath.detectValue("a", list);
        Assert.assertEquals(((List)result).get(0), b);

        result = MPath.detectValue("a.b", list);
        Assert.assertEquals(((List)result).get(0), c);

        result = MPath.detectValue("1.a.b", list);
        Assert.assertEquals(result, c);

        // 文档嵌套列表的数据
        list = new ArrayList<>();
        list.add(b);
        list.add(b);
        a = new Document("a", list);

        result = MPath.detectValue("a.b", a);
        Assert.assertEquals(((List)result).get(0), c);

        result = MPath.detectValue("a.1.b", a);
        Assert.assertEquals(result, c);

    }
}
