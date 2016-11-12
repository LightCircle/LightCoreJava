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
public class ModeGeneratorTest {

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
    }

    @Test
    public void testGenerateEntity() throws IOException {

        String pkg = Environment.instance().getPackages() + ".entity";
        List<String> target = Arrays.asList(
                "board", "configuration", "i18n", "route", "structure", "tenant", "user", "validator"
        );

        new ModelGenerator(pkg).generate(Constant.SYSTEM_DB, target);
    }

//    @Test
    public void testGenerateConfig() throws IOException {
        ConfigGenerator generator = new ConfigGenerator("cn.alphabets.light.config", "Configuration");

        generator.generate(Constant.SYSTEM_DB);
//        TypeSpec.Builder b = generator.subClass_(null, "aaa");
//        System.out.println(b.build().toString());

        generator.write();

//        String i = new Config().app.getHost();
    }
}
