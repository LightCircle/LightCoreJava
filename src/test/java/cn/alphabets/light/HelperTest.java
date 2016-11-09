package cn.alphabets.light;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HelperTest
 */
public class HelperTest {

    @Test
    public void testLoadTemplate() throws IOException {

        Helper.TemplateFunction dynamic = new Helper.TemplateFunction("dynamic", (x) -> x.get(0) + " : dynamic");
        Helper.TemplateFunction i = new Helper.TemplateFunction("i", (x) -> x.get(0) + " : i");

        Map<String, Object> map = new ConcurrentHashMap<String, Object>() {{
            put("conf", Config.instance());
            put("state", Boolean.TRUE);
        }};

        String result = Helper.loadTemplate("view/account/login.html", map, Arrays.asList(dynamic, i));
        Assert.assertTrue(result.contains("Host : 127.0.0.1"));
        Assert.assertTrue(result.contains("Hello : dynamic"));
        Assert.assertTrue(result.contains("Hello : i"));
        Assert.assertTrue(result.contains("Sub"));
    }

}
