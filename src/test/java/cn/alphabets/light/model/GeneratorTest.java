package cn.alphabets.light.model;

import cn.alphabets.light.Environment;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * GeneratorTest
 * Created by lilin on 2016/11/8.
 */
public class GeneratorTest {

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
    }

    @Test
    public void testGenerateEntity() throws IOException {

        String pkg = Environment.instance().getPackages() + ".entity";
        List<String> target = Arrays.asList(
                "access",
                "authority",
                "board",
                "category",
                "code",
                "configuration",
                "counter",
                "etl",
                "file",
                "form",
                "function",
                "group",
                "i18n",
                "job",
                "log",
                "markdown",
                "place",
                "role",
                "route",
                "setting",
                "structure",
                "tag",
                "tenant",
                "user",
                "validator"
        );

        new Generator(pkg).generate(Environment.instance().getAppName(), target);
    }

}
