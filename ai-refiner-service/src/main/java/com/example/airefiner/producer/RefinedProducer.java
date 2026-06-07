package com.example.airefiner.producer;

import com.example.airefiner.entity.RefinedNews;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefinedProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RefinedProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(RefinedNews news) {
        kafkaTemplate.send("news.refined", news);
    }
}
