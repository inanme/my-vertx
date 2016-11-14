package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.ArrayList;
import java.util.List;

public class MyRestAPIVerticle extends AbstractVerticle {

    public static void main(String... args1) {
        String[] args = {"run", MyRestAPIVerticle.class.getName()};
        Launcher.main(args);
    }

    // Maintain a simple list of names
    private List<String> names = new ArrayList<>();

    @Override
    public void start() {
        // Create a Vert.x web router
        Router router = Router.router(vertx);

        // Register a simple first route on /
        router.get("/").handler(rc -> {
            rc.response().end("Welcome");
        });

        // Register a second router retrieving all stored names as JSON
        router.get("/names")
                .produces("application/json")
                .handler(rc -> rc.response()
                        .putHeader("content-type", "application/json")
                        .end(Json.encode(names)));

        // Register a body handler indicating that other routes need
        // to read the request body
        router.route().handler(BodyHandler.create());

        // Register a third route to add names
        router.post("/names").handler(
                rc -> {
                    // Read the body
                    String name = rc.getBody().toString();
                    if (name.isEmpty()) {
                        // Invalid body -> Bad request
                        rc.response().setStatusCode(400).end();
                    } else if (names.contains(name)) {
                        // Already included name -> Conflict
                        rc.response().setStatusCode(409).end();
                    } else {
                        // Add the name to the list -> Created
                        names.add(name);
                        rc.response().setStatusCode(201).end(name);
                    }
                });

        vertx.createHttpServer()
                // Pass the router's accept method as request handler
                .requestHandler(router::accept)
                .listen(8080);

        serviceDiscovery();
    }

    public void serviceDiscovery() {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        discovery.publish(HttpEndpoint.createRecord(
                "my-rest-api",
                "localhost", 8080,
                "/names"),
                ar -> {
                    if (ar.succeeded()) {
                        System.out.println("REST API published");
                    } else {
                        System.out.println("Unable to publish the REST API: " +
                                ar.cause().getMessage());
                    }
                });

    }

}