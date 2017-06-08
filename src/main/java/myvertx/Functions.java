package myvertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(JsonArray jsonArray) {
        return jsonArray == null ? Collections.emptyList() : (List<T>) jsonArray.getList();
    }

    @SuppressWarnings("squid:S1166")
    public static Optional<JsonObject> parseJsonObject(String json) {
        try {
            return Optional.of(new JsonObject(json));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public static <T> Collector<T, List<T>, List<T>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableList);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ArrayList<T>, T[]> toArray(Class<T> clazz) {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, list -> list.toArray((T[]) Array.newInstance(clazz, list.size())));
    }

    public static <T> Collector<T, JsonArray, JsonArray> toJsonArray() {
        return Collector.of(JsonArray::new, JsonArray::add, JsonArray::addAll);
    }

    public static <T> Collector<T, List<T>, JsonArray> toImmutableJsonArray() {
        return Collector.of(
                ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> new JsonArray(Collections.unmodifiableList(list)));
    }


    public static <K, V> Collector<Map.Entry<K, V>, JsonObject, JsonObject> toJsonObject() {
        return Collector.of(JsonObject::new, (jo, e) -> jo.put(e.getKey().toString(), e.getValue()), JsonObject::mergeIn);
    }

    public static <K, V> Collector<Map.Entry<K, V>, HashMap<String, Object>, JsonObject> toImmutableJsonObject() {
        return Collector.of(
                HashMap::new,
                (map, e) -> map.put(e.getKey().toString(), e.getValue()),
                (left, right) -> {
                    left.putAll(right);
                    return left;
                },
                map -> new JsonObject(Collections.unmodifiableMap(map)));
    }


    public static <T> Stream<T> toStream(Optional<T> tOptional) {
        return tOptional.map(Stream::of).orElseGet(Stream::empty);
    }

}
