package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicInteger;

public class Worker extends AbstractVerticle {

    private final static AtomicInteger COUNTER = new AtomicInteger();
    private final Integer id;

    public Worker() {
        id = COUNTER.getAndIncrement();
    }

    @Override
    public void start(Future<Void> future) {
        vertx.eventBus().consumer("xman", message -> {
                    int intMessage = (int) message.body();
                    System.err.printf("%s %d %d\n", Thread.currentThread().getName(), id, intMessage);
                    message.reply((int) message.body() * 2);
                }
        );
    }
}
