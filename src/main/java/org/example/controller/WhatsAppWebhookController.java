package org.example.controller;

import org.example.service.MessageProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WhatsAppWebhookController {

    @Autowired
    private MessageProcessingService messageProcessingService;

    @PostMapping("/whatsapp")
    public ResponseEntity<String> receiveMessage(@RequestBody String payload) {

        messageProcessingService.processIncomingMessage(payload);

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}