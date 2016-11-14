package cn.alphabets.light;

import io.vertx.core.Vertx;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * AppTest
 * Created by lilin on 2016/11/8.
 */
public class AppTest {

    private static Environment env;
    private static Vertx vertx;

    @BeforeClass
    public static void setUpBeforeClass() throws IOException {

        vertx = Vertx.vertx();
        env = Environment.instance();
        env.args.local = true;

        new App().start();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        vertx.close();
    }

    @Test
    public void testHTML() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        vertx.createHttpClient().getNow(env.getAppPort(), "localhost", "/", response ->
                response.handler(body -> {
                    Assert.assertTrue(body.toString().contains("Host : 127.0.0.1"));
                    Assert.assertTrue(body.toString().contains("/Hello"));
                    Assert.assertTrue(body.toString().contains("Sub"));
                    latch.countDown();
                })
        );

        latch.await();
    }

    @Test
    public void testProcessAPI() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        vertx.createHttpClient().getNow(env.getAppPort(), "localhost", "/api/account/login", response ->
                response.handler(body -> {
                    Assert.assertTrue(body.toString().contains("OK"));
                    latch.countDown();
                })
        );

        latch.await();
    }

    @Test
    public void testDataAPI() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        vertx.createHttpClient().getNow(env.getAppPort(), "localhost", "/api/user/list", response ->
                response.handler(body -> {
                    Assert.assertTrue(body.toString().contains("user1"));
                    Assert.assertTrue(body.toString().contains("user2"));
                    latch.countDown();
                })
        );

        latch.await();
    }

    @Test
    public void testRiderAPI() throws InterruptedException {

        final CountDownLatch latch = new CountDownLatch(1);

        String id = "000000000000000000000001";
        vertx.createHttpClient().getNow(env.getAppPort(), "localhost", "/api/user/get?id=" + id, response ->
                response.handler(body -> {
                    Assert.assertTrue(body.toString().contains("admin"));
                    latch.countDown();
                })
        );

        latch.await();
    }
}
