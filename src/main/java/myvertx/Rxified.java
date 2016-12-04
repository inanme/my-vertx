package myvertx;

import io.vertx.core.*;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import myvertx.Services.Service1;
import myvertx.Services.Service2;
import myvertx.Services.Service3;
import rx.Observable;

import java.util.concurrent.atomic.AtomicInteger;

class Rxified {

    static class Zip extends io.vertx.rxjava.core.AbstractVerticle {

        private final AtomicInteger atomicInteger = new AtomicInteger();

        @Override
        public void start(Future<Void> future) {
            vertx.setPeriodic(2000L, l -> {
                Functions.log("new event");
                EventBus eventBus = vertx.eventBus();
                Observable<Message<String>> service1 = eventBus.sendObservable("service1", atomicInteger.getAndIncrement());
                Observable<Message<String>> service2 = eventBus.sendObservable("service2", atomicInteger.getAndIncrement());
                Observable<Message<String>> service3 = eventBus.sendObservable("service3", atomicInteger.getAndIncrement());

                Observable.zip(service1, service2, service3,
                        (x, y, z) -> String.format("%s %s %s", x.body(), y.body(), z.body()))
                        .subscribe(Functions::log);
            });
        }
    }

    static class FlatMap extends io.vertx.rxjava.core.AbstractVerticle {

        @Override
        public void start(Future<Void> future) {
            vertx.setPeriodic(2000L, l -> {
                Functions.log("new event");
                EventBus eventBus = vertx.eventBus();
                eventBus.<String>sendObservable("service1", "init")
                        .flatMap(message -> eventBus.<String>sendObservable("service2", message.body()))
                        .flatMap(message -> eventBus.<String>sendObservable("service3", message.body()))
                        .subscribe(message -> Functions.log(message.body()));
            });
        }
    }

    static class Problem extends AbstractVerticle {

        @Override
        public void start() {
            Thread.currentThread().setName("Problem");
            vertx.eventBus().<String>consumer("problem", message -> {
                alreadyEncoded(message.body(), ar1 -> {
                    if (ar1.succeeded()) {
                        requestMediaProcessing(ar1.result(), ar2 -> {
                            if (ar2.succeeded()) {
                                retrieveStatusAndStore(ar2.result(), ar3 -> {
                                    if (ar3.succeeded()) {
                                        message.reply(ar3.result());
                                    } else {
                                        System.out.println("err3");
                                    }
                                });
                            } else {
                                System.out.println("err2");
                            }
                        });
                    } else {
                        System.out.println("err1");
                    }
                });
            });
        }

        void alreadyEncoded(String message, Handler<AsyncResult<String>> handler) {
            vertx.eventBus().<String>send(Service1.NAME, message, ar -> {
                if (ar.succeeded()) {
                    handler.handle(Future.succeededFuture(ar.result().body()));
                } else {
                    handler.handle(Future.failedFuture(ar.cause()));
                }
            });
        }

        void requestMediaProcessing(String message, Handler<AsyncResult<String>> handler) {
            vertx.eventBus().<String>send(Service2.NAME, message, ar -> {
                if (ar.succeeded()) {
                    handler.handle(Future.succeededFuture(ar.result().body()));
                } else {
                    handler.handle(Future.failedFuture(ar.cause()));
                }
            });
        }

        void retrieveStatusAndStore(String message, Handler<AsyncResult<String>> handler) {
            vertx.eventBus().<String>send(Service2.NAME, message, ar -> {
                if (ar.succeeded()) {
                    handler.handle(Future.succeededFuture(ar.result().body()));
                } else {
                    handler.handle(Future.failedFuture(ar.cause()));
                }
            });
        }
    }

    public static void main(String... args1) {
        Thread.currentThread().setName("main");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Service1());
        vertx.deployVerticle(new Service2());
        vertx.deployVerticle(new Service3());
        vertx.deployVerticle(new Problem());
        vertx.eventBus().send("problem", "input", event -> System.out.println(event.result().body()));
    }
}
