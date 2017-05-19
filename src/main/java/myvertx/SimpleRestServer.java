package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.ArrayList;
import java.util.List;

import static myvertx.Functions.log;

public class SimpleRestServer extends AbstractVerticle {

    private List<String> names = new ArrayList<>();

    static final String NAME_ENDPOINT = "/name";

    @Override
    public void start() {
        // Create a Vert.x web router
        Router router = Router.router(vertx);

        // Register a simple first route on /
        router.get("/").handler(rc -> {
            rc.response().end("Welcome");
        });

        // Register a second router retrieving all stored names as JSON
        router.get(NAME_ENDPOINT)
                .produces("application/json")
                .handler(rc -> rc.response()
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(names)));

        // Register a body handler indicating that other routes need
        // to read the request body
        router.route().handler(BodyHandler.create());

        // Register a third route to add names
        router.post(NAME_ENDPOINT).handler(
                rc -> {
                    String name = rc.getBody().toString();
                    if (name.isEmpty()) {
                        rc.response().setStatusCode(400).end("No names");
                    } else if (names.contains(name)) {
                        rc.response().setStatusCode(409).end("Duplicated");
                    } else {
                        names.add(name);
                        rc.response().setStatusCode(201).end(name);
                    }
                });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void serviceDiscovery() {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        discovery.publish(HttpEndpoint.createRecord(
                "my-rest-api",
                "localhost", 8080,
                NAME_ENDPOINT),
                ar -> {
                    if (ar.succeeded()) {
                        log("REST API published");
                    } else {
                        log("Unable to publish the REST API: " + ar.cause().getMessage());
                    }
                });
    }

    public static void main1(String... args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(SimpleRestServer.class.getName(), new DeploymentOptions().setInstances(16));
    }

    public static void main(String... __) {
        String[] args = {"run", SimpleRestServer.class.getName()};
        Launcher.main(args);
    }

}