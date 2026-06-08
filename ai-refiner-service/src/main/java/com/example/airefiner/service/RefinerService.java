package com.example.airefiner.service;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.entity.RefinedNews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RefinerService {

    private static final Logger log = LoggerFactory.getLogger(RefinerService.class);

    public RefinedNews refine(LlmClient.LlmResult result, Long newsId) {
        try {
            RefinedNews refined = new RefinedNews();
            refined.setId(newsId);

            // Process title
            String title = result.getTitle();
            if (title == null || title.isBlank()) {
                title = "Untitled News";
                log.warn("Empty title after refinement for news id={}", newsId);
            }
            refined.setTitle(normalizeWhitespace(title));

            // Process summary
            String summary = result.getSummary();
            if (summary == null || summary.isBlank()) {
                summary = "No summary available.";
                log.warn("Empty summary after refinement for news id={}", newsId);
            }
            refined.setSummary(normalizeWhitespace(summary));

            // Process tags
            String tags = result.getTags();
            if (tags == null || tags.isBlank()) {
                tags = "";
            }
            refined.setTags(normalizeWhitespace(tags));

            log.info("Successfully refined news id={}", newsId);
            return refined;
        } catch (Exception e) {
            log.error("Refinement failed for news id={}, error: {}", newsId, e.getMessage(), e);
            throw new RuntimeException("Refinement failed for news id=" + newsId, e);
        }
    }

    /**
     * Normalizes whitespace by replacing multiple spaces/newlines with a single space
     * and trimming leading/trailing whitespace.
     */
    private String normalizeWhitespace(String input) {
        if (input == null) {
            return "";
        }
        // Replace all whitespace sequences (including newlines) with a single space
        return input.replaceAll("\\s+", " ").trim();
    }
}