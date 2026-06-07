package com.example.airefiner.service;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefinedResponseProcessorTest {

    private RefinedResponseProcessor processor;
    private NewsRaw raw;

    @BeforeEach
    void setUp() {
        processor = new RefinedResponseProcessor();
        raw = new NewsRaw();
        raw.setTitle("Raw Title");
        raw.setContent("Raw content of the news article that is fairly long.");
    }

    @Test
    void processValidResult() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle(" Clean Title ");
        result.setSummary("   Clean summary.   ");
        result.setTags(" tag1, tag2 ");

        LlmClient.LlmResult cleaned = processor.process(result, raw);
        assertEquals("Clean Title", cleaned.getTitle());
        assertEquals("Clean summary.", cleaned.getSummary());
        assertEquals("tag1, tag2", cleaned.getTags());
    }

    @Test
    void processResultMissingFieldsFallsBack() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("   "); // empty after trim
        result.setSummary(null);
        result.setTags("some tags");

        LlmClient.LlmResult cleaned = processor.process(result, raw);
        // fallback should use raw values
        assertEquals("Raw Title", cleaned.getTitle());
        assertEquals("Raw content of the news article that is fairly long.", cleaned.getSummary());
        assertEquals("", cleaned.getTags());
    }

    @Test
    void processNullResultFallsBack() {
        LlmClient.LlmResult cleaned = processor.process(null, raw);
        assertEquals("Raw Title", cleaned.getTitle());
        assertEquals("Raw content of the news article that is fairly long.", cleaned.getSummary());
        assertEquals("", cleaned.getTags());
    }
}
