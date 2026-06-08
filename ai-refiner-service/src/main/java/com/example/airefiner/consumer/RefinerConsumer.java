package com.example.airefiner.consumer;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import com.example.airefiner.entity.RefinedNews;
import com.example.airefiner.producer.RefinedProducer;
import com.example.airefiner.refinement.ResponseRefiner;
import com.example.airefiner.repository.RefinedNewsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class RefinerConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(RefinerConsumer.class);
    
    private final RefinedNewsRepository repository;
    private final RefinedProducer producer;
    private final LlmClient llmClient;
    private final ResponseRefiner responseRefiner;
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Autowired
    public RefinerConsumer(RefinedNewsRepository repository, 
                         RefinedProducer producer,
                         LlmClient llmClient,
                         ResponseRefiner responseRefiner) {
        this.repository = repository;
        this.producer = producer;
        this.llmClient = llmClient;
        this.responseRefiner = responseRefiner;
    }
    
    @KafkaListener(topics = "news.raw", groupId = "ai-refiner")
    public void refineNews(String message) {
        try {
            JsonNode node = mapper.readTree(message);
            NewsRaw raw = mapper.treeToValue(node, NewsRaw.class);
            
            // Call LLM to refine the news
            LlmClient.LlmResult llmResult = llmClient.refine(raw);
            
            // Refine the LLM result
            LlmClient.LlmResult refinedResult = responseRefiner.refine(llmResult, raw);
            
            // Create refined news entity
            RefinedNews refinedNews = new RefinedNews();
            refinedNews.setTitle(refinedResult.getTitle());
            refinedNews.setSummary(refinedResult.getSummary());
            refinedNews.setTags(refinedResult.getTags());
            
            // Save to database
            RefinedNews saved = repository.save(refinedNews);
            log.info("Refined and saved news: {}", saved.getId());
            
            // Publish to Kafka
            producer.publish(saved);
            
        } catch (Exception e) {
            log.error("Error processing raw news: {}", e.getMessage(), e);
        }
    }
}