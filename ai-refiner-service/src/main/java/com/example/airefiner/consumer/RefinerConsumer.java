package com.example.airefiner.consumer;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import com.example.airefiner.entity.RefinedNews;
import com.example.airefiner.producer.RefinedProducer;
import com.example.airefiner.repository.RefinedNewsRepository;
import com.example.airefiner.service.RefinerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RefinerConsumer {

    private final Logger log = LoggerFactory.getLogger(RefinerConsumer.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final LlmClient llmClient;
    private final RefinedNewsRepository refinedNewsRepository;
    private final RefinedProducer producer;
    private final RefinerService refinerService;

    public RefinerConsumer(LlmClient llmClient, 
                          RefinedNewsRepository refinedNewsRepository, 
                          RefinedProducer producer,
                          RefinerService refinerService) {
        this.llmClient = llmClient;
        this.refinedNewsRepository = refinedNewsRepository;
        this.producer = producer;
        this.refinerService = refinerService;
    }

    @KafkaListener(topics = "news.raw", groupId = "ai-refiner")
    public void consume(Object raw) {
        try {
            // convert to NewsRaw via ObjectMapper (handles Map or POJO)
            NewsRaw news = mapper.convertValue(raw, NewsRaw.class);
            log.info("Refining news id={} source={}", news.getId(), news.getSource());

            LlmClient.LlmResult res = llmClient.refine(news);
            
            // Use the refinement layer to clean and standardize the response
            RefinedNews rn = refinerService.refine(res, news.getId());
            RefinedNews saved = refinedNewsRepository.save(rn);
            producer.publish(saved);
            log.info("Published refined news id={}", saved.getId());
        } catch (Exception e) {
            log.warn("Failed to refine raw message: {}", e.getMessage());
        }
    }
}