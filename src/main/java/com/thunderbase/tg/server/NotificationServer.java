package com.thunderbase.tg.server;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thunderbase.tg.config.Configs;
import com.thunderbase.tg.notifier.Notification;
import com.thunderbase.tg.notifier.Notifier;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.logging.AccessLog;

public class NotificationServer {

    private static final Notifier TG_NOTIFIER = new Notifier(Configs.BOT_TOKEN);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void start(int port) {
        var server = HttpServer.create()
                .accessLog(true,
                        logArgs ->
                                AccessLog.create(
                                        "method={} | uri={} | duration={}",
                                        logArgs.method(), logArgs.uri(), logArgs.duration() + "ms"
                                )
                )
                .port(port)
                .route(routes -> {
                    routes.get(
                            "/health",
                            (req, resp) ->
                                    resp.sendString(Mono.just("Ready for work!"))
                    );
                    routes.post(
                            "/send-notification/{chatId}",
                            (req, resp) -> {
                                var notification = req.receive()
                                        .aggregate()
                                        .asByteArray()
                                        .flatMap(this::getNotificationFromBody);

                                var chatId = req.param("chatId");
                                if (chatId == null || chatId.isBlank()) {
                                    return resp.status(400)
                                            .sendString(Mono.just("Missing 'chatId' path variable"));
                                }

                                return resp.header(CONTENT_TYPE, APPLICATION_JSON)
                                        .sendString(
                                                notification.flatMap(ntf -> TG_NOTIFIER.send(chatId, ntf))
                                        );
                            }
                    );
                })
                .bindNow();

        server.onDispose().block();
    }

    private Mono<Notification> getNotificationFromBody(byte[] reqBodyBytes) {
        return Mono.fromCallable(() -> MAPPER.readValue(reqBodyBytes, Notification.class))
                .doOnError(ex ->
                        System.err.println("Failed to parse JSON: " + ex.getMessage())
                )
                // todo - blocking i/o?
                .subscribeOn(Schedulers.boundedElastic());
    }

}
