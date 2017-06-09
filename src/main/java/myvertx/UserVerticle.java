package myvertx;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.net.HttpURLConnection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static myvertx.Functions.getAsyncResultHandler;
import static myvertx.Functions.jsonArray;

class UserVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(UserVerticle.class);

    final int PORT = 8080;

    final JsonObject mysqlConfig = new JsonObject()
            .put("driver_class", "com.mysql.jdbc.Driver")
            .put("url", "jdbc:mysql://127.0.0.1:3306/db?autoReconnect=true")
            .put("user", "user")
            .put("password", "password")
            .put("max_idle_time1", 600);

    JDBCClient jdbcClient;
    MysqlConnectionPoolDataSource dataSource;

    public static final String JSON_CONTENT_TYPE = "application/json;charset=utf-8";

    public void handleException(RoutingContext ctx) {
        @Nullable Throwable failure = ctx.failure();
        ctx.response()
                .setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .putHeader("Content-Type", JSON_CONTENT_TYPE)
                .end(new JsonObject().put("message", failure.getMessage()).encode());
    }

    public void handleNotFound(RoutingContext ctx) {
        ctx.response()
                .setStatusCode(HttpURLConnection.HTTP_NOT_FOUND)
                .putHeader("Content-Type", JSON_CONTENT_TYPE)
                .end(new JsonObject().put("message", "not found").encode());
    }

    @Override
    public void start(Future<Void> future) {
        dataSource = new MysqlConnectionPoolDataSource();

        dataSource.setUser("user");
        dataSource.setPassword("password");
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/db?autoReconnect=true&useUnicode=true&characterEncoding=utf-8");

        final Router router = Router.router(vertx);

        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.PATCH)
                .allowedMethod(HttpMethod.DELETE)
                .allowedMethod(HttpMethod.OPTIONS)
                .allowCredentials(true)
                .allowedHeader("Authorization")
                .allowedHeader("Content-Type")
                .maxAgeSeconds(Integer.MAX_VALUE));

        router.route().handler(BodyHandler.create());

        router.route("/*").failureHandler(this::handleException);
        router.route().last().handler(this::handleNotFound);

        router.get("/save").handler(this::save);
        router.get("/saves").handler(this::saves);

        jdbcClient = JDBCClient.create(vertx, dataSource);
        CompletableFuture<HttpServer> httpServer = new CompletableFuture<>();
        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(PORT, "127.0.0.1", getAsyncResultHandler(httpServer));

        httpServer.thenRun(future::complete)
                .exceptionally(th -> {
                    future.fail(th);
                    return null;
                });
    }

    private void save(RoutingContext rc) {
        Optional<String> string = Optional.ofNullable(rc.request().getParam("string"));
        save(string.orElse("Poción"))
                .thenRun(() -> rc.response().setStatusCode(HttpResponseStatus.OK.code()).end("ok"))
                .exceptionally(th -> {
                    log.error(th.getMessage(), th);
                    rc.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end();
                    return null;
                });
    }

    private void saves(RoutingContext rc) {
        Optional<String> string = Optional.ofNullable(rc.request().getParam("string"));
        saves(dataSource, string.orElse("Poción"));
        rc.response().setStatusCode(HttpResponseStatus.OK.code()).end("ok");
    }

    CompletableFuture<UpdateResult> save(String string) {
        CompletableFuture<SQLConnection> connection = new CompletableFuture<>();
        jdbcClient.getConnection(getAsyncResultHandler(connection));
        return connection
                .thenCompose(conn -> save(conn, string))
                .whenComplete((__, th) -> connection.thenAccept(SQLConnection::close));
    }

    CompletableFuture<UpdateResult> saveInsert(SQLConnection sqlConnection, String string) {
        CompletableFuture<UpdateResult> updateResult = new CompletableFuture<>();
        sqlConnection.updateWithParams(
                "insert into char_test(name) values(?)",
                jsonArray(string),
                getAsyncResultHandler(updateResult));
        return updateResult;
    }

    CompletableFuture<UpdateResult> save(SQLConnection sqlConnection, String string) {
        CompletableFuture<UpdateResult> updateResult = new CompletableFuture<>();
        sqlConnection.updateWithParams(
                "update some_table set name = ? where id = ?",
                jsonArray(string + string, 1),
                getAsyncResultHandler(updateResult));
        return updateResult;
    }

    int saves(DataSource dataSource, String string) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.update("update some_table set name = ? where id = ?", string + string, 1);
    }
}
