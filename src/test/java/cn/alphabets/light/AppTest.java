package cn.alphabets.light;

import cn.alphabets.light.cache.CacheManager;
import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * AppTest
 * Created by lilin on 2016/11/8.
 */
public class AppTest {

    private Config conf;
    private Vertx vertx;

    @Before
    public void setUp() throws IOException {

        vertx = Vertx.vertx();
        conf = Config.instance();
        conf.args.local = true;
        CacheManager.INSTANCE.setUp(conf.app.domain);

        new App(new AppOptions()
                .setAppDomain(conf.app.domain)
                .setAppPort(conf.app.port)
                .setDbHost(conf.mongodb.host)
                .setDbPort(conf.mongodb.port)
                .setDbUser(conf.mongodb.user)
                .setDbPass(conf.mongodb.pass)
                .setDev(true)
                .setPackageNmae("test"))
                .start();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void testMyApplication() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        vertx.createHttpClient().getNow(conf.app.port, "localhost", "/", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                latch.countDown();
            });
        });

        latch.await();
    }
}
