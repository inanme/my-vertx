package myvertx;

import io.vertx.core.*;

public class ConsumerMain extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.deployVerticle(Worker.class.getName(),
                new DeploymentOptions().setWorker(true).setWorkerPoolName("worker-th1").setWorkerPoolSize(4)
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