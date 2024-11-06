package com.thunderbase.tg.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thunderbase.tg.bot.Bot;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class Notifier {

    private final Bot bot;
    private final ObjectMapper mapper;

    public Notifier(String token) {
        this.bot = Bot.builder(token).build();
        this.mapper = new ObjectMapper();
    }

    public Mono<String> send(Notification ntf) {
        return Mono.just(
                new TgErrorNotification(
                        ntf.chatId(),
                        ntf.msg(),
                        OffsetDateTime.now().toLocalDateTime().toString(),
                        ntf.details()
                )
        )
        .flatMap(this::createNotificationDetailsFile)
        .flatMap(file ->
                bot.sendDocument(
                                ntf.chatId(), ntf.msg(), file.toString()
                )
                // todo - handle errors and remove temporary file properly
                .log()
                .onErrorReturn("Can't send notification. Check log.")
                .flatMap(resp ->
                        removeNotificationDetailsFile(file).thenReturn(resp)
                )
        );
    }

    private Mono<Path> createNotificationDetailsFile(TgErrorNotification tgErrorNotification) {
        return Mono.fromCallable(() -> {
            var tmpDetailsFile = File.createTempFile(
                    "notification-details-", ".json"
            );

            mapper.writeValue(tmpDetailsFile, tgErrorNotification);

            return tmpDetailsFile.toPath();
        })
        // todo - handle with blocking file i/o via `boundedElastic()`
        .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Boolean> removeNotificationDetailsFile(Path file) {
        // todo - handle with blocking file i/o via `boundedElastic()`
        return Mono.fromCallable(() -> Files.deleteIfExists(file))
                .subscribeOn(Schedulers.boundedElastic());
    }

    record TgErrorNotification(String chatId,
                               String msg,
                               String timestamp,
                               Object details) {

    }

}
