package com.example.notification.service;

import org.springframework.beans.factory.annotation.Value;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.springframework.web.util.HtmlUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TelegramService {

    private final String botToken;
    private final String chatId;
    private final boolean enabled;
    private final RestTemplate restTemplate = new RestTemplate();

    public TelegramService(
            @Value("${telegram.bot-token:}") String botToken,
            @Value("${telegram.chat-id:}") String chatId,
            @Value("${telegram.enabled:true}") boolean enabled
    ) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.enabled = enabled;
    }

    public void sendMessage(String message) {
        if (!enabled) return;
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) {
            System.err.println("Telegram not configured (missing token/chat id). Skipping send.");
            return;
        }

        String decoded = decodeAndClean(message);

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", decoded
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Telegram send status: " + resp.getStatusCode());
        } catch (Exception e) {
            System.err.println("Failed to send Telegram message: " + e.getMessage());
        }
    }

    private String decodeAndClean(String message) {
        if (message == null) return "";
        String cur = message;
        // repeatedly decode up to 3 times to handle double-encoding
        for (int i = 0; i < 3; i++) {
            try {
                String d = URLDecoder.decode(cur, StandardCharsets.UTF_8.name());
                if (d.equals(cur)) break;
                cur = d;
            } catch (Exception e) {
                break;
            }
        }

        // unescape HTML entities (e.g., &nbsp;, &amp;)
        cur = HtmlUtils.htmlUnescape(cur);

        // strip HTML tags
        cur = cur.replaceAll("(?s)<[^>]*>", "");

        // replace non-breaking spaces with regular spaces
        cur = cur.replace('\u00A0', ' ');

        // normalize whitespace: keep newlines, collapse other whitespace
        cur = cur.replaceAll("[\u0000-\u001F&&[^\n\r]]+", "");
        cur = cur.replaceAll("\r\n", "\n");
        cur = cur.replaceAll("[ \t]{2,}", " ");
        cur = cur.trim();
        return cur;
    }
}
