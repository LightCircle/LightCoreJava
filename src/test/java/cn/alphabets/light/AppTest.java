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

    private Environment env;
    private Vertx vertx;

    @Before
    public void setUp() throws IOException {

        vertx = Vertx.vertx();
        env = Environment.instance();
        env.args.local = true;

        new App().start();
    }

    @After
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void testMyApplication() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        vertx.createHttpClient().getNow(env.getAppPort(), "localhost", "/", response -> {
            response.handler(body -> {
                System.out.println(body.toString());
                latch.countDown();
            });
        });

        latch.await();
    }
}
