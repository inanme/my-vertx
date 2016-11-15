package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

class Services {

    static class Service1 extends AbstractVerticle {
        @Override
        public void start(Future<Void> future) {
            vertx.eventBus().consumer("service1", event -> {
                Functions.sleep(300L);
                event.reply("service1 - " + event.body());
            });
        }
    }

    static class Service2 extends AbstractVerticle {
        @Override
        public void start(Future<Void> future) {
            vertx.eventBus().consumer("service2", event -> {
                Functions.sleep(200L);
                event.reply("service2 - " + event.body());
            });
        }
    }

    static class Service3 extends AbstractVerticle {
        @Override
        public void start(Future<Void> future) {
            vertx.eventBus().consumer("service3", event -> {
                Functions.sleep(100L);
                event.reply("service3 - " + event.body());
            });
        }
    }

}
