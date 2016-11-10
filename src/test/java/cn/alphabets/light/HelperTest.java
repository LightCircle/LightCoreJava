package cn.alphabets.light;

import cn.alphabets.light.model.Json;
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
    public void testUnParam() {

        String slimjson = "[ \"]*";
        Json source = Helper.unParam("s=0&c[m][]=a&c[m][]=b&c[p][p1][p11]=d&c[p][p1][p12][]=1&c[p][p1][p12][]=2");
        String json = source.toJson().replaceAll(slimjson, "");

        Assert.assertEquals(json, "{s:0,c:{m:[a,b],p:{p1:{p11:d,p12:[1,2]}}}}");
    }

    @Test
    public void testLookup() {

        String slimjson = "[ \"]*";

        Json source = new Json();
        Helper.setValueByJsonPath(source, Arrays.asList("key", ""), 1);
        Helper.setValueByJsonPath(source, Arrays.asList("key", ""), 2);
        Assert.assertEquals(source.toJson().replaceAll(slimjson, ""), "{key:[1,2]}");

        source = new Json();
        Helper.setValueByJsonPath(source, Arrays.asList("key", "son"), 1);
        Assert.assertEquals(source.toJson().replaceAll(slimjson, ""), "{key:{son:1}}");

        source = new Json();
        Helper.setValueByJsonPath(source, Arrays.asList("key", "0", "son"), "value1");
        Helper.setValueByJsonPath(source, Arrays.asList("key", "1", "son"), "value2");
        Assert.assertEquals(source.toJson().replaceAll(slimjson, ""), "{key:[{son:value1},{son:value2}]}");
    }

    @Test
    public void testLoadTemplate() throws IOException {

        Helper.TemplateFunction dynamic = new Helper.TemplateFunction("dynamic", (x) -> x.get(0) + " : dynamic");
        Helper.TemplateFunction i = new Helper.TemplateFunction("i", (x) -> x.get(0) + " : i");

        Map<String, Object> map = new ConcurrentHashMap<String, Object>() {{
            put("conf", Config.instance());
            put("state", Boolean.TRUE);
        }};

        Helper.cleanTemplate();
        String result = Helper.loadTemplate("view/accounts.html", map, Arrays.asList(dynamic, i));
        Assert.assertTrue(result.contains("Host : 127.0.0.1"));
        Assert.assertTrue(result.contains("Hello : dynamic"));
        Assert.assertTrue(result.contains("Hello : i"));
        Assert.assertTrue(result.contains("Sub"));
    }

}
