package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.Optional;

class Verticle1 extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.createHttpServer()
                .requestHandler(r -> {
                    Optional<String> name = Optional.ofNullable(r.getParam("name"));
                    r.response().end("hello " + name.orElse("there!"));
                })
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                });
    }
}