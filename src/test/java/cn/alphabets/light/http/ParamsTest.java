package cn.alphabets.light.http;

import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by lilin on 2017/11/23.
 *
 */
public class ParamsTest {

    @Test
    public void testParams() {

        Document document = new Document();

        // 空参数
        Params params = new Params(document);

        Assert.assertEquals(0, params.getSkip());
        Assert.assertEquals(0, params.getLimit());

        // 空字符串参数
        document.put("skip", "");
        document.put("limit", "");

        params = new Params(document);

        Assert.assertEquals(0, params.getSkip());
        Assert.assertEquals(0, params.getLimit());

        // 字符串类型的skip和limit
        document.put("skip", "1");
        document.put("limit", "10");

        params = new Params(document);

        Assert.assertEquals(1, params.getSkip());
        Assert.assertEquals(10, params.getLimit());

        // 数字类型的skip和limit
        document.put("skip", 2);
        document.put("limit", 11);

        params = new Params(document);

        Assert.assertEquals(2, params.getSkip());
        Assert.assertEquals(11, params.getLimit());
    }

}
