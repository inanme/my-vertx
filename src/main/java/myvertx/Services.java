package myvertx;

import io.vertx.core.AbstractVerticle;

class Services {

    static class Service1 extends AbstractVerticle {
        public static final String NAME = "service1";

        public void start() {
            Thread.currentThread().setName(NAME);
            vertx.eventBus().consumer(NAME, event -> {
                Functions.sleep(300L);
                event.reply("service1 - " + event.body());
            });
        }
    }

    static class Service2 extends AbstractVerticle {
        public static final String NAME = "service2";

        @Override
        public void start() {
            Thread.currentThread().setName(NAME);
            vertx.eventBus().consumer(NAME, event -> {
                Functions.sleep(200L);
                event.reply("service2 - " + event.body());
            });
        }
    }

    static class Service3 extends AbstractVerticle {
        public static final String NAME = "service3";

        @Override
        public void start() {
            Thread.currentThread().setName(NAME);
            vertx.eventBus().consumer(NAME, event -> {
                Functions.sleep(100L);
                event.reply("service3 - " + event.body());
            });
        }
    }

}
