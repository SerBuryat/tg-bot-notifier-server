package com.thunderbase.tg;

import com.thunderbase.tg.server.NotificationServer;

public class Run {

    private static final int DEFAULT_PORT = 8081;

    public static void main(String[] args) {
        var server = new NotificationServer();
        server.start(DEFAULT_PORT);
    }

}
