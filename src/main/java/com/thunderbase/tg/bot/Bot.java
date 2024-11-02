package com.thunderbase.tg.bot;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static io.netty.handler.codec.http.HttpHeaderValues.MULTIPART_FORM_DATA;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpHeaders;
import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.http.client.HttpClient;

public class Bot {

    private final ObjectMapper mapper;
    private final HttpClient httpClient;

    Bot(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @JsonInclude(Include.NON_NULL)
    record SendMessageBody(@JsonProperty("chat_id")
                           String chatId,
                           @JsonProperty("text")
                           String message
    ) {}

    public static BotBuilder builder(String token) {
        return new BotBuilder(token);
    }

    public Mono<String> getMe() {
        return get("/getMe");
    }

    public Mono<String> sendMessage(String chatId, String message) {
        return post(
                "/sendMessage",
                headers -> headers.add(CONTENT_TYPE, APPLICATION_JSON),
                ByteBufFlux.fromString(
                        Mono.just(
                                mapper.valueToTree(
                                        new SendMessageBody(chatId, message)
                                ).toPrettyString()
                        )
                )
        );
    }

    public Mono<String> sendDocument(String chatId, String title, String filePath) {
        return postWithForm(
                "/sendDocument",
                headers -> headers.add(CONTENT_TYPE, MULTIPART_FORM_DATA),
                Map.of(
                        "chat_id", chatId,
                        "caption", title,
                        "parse_mode", "html"
                ),
                Map.of(
                        "document", new File(filePath)
                )
        );
    }

    /** Make <b>single</b> <code>/getUpdates</code> and return unconfirmed updates. */
    public Mono<String> getUpdates() {
        return get("/getUpdates");
    }

    /** Make <b>endless</b> <code>/getUpdates</code> with duration and return unconfirmed updates. */
    public Flux<String> getUpdates(Duration duration) {
        return Flux.interval(duration)
                .flatMap(l -> getUpdates());
    }

    /** For custom <code>GET</code> requests "application/json"*/
    public Mono<String> get(String uri) {
        return httpClient.get()
                .uri(uri)
                .responseContent()
                .aggregate()
                .asString();
    }
    /** For custom <code>POST/code> requests with "application/json" */
    public Mono<String> post(String uri,
                               Consumer<HttpHeaders> headers,
                               ByteBufFlux body) {
        return httpClient
                .headers(headers)
                .post()
                .uri(uri)
                .send(body)
                .responseContent()
                .aggregate()
                .asString();
    }


    /** For custom <code>POST/code> requests with "multipart/form-data" for files */
    public Mono<String> postWithForm(String uri,
                                       Consumer<HttpHeaders> headers,
                                       Map<String, String> formAttrs,
                                       Map<String, File> formFile) {
        return httpClient
                .headers(headers)
                .post()
                .uri(uri)
                .sendForm((req, form) -> {
                    form.multipart(true);
                    formAttrs.forEach(form::attr);
                    formFile.forEach(form::file);
                })
                .responseContent()
                .aggregate()
                .asString();
    }
}
