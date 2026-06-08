package com.example.airefiner.controller;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import com.example.airefiner.entity.RefinedNews;
import com.example.airefiner.producer.RefinedProducer;
import com.example.airefiner.repository.RefinedNewsRepository;
import com.example.airefiner.service.RefinerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/refine")
public class RefinerTestController {

    private final Logger log = LoggerFactory.getLogger(RefinerTestController.class);
    private final LlmClient llmClient;
    private final RefinedNewsRepository repo;
    private final RefinedProducer producer;
    private final RefinerService refinerService;
    private final WebClient webClient;
    private final String notificationBase;

    public RefinerTestController(LlmClient llmClient,
                                 RefinedNewsRepository repo,
                                 RefinedProducer producer,
                                 RefinerService refinerService,
                                 @Value("${notification.url:http://localhost:8083}") String notificationBase) {
        this.llmClient = llmClient;
        this.repo = repo;
        this.producer = producer;
        this.refinerService = refinerService;
        this.notificationBase = notificationBase;
        this.webClient = WebClient.create(notificationBase);
    }

    @PostMapping("/test")
    public Mono<ResponseEntity<Map<String,Object>>> refineTest(@RequestBody NewsRaw raw) {
        return Mono.fromCallable(() -> {
            com.example.airefiner.client.LlmClient.LlmResult res = llmClient.refine(raw);
            
            // Use the refinement layer to clean and standardize the response
            RefinedNews rn = refinerService.refine(res, raw.getId());
            RefinedNews saved = repo.save(rn);

            // try publish to Kafka (best-effort)
            try { producer.publish(saved); } catch (Exception e) { log.warn("Kafka publish failed: {}", e.getMessage()); }

                // call notification-service test endpoint as fallback to produce Telegram message
                String message = formatMessage(saved);
                String notifyResp = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/notify/test").queryParam("message", message).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(10));

            Map<String, Object> out = new HashMap<>();
            out.put("refinedId", saved.getId());
            out.put("notifyResponse", notifyResp);
            return ResponseEntity.ok(out);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> {
            log.warn("Refine test failed: {}", e.getMessage());
            return Mono.just(ResponseEntity.status(500).body(Map.of("error", e.getMessage())));
        });
    }

    private String formatMessage(RefinedNews r) {
        StringBuilder sb = new StringBuilder();
        if (r.getTitle() != null && !r.getTitle().isBlank()) sb.append(r.getTitle()).append("\n\n");
        if (r.getSummary() != null && !r.getSummary().isBlank()) sb.append(r.getSummary()).append("\n\n");
        if (r.getTags() != null && !r.getTags().isBlank()) sb.append("Tags: ").append(r.getTags());
        return sb.toString().trim();
    }
}