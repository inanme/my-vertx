package myvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Launcher;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> future) {
        vertx.deployVerticle(new Standard());
        vertx.deployVerticle(Worker.class.getName(),
                new DeploymentOptions().setWorker(true).setWorkerPoolName("worker-th1").setWorkerPoolSize(4)
                        .setInstances(4));
        vertx.deployVerticle(new MultiThreadedWorker(),
                new DeploymentOptions().setWorker(true).setMultiThreaded(true));
    }

    public static void main(String... args1) {
        String[] args = {"run", MainVerticle.class.getName()};
        Launcher.main(args);
    }
}