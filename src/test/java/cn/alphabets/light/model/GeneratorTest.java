package cn.alphabets.light.model;

import cn.alphabets.light.Constant;
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
                "board", "configuration", "i18n", "route", "structure", "tenant", "validator",
                "access",
                "authority",
                "role",
                "user",
                "group",
                "category",
                "file",
                "code",
                "form",
                "place",
                "function",
                "setting",
                "tag"
        );

        new Generator(pkg).generate(Constant.SYSTEM_DB, target);
    }

}
