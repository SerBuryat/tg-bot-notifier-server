package com.thunderbase.tg.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;

public class Configs {

    private static final Config CONFIG = ConfigFactory.load();

    public static final String BOT_TOKEN = getBotToken();

    private static String getBotToken() {
        var fromEnvs = System.getenv("BOT_NOTIFIER_TOKEN");
        if(fromEnvs != null) {
            return fromEnvs;
        }
        try {
            return CONFIG.getString("server.bot.token");
        } catch (ConfigException ex) {
            throw new RuntimeException(
                    """
                        --------
                        Server props error:
                        --------
                        `server.bot.token` props variable is absent or wrong type.
                        Fix or create `src/main/resources/application.conf` with:
                        1)
                        server {
                                    bot {
                                        token = "your-bot-token-value"
                                    }
                                }
                        2)
                        Set environment variable: `BOT_NOTIFIER_TOKEN` with "your-bot-token"
                        --------
                    """
            );
        }
    }

}
