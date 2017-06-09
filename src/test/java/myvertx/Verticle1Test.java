package myvertx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CompletableFuture;

import static myvertx.Functions.getAsyncResultHandler;

@RunWith(VertxUnitRunner.class)
public class Verticle1Test {

    private static final Logger log = LoggerFactory.getLogger(Verticle1Test.class);

    private Vertx vertx;

    private TestingServer testingServer;

    private final ServiceDiscoveryOptions zkConfiguration = new ServiceDiscoveryOptions()
            .setBackendConfiguration(new JsonObject().put("connection", "127.0.0.1:4296"));

    @Before
    public void setUp(TestContext context) throws Exception {
        Async async = context.async();
        vertx = Vertx.vertx();
        testingServer = new TestingServer(4296);
        vertx.deployVerticle(new Verticle1(zkConfiguration), ar -> {
            if (ar.succeeded()) {
                log.info("finished setup");
                async.complete();
            }
        });
    }

    @After
    public void dest() throws Exception {
        testingServer.close();
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) throws Exception {
        log.info("begin test");

        ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx, zkConfiguration);

        /*
            Creating an Async object with the async method marks the executed test case as non terminated.
            The test case terminates when the complete method is invoked.
            When the complete callback is not invoked, the test case fails after a certain timeout.
         */
        Async async = context.async();
        CompletableFuture<Record> record = new CompletableFuture<>();
        serviceDiscovery.getRecord(new JsonObject().put("name", "my-rest-api"), getAsyncResultHandler(record));

        record.thenApply(serviceDiscovery::getReference)
                .thenApply(ref -> ref.getAs(HttpClient.class))
                .whenComplete((it, th) -> System.out.println(it))
                .thenAccept(client ->
                        client.get("/", response -> {
                            response.handler(body -> {
                                String str = body.toString();
                                System.out.println(str);
                                context.assertTrue(StringUtils.containsIgnoreCase(str, "hello"));
                                async.complete();
                            });
                        }).end())
                .exceptionally(th -> {
                    context.fail(th);
                    return null;
                });
    }
}
