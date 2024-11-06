package com.thunderbase.tg.notifier;

public record Notification(String chatId, String msg, Object details) {

}
