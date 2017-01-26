package myvertx;

import io.vertx.core.*;

import java.util.concurrent.atomic.AtomicInteger;

class ConsumerMain extends AbstractVerticle {

    public static class Consumer extends AbstractVerticle {

        private final AtomicInteger COUNTER = new AtomicInteger();
        private final Integer id;

        public Consumer() {
            id = COUNTER.getAndIncrement();
        }

        @Override
        public void start(Future<Void> future) {
            vertx.eventBus().consumer("xman", message -> {
                        int intMessage = (int) message.body();
                        Functions.log(id, intMessage);
                        message.reply((int) message.body() * 2);
                    }
            );
        }
    }

    @Override
    public void start(Future<Void> future) {
        vertx.deployVerticle(Consumer.class.getName(),
                new DeploymentOptions()
                        .setWorker(true)
                        .setWorkerPoolName("worker-th1")
                        .setWorkerPoolSize(4)
                        .setInstances(4));
    }

    public static void main(String... args1) {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), handler -> {
            if (handler.succeeded()) {
                handler.result().deployVerticle(new ConsumerMain());
            }
        });
    }
}

class ProducerMain extends AbstractVerticle {

    public static class ProducerStandard extends AbstractVerticle {

        private final AtomicInteger atomicInteger = new AtomicInteger();

        @Override
        public void start(Future<Void> future) {
            vertx.setPeriodic(1000l, l ->
                    vertx.eventBus().send(
                            "xman",
                            atomicInteger.getAndIncrement(),
                            event -> Functions.log("here ", event.result().body())));
        }
    }

    @Override
    public void start(Future<Void> future) {
        vertx.deployVerticle(new ProducerStandard());
        vertx.deployVerticle(new MultiThreadedWorker(),
                new DeploymentOptions().setWorker(true).setMultiThreaded(true));
    }

    public static void main(String... args1) {
        Vertx.clusteredVertx(new VertxOptions().setClustered(true), handler -> {
            if (handler.succeeded()) {
                handler.result().deployVerticle(new ProducerMain());
            }
        });
    }
}

class MultiThreadedWorker extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.eventBus().consumer("xman1", message -> message.reply((int) message.body() * 2));
    }
}
