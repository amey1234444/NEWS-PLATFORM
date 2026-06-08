package com.example.airefiner.service;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.entity.RefinedNews;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RefinerServiceTest {

    private final RefinerService refinerService = new RefinerService();

    @Test
    void shouldNormalizeWhitespaceInSummary() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Test Title");
        result.setSummary("Multiple   spaces\n\nand\nnewlines\twith\ttabs");
        result.setTags("tag1, tag2,   tag3");

        RefinedNews refined = refinerService.refine(result, 1L);

        assertEquals("Test Title", refined.getTitle());
        assertEquals("Multiple spaces and newlines with tabs", refined.getSummary());
        assertEquals("tag1, tag2, tag3", refined.getTags());
    }

    @Test
    void shouldHandleNullTitle() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle(null);
        result.setSummary("Valid summary");
        result.setTags("tag1, tag2");

        RefinedNews refined = refinerService.refine(result, 2L);

        assertEquals("Untitled News", refined.getTitle());
        assertEquals("Valid summary", refined.getSummary());
        assertEquals("tag1, tag2", refined.getTags());
    }

    @Test
    void shouldHandleEmptyTitle() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("   ");
        result.setSummary("Valid summary");
        result.setTags("tag1, tag2");

        RefinedNews refined = refinerService.refine(result, 3L);

        assertEquals("Untitled News", refined.getTitle());
        assertEquals("Valid summary", refined.getSummary());
        assertEquals("tag1, tag2", refined.getTags());
    }

    @Test
    void shouldHandleNullSummary() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Valid Title");
        result.setSummary(null);
        result.setTags("tag1, tag2");

        RefinedNews refined = refinerService.refine(result, 4L);

        assertEquals("Valid Title", refined.getTitle());
        assertEquals("No summary available.", refined.getSummary());
        assertEquals("tag1, tag2", refined.getTags());
    }

    @Test
    void shouldHandleEmptySummary() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Valid Title");
        result.setSummary("   ");
        result.setTags("tag1, tag2");

        RefinedNews refined = refinerService.refine(result, 5L);

        assertEquals("Valid Title", refined.getTitle());
        assertEquals("No summary available.", refined.getSummary());
        assertEquals("tag1, tag2", refined.getTags());
    }

    @Test
    void shouldNormalizeWhitespaceInTitle() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("  Extra   Spaces  \n\nIn\nTitle  ");
        result.setSummary("Valid summary");
        result.setTags("tag1, tag2");

        RefinedNews refined = refinerService.refine(result, 6L);

        assertEquals("Extra Spaces In Title", refined.getTitle());
        assertEquals("Valid summary", refined.getSummary());
        assertEquals("tag1, tag2", refined.getTags());
    }

    @Test
    void shouldNormalizeWhitespaceInTags() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Valid Title");
        result.setSummary("Valid summary");
        result.setTags("  tag1,   tag2, tag3  \n\n  ");

        RefinedNews refined = refinerService.refine(result, 7L);

        assertEquals("Valid Title", refined.getTitle());
        assertEquals("Valid summary", refined.getSummary());
        assertEquals("tag1, tag2, tag3", refined.getTags());
    }

    @Test
    void shouldHandleNullTags() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Valid Title");
        result.setSummary("Valid summary");
        result.setTags(null);

        RefinedNews refined = refinerService.refine(result, 8L);

        assertEquals("Valid Title", refined.getTitle());
        assertEquals("Valid summary", refined.getSummary());
        assertEquals("", refined.getTags());
    }

    @Test
    void shouldHandleAllNullFields() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle(null);
        result.setSummary(null);
        result.setTags(null);

        RefinedNews refined = refinerService.refine(result, 9L);

        assertEquals("Untitled News", refined.getTitle());
        assertEquals("No summary available.", refined.getSummary());
        assertEquals("", refined.getTags());
    }

    @Test
    void shouldNormalizeMultipleNewlines() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Title");
        result.setSummary("Line1\n\n\nLine2\n\n\nLine3");
        result.setTags("tag1, tag2");

        RefinedNews refined = refinerService.refine(result, 10L);

        assertEquals("Title", refined.getTitle());
        assertEquals("Line1 Line2 Line3", refined.getSummary());
        assertEquals("tag1, tag2", refined.getTags());
    }
}