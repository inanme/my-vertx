package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

class Websocket extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.createHttpServer()
                .websocketHandler(webSocket -> {
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