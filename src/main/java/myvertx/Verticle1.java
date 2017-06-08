package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static myvertx.Functions.getAsyncResultHandler;

class Verticle1 extends AbstractVerticle {

    final int PORT = 8080;

    final ServiceDiscoveryOptions zkConfiguration;

    Verticle1(ServiceDiscoveryOptions zkConfiguration) {
        this.zkConfiguration = zkConfiguration;
    }

    @Override
    public void start(Future<Void> future) {
        ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx, zkConfiguration);

        CompletableFuture<HttpServer> server = new CompletableFuture<>();
        vertx.createHttpServer()
                .requestHandler(r -> {
                    Optional<String> name = Optional.ofNullable(r.getParam("name"));
                    r.response().end("Hello " + name.orElse("there!"));
                })
                .listen(PORT, getAsyncResultHandler(server));

        CompletableFuture<Record> myRestApiRecord = new CompletableFuture<>();
        serviceDiscovery.publish(
                HttpEndpoint.createRecord("my-rest-api", "localhost", PORT, "/"),
                getAsyncResultHandler(myRestApiRecord));

        CompletableFuture.allOf(server, myRestApiRecord)
                .thenRun(future::complete)
                .exceptionally(th -> {
                    future.fail(th);
                    return null;
                });
    }
}
