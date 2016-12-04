package myvertx;

import io.vertx.core.AbstractVerticle;

import java.util.Random;

class Services {
    static final Random RND = new Random();

    static class Service1 extends AbstractVerticle {
        static final String NAME = "service1";

        static class Service1Exception extends RuntimeException {
        }

        @Override
        public void start() {
            Thread.currentThread().setName(NAME);
            vertx.eventBus().consumer(NAME, event -> {
                Functions.sleep(300L);
                event.reply(NAME + " - " + event.body());
            });
        }
    }

    static class Service2 extends AbstractVerticle {
        static final String NAME = "service2";

        static class Service2Exception extends RuntimeException {
        }

        @Override
        public void start() {
            Thread.currentThread().setName(NAME);
            vertx.eventBus().consumer(NAME, event -> {
                Functions.sleep(200L);
                if (RND.nextBoolean()) {
                    event.fail(1, "Service2Exception");
                } else {
                    event.reply(NAME + " - " + event.body());
                }

            });
        }
    }

    static class Service3 extends AbstractVerticle {
        static final String NAME = "service3";

        static class Service3Exception extends RuntimeException {
        }

        @Override
        public void start() {
            Thread.currentThread().setName(NAME);
            vertx.eventBus().consumer(NAME, event -> {
                Functions.sleep(100L);
                event.reply(NAME + " - " + event.body());
            });
        }
    }

}
