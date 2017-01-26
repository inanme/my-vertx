package myvertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomTests extends Infra {

    @Test
    public void test3() {
        Random random = new Random(Long.MAX_VALUE);
        System.out.println(random.nextInt());

        Random random1 = new Random(Long.MAX_VALUE);
        System.out.println(random1.nextInt());
        //1155099827
    }

    @Test
    public void test4() {
        m2(ar -> {
            if (ar.succeeded()) {
                log("ok : " + ar.result());
            } else if (ar.failed()) {
                log("failed");
            }
        });
        log("waiting...");
        giveMeTime(3);
    }

    public void m2(Handler<AsyncResult<Integer>> handler) {
        Observable.<Integer>create(subscriber -> {
            m1(ar -> {
                if (ar.succeeded()) {
                    subscriber.onNext(ar.result());
                    subscriber.onCompleted();
                } else if (ar.failed()) {
                    subscriber.onError(ar.cause());
                }
            });
            log("submitted");
        }).map(Math::incrementExact)
                .subscribe(it -> handler.handle(Future.succeededFuture(it)),
                        throwable -> handler.handle(Future.failedFuture(throwable)));
    }

    @Test
    public void test5() {
        m3(ar -> {
            if (ar.succeeded()) {
                log("ok : " + ar.result());
            } else if (ar.failed()) {
                log("failed");
            }
        });
        log("waiting...");
        giveMeTime(3);
    }

    public void m3(Handler<AsyncResult<Void>> handler) {
        Observable.<Void>create(subscriber -> {
            m1(ar -> {
                if (ar.succeeded()) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } else if (ar.failed()) {
                    subscriber.onError(ar.cause());
                }
            });
            log("submitted");
        }).subscribe(it -> handler.handle(Future.succeededFuture(it)),
                throwable -> handler.handle(Future.failedFuture(throwable)));
    }

    public void m1(Handler<AsyncResult<Integer>> handler) {
        thread1.submit(() -> {
            //Functions.sleep(Long.MAX_VALUE);
            //handler.handle(Future.failedFuture(new RuntimeException("d")));
            handler.handle(Future.succeededFuture(2));
        });
    }

    public static class Sub {

        final Handler<AsyncResult<Integer>> WTF = ar -> {
            if (ar.succeeded()) {
                System.out.println("Result : " + ar.result());
            } else if (ar.failed()) {
                ar.cause().printStackTrace(System.out);
            } else {
                throw new RuntimeException("WTF");
            }
        };

        @Test
        public void test4() {
            Future.<Integer>future().setHandler(WTF).completer().handle(Future.succeededFuture(23));
        }

        @Test
        public void test5() {
            Future<Integer> future = Future.future();
            future.complete(23);
            future.setHandler(WTF);
        }

        @Test
        public void test6() {
            Future.<Integer>future().setHandler(WTF).complete(23);
        }


    }
}