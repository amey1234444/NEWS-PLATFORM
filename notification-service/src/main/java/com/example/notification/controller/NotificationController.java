package com.example.notification.controller;

import com.example.notification.service.TelegramService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    private final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final TelegramService telegramService;

    public NotificationController(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @GetMapping("/test")
    public String test(@RequestParam(value = "message", defaultValue = "Test message from notification-service") String message) {
        log.info("Received notify/test message param: [{}]", message);
        telegramService.sendMessage(message);
        return "ok";
    }
}
