package myvertx;

import com.google.common.io.Resources;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class SimpleWebsocketServer extends AbstractVerticle {

    @SuppressWarnings("CodeBlock2Expr")
    @Override
    public void start() {
        vertx.createHttpServer()
                .websocketHandler(serverWebSocket -> {
                    serverWebSocket.handler(buffer -> {
                        serverWebSocket.writeFinalTextFrame(buffer.toString());

                    });
                })
                .requestHandler(req -> {
                    if (req.uri().equals("/")) req.response().sendFile(Resources.getResource("ws.html").getFile());
                })
                .listen(8080);
    }

    public static void main(String... args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(SimpleWebsocketServer.class.getName());
    }

}