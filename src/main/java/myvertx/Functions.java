package myvertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class Functions {

    static void sleep(long milis) {
        try {
            TimeUnit.MILLISECONDS.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static String now() {
        return LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    static void log(Object... objects) {
        System.err.format("%s %s: %s%n", now(), Thread.currentThread().getName(),
                Arrays.stream(objects).map(Object::toString).collect(Collectors.joining(",")));
    }

    static <T> Handler<AsyncResult<T>> getAsyncResultHandler(CompletableFuture<T> completableFuture) {
        return r -> {
            if (r.succeeded()) {
                completableFuture.complete(r.result());
            } else {
                completableFuture.completeExceptionally(r.cause());
            }
        };
    }

    public static <A, T> Handler<AsyncResult<T>> getAsyncResultHandler(A associate, CompletableFuture<Pair<A, T>> completableFuture) {
        return r -> {
            if (r.succeeded()) {
                completableFuture.complete(Pair.of(associate, r.result()));
            } else {
                completableFuture.completeExceptionally(r.cause());
            }
        };
    }

    public static <T> CompletableFuture<T> completedExceptionally(Throwable ex) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(ex);
        return completableFuture;
    }
}
