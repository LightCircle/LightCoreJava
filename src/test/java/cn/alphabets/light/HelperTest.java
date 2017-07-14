package cn.alphabets.light;

import cn.alphabets.light.validator.MPath;
import org.bson.Document;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HelperTest
 */
public class HelperTest {

    @Test
    public void testXmlToJSON() {
        String xml = "" +
            "<NULL>null</NULL>" +
            "<ARRAY><EMPTY/></ARRAY><ARRAY><EMPTY/></ARRAY>" +
            "<STRING>A</STRING>" +
            "<OBJECT><NUMBER>1</NUMBER></OBJECT>";

        Document doc = Helper.xmlToJSON(xml);

        Assert.assertNull(doc.get("NULL"));
        Assert.assertEquals(doc.get("STRING"), "A");
        Assert.assertEquals(((List<Document>)doc.get("ARRAY")).get(0).get("EMPTY"), "");
        Assert.assertEquals(((Document)doc.get("OBJECT")).getInteger("NUMBER"), new Integer(1));
    }

    @Test
    public void testJsonToXML() {

        Document sub = new Document();
        sub.put("NUMBER", 1);

        Document arr1 = new Document();
        arr1.put("EMPTY", "");
        Document arr2 = new Document();
        arr2.put("EMPTY", "");

        Document doc = new Document();
        doc.put("STRING", "A");
        doc.put("NULL", null);
        doc.put("OBJECT", sub);
        doc.put("ARRAY", Arrays.asList(arr1, arr2));
        doc.put("EMPTY_DOCUMENT", new Document());

        List<String> emptyArray = new ArrayList<>();
        doc.put("EMPTY_ARRAY", emptyArray);

        String xml = Helper.jsonToXML(doc);
        Assert.assertEquals("" +
            "<NULL>null</NULL>" +
            "<ARRAY><EMPTY/></ARRAY><ARRAY><EMPTY/></ARRAY>" +
            "<EMPTY_DOCUMENT></EMPTY_DOCUMENT>" +
            "<STRING>A</STRING>" +
            "<OBJECT><NUMBER>1</NUMBER></OBJECT>", xml);
    }

    @Test
    public void testToUTCString() {
        String utc = Helper.toUTCString(new Date());
        Assert.assertTrue(utc.length() == 24);
        Assert.assertEquals("T", utc.substring(10, 11));
        Assert.assertEquals("Z", utc.substring(23));
    }

    @Test
    public void testFromSupportedString() throws Exception {
        String case1 = "2016/12/23";
        String case2 = "2015/12/23 09:32:33";
        String case3 = "2015/12/23 09:32:33.111";
        Date date1 = Helper.fromSupportedString(case1, TimeZone.getTimeZone("GMT+8"));
        Date date2 = Helper.fromSupportedString(case2, TimeZone.getTimeZone("GMT+8"));
        Date date3 = Helper.fromSupportedString(case3, TimeZone.getTimeZone("GMT+9"));
        Assert.assertEquals("Fri Dec 23 00:00:00 CST 2016",date1.toString());
        Assert.assertEquals("Wed Dec 23 09:32:33 CST 2015",date2.toString());
        Assert.assertEquals("Wed Dec 23 08:32:33 CST 2015",date3.toString());
    }
    @Test
    public void testGetMimeType() throws FileNotFoundException {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream;

        inputStream = classLoader.getResourceAsStream("tmp/logo.png");
        Assert.assertEquals("image/png", Helper.getContentType(inputStream));

        inputStream = classLoader.getResourceAsStream("views/accounts.html");
        Assert.assertEquals("text/html", Helper.getContentType(inputStream));

        inputStream = classLoader.getResourceAsStream("config.yml");
        Assert.assertEquals("text/plain", Helper.getContentType(inputStream));

        inputStream = classLoader.getResourceAsStream("log4j2.xml");
        Assert.assertEquals("application/xml", Helper.getContentType(inputStream));
    }

    @Test
    public void testSetEnv() {
        Map<String, String> env = new ConcurrentHashMap<String, String>() {{
            put("key1", "val1");
            put("key2", "val2");
        }};
        Helper.setEnv(env);

        Assert.assertEquals(System.getenv("key1"), "val1");
        Assert.assertEquals(System.getenv("key2"), "val2");
    }

    @Test
    public void testUnParam() {

        String slimjson = "[ \"]*";
        Document source = Helper.unParam("s=0&c[m][]=a&c[m][]=b&c[p][p1][p11]=d&c[p][p1][p12][]=1&c[p][p1][p12][]=2");
        String json = source.toJson().replaceAll(slimjson, "");

        Assert.assertEquals(json, "{s:0,c:{m:[a,b],p:{p1:{p11:d,p12:[1,2]}}}}");
    }

    @Test
    public void testLookup() {

        String slimjson = "[ \"]*";

        Document source = new Document();
        MPath.setValueByJsonPath(source, Arrays.asList("key", ""), 1);
        MPath.setValueByJsonPath(source, Arrays.asList("key", ""), 2);
        Assert.assertEquals(source.toJson().replaceAll(slimjson, ""), "{key:[1,2]}");

        source = new Document();
        MPath.setValueByJsonPath(source, Arrays.asList("key", "son"), 1);
        Assert.assertEquals(source.toJson().replaceAll(slimjson, ""), "{key:{son:1}}");

        source = new Document();
        MPath.setValueByJsonPath(source, Arrays.asList("key", "0", "son"), "value1");
        MPath.setValueByJsonPath(source, Arrays.asList("key", "1", "son"), "value2");
        Assert.assertEquals(source.toJson().replaceAll(slimjson, ""), "{key:[{son:value1},{son:value2}]}");
    }

    @Test
    public void testLoadTemplate() throws IOException {

        Helper.StringFunction dynamic = new Helper.StringFunction("dynamic", (x) -> x.get(0) + " : dynamic");
        Helper.StringFunction i = new Helper.StringFunction("i", (x) -> x.get(0) + " : i");

        Map<String, Object> map = new ConcurrentHashMap<String, Object>() {{
            put("conf", Environment.instance());
            put("state", Boolean.TRUE);
        }};

        String result = Helper.loadTemplate("views/accounts.html", map, Arrays.asList(dynamic, i));
        Assert.assertTrue(result.contains("Host : 127.0.0.1"));
        Assert.assertTrue(result.contains("Hello : dynamic"));
        Assert.assertTrue(result.contains("Sub"));
    }

}
