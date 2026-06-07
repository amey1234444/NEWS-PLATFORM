package com.example.notification.consumer;

import com.example.notification.service.TelegramService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final TelegramService telegramService;

    public NotificationConsumer(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @KafkaListener(topics = "news.refined", groupId = "notification")
    public void consume(Object refined) {
        String message = buildMessage(refined);
        telegramService.sendMessage(message);
    }

    private String buildMessage(Object refined) {
        if (refined == null) return "(empty refined news)";
        return "New refined news:\n" + refined.toString();
    }
}
