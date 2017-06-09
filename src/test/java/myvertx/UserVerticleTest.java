package myvertx;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UserVerticleTest {
    private static final Logger log = LoggerFactory.getLogger(Verticle1Test.class);
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) throws Exception {
        VertxOptions options = new VertxOptions();
        options.setBlockedThreadCheckInterval(Integer.MAX_VALUE);
        options.setMaxEventLoopExecuteTime(Integer.MAX_VALUE);

        Async async = context.async();
        vertx = Vertx.vertx(options);
        vertx.deployVerticle(new UserVerticle(), ar -> {
            if (ar.succeeded()) {
                async.complete();
            } else {
                ar.cause().printStackTrace();
            }
        });
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) throws Exception {
        Async async = context.async();
        vertx.createHttpClient()
                .get(8080, "127.0.0.1", "/saves", response -> {
                    response.handler(body -> {
                        String str = body.toString();
                        System.err.println(str);
                        async.complete();
                    });
                })
                .exceptionHandler(context::fail)
                .end();
    }
}
