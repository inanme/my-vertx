package myvertx;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.rxjava.core.eventbus.Message;
import myvertx.Services.Service1;
import myvertx.Services.Service2;
import myvertx.Services.Service3;
import rx.Observable;

import java.util.concurrent.atomic.AtomicInteger;

public class Rxified extends io.vertx.rxjava.core.AbstractVerticle {

    private final AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public void start(Future<Void> future) {
        vertx.setPeriodic(2000L, l -> {
            Functions.log("new event");
            Observable<Message<String>> service1 = vertx.eventBus().sendObservable("service1", atomicInteger.getAndIncrement());
            Observable<Message<String>> service2 = vertx.eventBus().sendObservable("service2", atomicInteger.getAndIncrement());
            Observable<Message<String>> service3 = vertx.eventBus().sendObservable("service3", atomicInteger.getAndIncrement());

            Observable.zip(service1, service2, service3,
                    (x, y, z) -> x.body() + y.body() + z.body())
                    .subscribe(Functions::log);
        });
    }

    public static void main(String... args1) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Service1());
        vertx.deployVerticle(new Service2());
        vertx.deployVerticle(new Service3());
        vertx.deployVerticle(new Rxified());
    }
}