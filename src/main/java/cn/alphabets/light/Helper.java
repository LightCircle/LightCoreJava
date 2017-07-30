package cn.alphabets.light;

import cn.alphabets.light.validator.MPath;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.bson.Document;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONObject;
import org.json.XML;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.environment.EnvironmentFactory;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;
import org.jtwig.resource.reference.ResourceReference;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Helper
 */
public class Helper {

    public static final SimpleDateFormat ISO_FORMATTER;

    static {
        ISO_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        ISO_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static String fileMD5(String file) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return DigestUtils.md5Hex(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public static String randomGUID4() {
        return RandomStringUtils.randomAlphanumeric(4);
    }

    public static String randomGUID8() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    public static String randomGUID12() {
        return RandomStringUtils.randomAlphanumeric(12);
    }

    public static org.w3c.dom.Document stringToXml(String xml) {
        try {
            return DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document xmlToJSON(String xml) {
        return Document.parse(XML.toJSONObject(xml).toString());
    }

    public static String jsonToXML(Document doc) {
        // If you use an empty array, the tags are missing
        return XML.toString(new JSONObject(doc.toJson()));
    }

    public static Model getPOM(String path) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read(new FileReader(path));
        } catch (XmlPullParserException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toUTCString(Date date) {
        return ISO_FORMATTER.format(date);
    }

    public static Date fromUTCString(String date) {
        try {
            return ISO_FORMATTER.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date fromSupportedString(String date, TimeZone timeZone) throws Exception {

        HashMap<Pattern, BiFunction<String, TimeZone, Date>> patternFormatMap = new LinkedHashMap<Pattern, BiFunction<String, TimeZone, Date>>() {
            {
                put(Pattern.compile("^\\d{4}/\\d{2}/\\d{2}$")
                        , (s, tz) -> {
                            DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
                            format.setTimeZone(tz);
                            try {
                                return format.parse(s);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        });

                put(Pattern.compile("^\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}$")
                        , (s, tz) -> {
                            DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            format.setTimeZone(tz);
                            try {
                                return format.parse(s);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        });

                put(Pattern.compile("^\\d{4}/\\d{2}/\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")
                        , (s, tz) -> {
                            DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
                            format.setTimeZone(tz);
                            try {
                                return format.parse(s);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        };

        Iterator<Map.Entry<Pattern, BiFunction<String, TimeZone, Date>>> iterator = patternFormatMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Pattern, BiFunction<String, TimeZone, Date>> entry = iterator.next();
            if (entry.getKey().matcher(date).find()) {
                return entry.getValue().apply(date, timeZone);
            }
        }
        throw new RuntimeException("time format is unsupported");
    }

    /**
     * Speculate Content-Type from a file
     *
     * @param stream file stream
     * @return content type
     */
    public static String getContentType(InputStream stream) {

        Metadata metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        try {
            parser.parse(stream, new BodyContentHandler(), metadata, new ParseContext());
        } catch (IOException | SAXException | TikaException e) {
            return null;
        }

        return metadata.get(Metadata.CONTENT_TYPE);
    }

    /**
     * Set the system environment variables
     *
     * @param newEnv environment
     */
    @SuppressWarnings("unchecked")
    public static void setEnv(Map<String, String> newEnv) {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class cl : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                try {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Map<String, String> map = (Map<String, String>) field.get(env);
                    map.clear();
                    map.putAll(newEnv);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void setEnv(String key, String value) {
        Map<String, String> env = new ConcurrentHashMap<String, String>() {{
            put(key, value);
        }};

        Helper.setEnv(env);
    }

    /**
     * Determines whether the current runtime environment is JUnit
     *
     * @return true is JUnit
     */
    public static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether is debug run
     *
     * @return true is JUnit
     */
    public static boolean isIdeDebug() {
        List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
        boolean isDebug = false;
        for (String arg : args) {
            if (arg.startsWith("-Xrunjdwp") || arg.startsWith("-agentlib:jdwp")) {
                isDebug = true;
                break;
            }
        }
        return isDebug;
    }

    /**
     * Determine whether the request comes to the browser
     *
     * @param request request
     * @return true is browser
     */
    public static boolean isBrowser(HttpServerRequest request) {

        String ua = request.getHeader("user-agent");
        return ua != null && ua.toLowerCase().matches("mozilla.*");
    }

    /**
     * Deserializes the JSON generated by JQuery.param() method
     *
     * @param url full request url
     * @return Json Object
     */
    public static Document unParam(String url) {

        String decoded;
        Pattern pattern = Pattern.compile("\\[([^\\]]*)\\]");

        try {
            if (url.contains("?")) {
                url = url.substring(url.indexOf("?") + 1);
            } else {
                return new Document();
            }
            decoded = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        final Document json = new Document();

        Arrays.stream(decoded.split("&")).forEach(keyVal -> {

            String[] splitted = keyVal.split("=");
            String key = splitted[0];
            String val = splitted.length > 1 ? splitted[1] : "";

            List<String> path = new ArrayList<>();
            path.add(key.indexOf("[") > 0 ? key.substring(0, key.indexOf("[")) : key);

            Matcher m = pattern.matcher(key);
            while (m.find()) {
                path.add(m.group(1));
            }

            MPath.setValueByJsonPath(json, path, val);
        });

        return json;
    }

    /**
     * Use the template file to generate a string
     *
     * @param name     resource name
     * @param model    parameters
     * @param function custom functions
     * @return The resulting string
     */
    public static String loadTemplate(String name, Map<String, Object> model, List<SimpleJtwigFunction> function) {

        EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder
                .configuration()
                .render()
                .withOutputCharset(Charset.forName("UTF-8"))
                .and()
                .resources()
                .withDefaultInputCharset(Charset.forName("UTF-8"))
                .and()
                .functions()
                .add(function)
                .and()
                .parser()
                .syntax()
                .withStartCode("<%~").withEndCode("%>")
                .withStartOutput("<%=").withEndOutput("%>")
                .withStartComment("<#").withEndComment("#>")
                .and()
                .and()
                .build();

        org.jtwig.environment.Environment environment = new EnvironmentFactory().create(configuration);
        ResourceReference resource = new ResourceReference(ResourceReference.CLASSPATH, name);

        return new JtwigTemplate(environment, resource).render(JtwigModel.newModel(model));
    }


    public static String loadInlineTemplate(String template, Map<String, Object> model) {
        List<SimpleJtwigFunction> function = new ArrayList<>();
        return loadInlineTemplate(template, model, function);
    }
    public static String loadInlineTemplate(String template, Map<String, Object> model, List<SimpleJtwigFunction> function) {

        EnvironmentConfiguration configuration = EnvironmentConfigurationBuilder
                .configuration()
                .render()
                .withOutputCharset(Charset.forName("UTF-8"))
                .and()
                .resources()
                .withDefaultInputCharset(Charset.forName("UTF-8"))
                .and()
                .functions()
                .add(function)
                .and()
                .parser()
                .syntax()
                .withStartCode("<%~").withEndCode("%>")
                .withStartOutput("<%=").withEndOutput("%>")
                .withStartComment("<#").withEndComment("#>")
                .and()
                .and()
                .build();

        return JtwigTemplate.inlineTemplate(template, configuration).render(JtwigModel.newModel(model));
    }

    public static class StringFunction extends SimpleJtwigFunction {

        private String name;
        Function<List<Object>, String> function;

        public StringFunction(String name, Function<List<Object>, String> function) {
            this.name = name;
            this.function = function;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Object execute(FunctionRequest request) {
            return this.function.apply(request.getArguments());
        }
    }

    public static class ListFunction extends SimpleJtwigFunction {

        private String name;
        Function<List<Object>, List<String>> function;

        public ListFunction(String name, Function<List<Object>, List<String>> function) {
            this.name = name;
            this.function = function;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Object execute(FunctionRequest request) {
            return this.function.apply(request.getArguments());
        }
    }

    public static class MapFunction extends SimpleJtwigFunction {

        private String name;
        Function<List<Object>, Map<String, String>> function;

        public MapFunction(String name, Function<List<Object>, Map<String, String>> function) {
            this.name = name;
            this.function = function;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Object execute(FunctionRequest request) {
            return this.function.apply(request.getArguments());
        }
    }
}
