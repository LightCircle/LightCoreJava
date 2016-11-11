package cn.alphabets.light.model;

import cn.alphabets.light.Config;
//import cn.alphabets.light.config.Configuration;
import cn.alphabets.light.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * GeneratorTest
 * Created by lilin on 2016/11/8.
 */
public class ModeGeneratorTest {

    @Before
    public void setUp() {
        Config.instance().args.local = true;
    }

    @Test
    public void test() throws IOException {

        String pkg = "cn.alphabets.light.model";

        ModelGenerator generator = new ModelGenerator(pkg);

        generator.generate(Config.Constant.SYSTEM_DB, Arrays.asList("board"));

    }

//    @Test
    public void testGenerateConfig() throws IOException {
        ConfigGenerator generator = new ConfigGenerator("cn.alphabets.light.config", "Configuration");

        generator.generate(Config.Constant.SYSTEM_DB);
//        TypeSpec.Builder b = generator.subClass_(null, "aaa");
//        System.out.println(b.build().toString());

        generator.write();

        String i = new Configuration().app.getHost();
    }
}
