package org.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WhatsAppService {

    @Value("${whatsapp.api.url}")
    private String apiUrl;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String token;

    public void sendMessage(String to, String message) {

        RestTemplate restTemplate = new RestTemplate();

        String url = apiUrl + "/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = """
        {
          "messaging_product": "whatsapp",
          "to": "%s",
          "type": "text",
          "text": {
            "body": "%s"
          }
        }
        """.formatted(to, message);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}