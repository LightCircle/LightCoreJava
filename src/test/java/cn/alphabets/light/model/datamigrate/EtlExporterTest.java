package cn.alphabets.light.model.datamigrate;

import cn.alphabets.light.Constant;
import cn.alphabets.light.Environment;
import cn.alphabets.light.cache.CacheManager;
import cn.alphabets.light.config.ConfigManager;
import cn.alphabets.light.entity.ModEtl;
import cn.alphabets.light.exception.BadRequestException;
import cn.alphabets.light.http.Context;
import cn.alphabets.light.http.Params;
import cn.alphabets.light.mock.MockRoutingContext;
import cn.alphabets.light.model.Singular;
import cn.alphabets.light.model.datarider.Rider;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * EtlExporterTest
 * Created by lilin on 2017/7/12.
 */
public class EtlExporterTest {

    private Context handler;

    @Before
    public void setUp() {
        Environment.instance().args.local = true;
        CacheManager.INSTANCE.setUp(Constant.SYSTEM_DB);
        ConfigManager.INSTANCE.setUp();
        handler = new Context(new MockRoutingContext(), Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX);
    }

    @Test
    public void testExec() throws IOException, BadRequestException {

        // 检索定义
        Params params = new Params().condition(new Document("name", "i18n-imp"));
        Singular<ModEtl> define = Rider.get(handler, ModEtl.class, params);

        // 导出
        params.condition(new Document());
        Context handler = new Context(params, Constant.SYSTEM_DB, Constant.SYSTEM_DB_PREFIX, null);
        Document result = new EtlExporter(handler, define.item).exec();
        System.out.println(result);
    }
}
