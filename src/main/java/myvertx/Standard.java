package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicInteger;

class Standard extends AbstractVerticle {

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public void start(Future<Void> future) {
        vertx.setPeriodic(1000l, l ->
                vertx.eventBus().send(
                        "xman",
                        atomicInteger.getAndIncrement(),
                        event -> Functions.log(event.result().body())));
    }
}