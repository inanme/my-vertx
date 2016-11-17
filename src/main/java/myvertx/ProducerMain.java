package myvertx;

import io.vertx.core.*;

class ProducerMain extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.deployVerticle(new Standard());
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