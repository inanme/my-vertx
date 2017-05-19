package myvertx;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static myvertx.Functions.getAsyncResultHandler;
import static myvertx.SimpleRestServer.NAME_ENDPOINT;

public class SimpleRestClient {

    static CompletableFuture<HttpClient> getClient(ServiceDiscovery discovery) {
        CompletableFuture<HttpClient> httpClient = new CompletableFuture<>();
        HttpEndpoint.getClient(discovery, new JsonObject().put("name", "my-rest-api"), getAsyncResultHandler(httpClient));
        return httpClient;
    }

    static CompletableFuture<HttpClient> getClient(Vertx vertx) {
        HttpClient httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(8080));
        return CompletableFuture.completedFuture(httpClient);
    }

    static CompletableFuture<Pair<HttpClient, String>> getNames(HttpClient httpClient) {
        CompletableFuture<Pair<HttpClient, String>> result = new CompletableFuture<>();
        httpClient.get(NAME_ENDPOINT,
                response -> response.bodyHandler(buffer -> result.complete(Pair.of(httpClient, buffer.toJsonArray().encode()))))
                .exceptionHandler(result::completeExceptionally)
                .end();
        return result;
    }

    static CompletableFuture<Pair<HttpClient, String>> addName(HttpClient httpClient, String name) {
        CompletableFuture<Pair<HttpClient, String>> result = new CompletableFuture<>();
        httpClient.post(NAME_ENDPOINT,
                response -> {
                    if (Arrays.asList(200, 201).contains(response.statusCode())) {
                        result.complete(Pair.of(httpClient, "OK"));
                    } else {
                        String statusMessage = response.statusMessage();
                        result.completeExceptionally(new IllegalStateException(statusMessage));
                    }
                })
                .exceptionHandler(result::completeExceptionally)
                .end(name);
        return result;
    }

    public static void main(String[] args) throws Exception {
        Vertx vertx = Vertx.vertx();
        CompletableFuture<HttpClient> clientCF = getClient(vertx);
        clientCF.thenCompose(client -> addName(client, "mert"))
                .thenCompose(result -> getNames(result.getLeft()))
                .thenAccept(result -> System.err.println(result.getRight()))
                .handle((__, th) -> {
                    if (Objects.nonNull(th)) {
                        System.err.println(th.getCause().getMessage());
                    }
                    vertx.close();
                    clientCF.thenAccept(HttpClient::close);
                    return null;
                })
                .get();
    }
}
