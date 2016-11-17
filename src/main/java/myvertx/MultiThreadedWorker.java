package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

class MultiThreadedWorker extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.eventBus().consumer("xman1", message -> message.reply((int) message.body() * 2));
    }
}
