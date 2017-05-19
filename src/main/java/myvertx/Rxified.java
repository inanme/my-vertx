package myvertx;

import io.vertx.core.*;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import myvertx.Services.Service1;
import myvertx.Services.Service2;
import myvertx.Services.Service3;
import rx.Single;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.err;

class Rxified {

    static class Zip extends io.vertx.rxjava.core.AbstractVerticle {

        private final AtomicInteger atomicInteger = new AtomicInteger();

        @Override
        public void start(Future<Void> future) {
            vertx.setPeriodic(2000L, l -> {
                Functions.log("new event");
                EventBus eventBus = vertx.eventBus();
                Single<Message<String>> service1 = eventBus.rxSend("service1", atomicInteger.getAndIncrement());
                Single<Message<String>> service2 = eventBus.rxSend("service2", atomicInteger.getAndIncrement());
                Single<Message<String>> service3 = eventBus.rxSend("service3", atomicInteger.getAndIncrement());

                Single.zip(service1, service2, service3,
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
                eventBus.<String>rxSend("service1", "init")
                        .flatMap(message -> eventBus.<String>rxSend("service2", message.body()))
                        .flatMap(message -> eventBus.<String>rxSend("service3", message.body()))
                        .subscribe(message -> Functions.log(message.body()));
            });
        }
    }

    static class Problem extends AbstractVerticle {

        @Override
        public void start() {
            Thread.currentThread().setName("problem");
            vertx.eventBus().<String>consumer("problem", message -> {
                alreadyEncoded(message.body(), ar1 -> {
                    if (ar1.succeeded()) {
                        requestMediaProcessing(ar1.result(), ar2 -> {
                            if (ar2.succeeded()) {
                                retrieveStatusAndStore(ar2.result(), ar3 -> {
                                    if (ar3.succeeded()) {
                                        message.reply(ar3.result());
                                    } else {
                                        message.reply("problem - " + ar3.cause().getMessage());
                                    }
                                });
                            } else {
                                message.reply("problem - " + ar2.cause().getMessage());
                            }
                        });
                    } else {
                        message.reply("problem - " + ar1.cause().getMessage());
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
            vertx.eventBus().<String>send(Service3.NAME, message, ar -> {
                if (ar.succeeded()) {
                    handler.handle(Future.succeededFuture(ar.result().body()));
                } else {
                    handler.handle(Future.failedFuture(ar.cause()));
                }
            });
        }
    }

    static class Solution extends io.vertx.rxjava.core.AbstractVerticle {

        @Override
        public void start() {
            Thread.currentThread().setName("solution");
            vertx.eventBus().<String>consumer("solution", message -> {
                alreadyEncoded(message.body())
                        .flatMap(msg1 -> requestMediaProcessing(msg1.body()))
                        .flatMap(msg2 -> retrieveStatusAndStore(msg2.body()))
                        .subscribe(
                                msg3 -> message.reply(msg3.body()),
                                throwable -> message.reply("solution - " + throwable.getMessage()));
            });
        }

        Single<Message<String>> alreadyEncoded(String message) {
            return vertx.eventBus().rxSend(Service1.NAME, message);
        }

        Single<Message<String>> requestMediaProcessing(String message) {
            return vertx.eventBus().rxSend(Service2.NAME, message);
        }

        Single<Message<String>> retrieveStatusAndStore(String message) {
            return vertx.eventBus().rxSend(Service3.NAME, message);
        }
    }

    public static void main(String... args1) {
        Thread.currentThread().setName("main");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Service1());
        vertx.deployVerticle(new Service2());
        vertx.deployVerticle(new Service3());
        vertx.deployVerticle(new Problem());
        vertx.deployVerticle(new Solution());
        vertx.eventBus().send("problem", "input-problem", event -> err.println(event.result().body()));
        vertx.eventBus().send("solution", "input-solution", event -> err.println(event.result().body()));
    }
}
