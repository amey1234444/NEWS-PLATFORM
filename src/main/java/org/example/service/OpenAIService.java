package org.example.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OpenAIService {

    @Autowired
    private ChatClient chatClient;

    public String askAI(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}