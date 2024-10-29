package com.thunderbase.tg.notifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thunderbase.tg.bot.Bot;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

public class Notifier {

    private final Bot bot;
    private final ObjectMapper mapper;

    public Notifier(String token) {
        this.bot = Bot.builder(token).build();
        this.mapper = new ObjectMapper();
    }

    public Mono<String> send(String chatId, Notification ntf) {
        return Mono.just(
                new TgErrorNotification(
                        ntf.title(),
                        ntf.msg(),
                        OffsetDateTime.now().toLocalDateTime().toString(),
                        ntf.details()
                )
        )
        .flatMap(this::createNotificationDetailsFile)
        .flatMap(file ->
                bot.sendDocument(
                        chatId, ntf.title() + " \n" + ntf.msg(), file.toString()
                )
                // todo - handle errors and remove temporary file properly
                .onErrorReturn("Can't send notification")
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

    record TgErrorNotification(String name,
                               String msg,
                               String timestamp,
                               Object details) {

    }

}
