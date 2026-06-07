package com.example.newsfetcher.producer;

import com.example.newsfetcher.entity.News;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NewsProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public NewsProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(News news){
        kafkaTemplate.send("news.raw", news);
    }
}
