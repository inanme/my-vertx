package myvertx;

import com.google.common.collect.ImmutableList;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
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
import java.util.function.Function;
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

    public static <T> Handler<AsyncResult<T>> getAsyncResultHandler(CompletableFuture<T> completableFuture,
                                                                    Function<Throwable, Exception> exceptionFunction) {
        return r -> {
            if (r.succeeded()) {
                completableFuture.complete(r.result());
            } else {
                completableFuture.completeExceptionally(exceptionFunction.apply(r.cause()));
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

    public static CompletableFuture<SQLConnection> getConnection(JDBCClient jdbcClient) {
        CompletableFuture<SQLConnection> completableFuture = new CompletableFuture<>();
        jdbcClient.getConnection(getAsyncResultHandler(completableFuture, th -> new Exception("Failed to acquire db connection", th)));
        return completableFuture;
    }

    public static JsonArray jsonArray(Object object) {
        return new JsonArray(ImmutableList.of(object));
    }

    public static JsonArray jsonArray(Object object1, Object object2) {
        return new JsonArray(ImmutableList.of(object1, object2));
    }

    public static JsonArray jsonArray(Object object1, Object object2, Object object3) {
        return new JsonArray(ImmutableList.of(object1, object2, object3));
    }

    public static JsonArray jsonArray(Object object1, Object object2, Object object3, Object object4, Object... rest) {
        return new JsonArray(ImmutableList.builder().add(object1).add(object2).add(object3).add(object4).addAll(Arrays.asList(rest)).build());
    }

}
