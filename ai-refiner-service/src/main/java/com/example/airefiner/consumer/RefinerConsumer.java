package com.example.airefiner.consumer;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import com.example.airefiner.entity.RefinedNews;
import com.example.airefiner.producer.RefinedProducer;
import com.example.airefiner.repository.RefinedNewsRepository;
import com.example.airefiner.service.RefinedResponseProcessor;
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
    private final RefinedResponseProcessor processor;

    public RefinerConsumer(LlmClient llmClient,
                           RefinedNewsRepository refinedNewsRepository,
                           RefinedProducer producer,
                           RefinedResponseProcessor processor) {
        this.llmClient = llmClient;
        this.refinedNewsRepository = refinedNewsRepository;
        this.producer = producer;
        this.processor = processor;
    }

    @KafkaListener(topics = "news.raw", groupId = "ai-refiner")
    public void consume(Object raw) {
        try {
            NewsRaw news = mapper.convertValue(raw, NewsRaw.class);
            log.info("Refining news id={} source={}", news.getId(), news.getSource());

            LlmClient.LlmResult rawResult = llmClient.refine(news);
            LlmClient.LlmResult cleaned = processor.process(rawResult, news);

            RefinedNews rn = new RefinedNews();
            rn.setTitle(cleaned.getTitle());
            rn.setSummary(cleaned.getSummary());
            rn.setTags(cleaned.getTags());

            RefinedNews saved = refinedNewsRepository.save(rn);
            producer.publish(saved);
            log.info("Published refined news id={}", saved.getId());
        } catch (Exception e) {
            log.warn("Failed to refine raw message: {}", e.getMessage());
        }
    }
}
