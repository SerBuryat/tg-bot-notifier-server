package com.thunderbase.tg.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.netty.http.client.HttpClient;

public class BotBuilder {

    private static final String DEFAULT_TG_BOT_BASE_URL = "https://api.telegram.org/bot";

    private final String token;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    protected BotBuilder(String token) {
        this.httpClient = HttpClient.create()
                .baseUrl(DEFAULT_TG_BOT_BASE_URL + token);
        this.token = token;
        this.mapper = new ObjectMapper();
    }

    public BotBuilder baseUrl(String baseUrl) {
        this.httpClient.baseUrl(baseUrl + token);
        return this;
    }

    public Bot build() {
        return new Bot(httpClient, mapper);
    }
}
