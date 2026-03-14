package org.example.service;

import org.example.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessingService {

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private ChatRepository chatRepository;

    public void processIncomingMessage(String payload) {

        String userId = "demo-user";
        String userMessage = payload;

        String history = chatRepository.getChatHistory(userId);

        String aiResponse = openAIService.askAI(history + "\nUser: " + userMessage);

        chatRepository.saveChat(userId, userMessage, aiResponse);

        whatsAppService.sendMessage(userId, aiResponse);
    }
}