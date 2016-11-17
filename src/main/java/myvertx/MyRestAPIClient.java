package myvertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.concurrent.TimeUnit;

class MyRestAPIClient {

    private HttpClient client;

    MyRestAPIClient(ServiceDiscovery discovery,
                    Handler<AsyncResult<Void>> completionHandler) {
        HttpEndpoint.getClient(discovery,
                new JsonObject().put("name", "my-rest-api"),
                ar -> {
                    if (ar.failed()) {
                        // No service
                        completionHandler.handle(Future.failedFuture("No matching services"));
                    } else {
                        client = ar.result();
                        completionHandler.handle(Future.succeededFuture());
                    }
                });
    }

    MyRestAPIClient(Vertx vertx) {
        // Create the HTTP client and configure the host and post.
        client = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(8080)
        );
    }

    public void close() {
        // Don't forget to close the client when you are done.
        client.close();
    }

    public void getNames(Handler<AsyncResult<JsonArray>> handler) {
        // Emit a HTTP GET
        client.get("/names",
                response ->
                        // Handler called when the response is received
                        // We register a second handler to retrieve the body
                        response.bodyHandler(body -> {
                            // When the body is read, invoke the result handler
                            handler.handle(Future.succeededFuture(body.toJsonArray()));
                        }))
                .exceptionHandler(t -> {
                    // If something bad happen, report the failure to the passed handler
                    handler.handle(Future.failedFuture(t));
                })
                // Call end to send the request
                .end();
    }

    public void addName(String name, Handler<AsyncResult<Void>> handler) {
        // Emit a HTTP POST
        client.post("/names",
                response -> {
                    // Check the status code and act accordingly
                    if (response.statusCode() == 200) {
                        handler.handle(Future.succeededFuture());
                    } else {
                        handler.handle(Future.failedFuture(response.statusMessage()));
                    }
                })
                .exceptionHandler(t -> handler.handle(Future.failedFuture(t)))
                // Pass the name we want to add
                .end(name);
    }

    public static void main(String[] args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        MyRestAPIClient client = new MyRestAPIClient(discovery, event -> {
            Functions.log(event.succeeded());
        });
        TimeUnit.SECONDS.sleep(2);
        client.getNames(ar -> {
            if (ar.succeeded()) {
                Functions.log("Names: " + ar.result().encode());
            } else {
                Functions.log("Unable to retrieve the list of names: " + ar.cause().getMessage());
            }
        });
    }
}