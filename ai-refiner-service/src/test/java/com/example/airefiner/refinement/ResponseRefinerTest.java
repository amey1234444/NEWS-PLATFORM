package com.example.airefiner.refinement;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class ResponseRefinerTest {
    
    private ResponseRefiner responseRefiner;
    private NewsRaw sampleRawNews;
    
    @BeforeEach
    void setUp() {
        responseRefiner = new ResponseRefiner();
        sampleRawNews = new NewsRaw();
        sampleRawNews.setTitle("Sample News Title");
        sampleRawNews.setContent("This is sample news content for testing purposes.");
        sampleRawNews.setSource("test-source");
    }
    
    @Test
    void shouldRefineNormalLlmResult() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Normal Title");
        result.setSummary("Normal summary content.");
        result.setTags("news, breaking, important");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Normal Title", refined.getTitle());
        assertEquals("Normal summary content.", refined.getSummary());
        assertEquals("news, breaking, important", refined.getTags());
    }
    
    @Test
    void shouldCleanExcessiveWhitespaceInTitle() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Title	with extra spaces and  multiple   spaces");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Title with extra spaces and multiple spaces", refined.getTitle());
    }
    
    @Test
    void shouldCleanExcessiveWhitespaceInSummary() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setSummary("Summary with extra spaces and line breaks\n\nand multiple   spaces");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Summary with extra spaces and line breaks\n\nand multiple spaces", refined.getSummary());
    }
    
    @Test
    void shouldRemoveHtmlTags() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("<h1>Title with HTML</h1> and <b>formatting</b>");
        result.setSummary("Summary with <a href=\"#\">links</a> and <em>emphasis</em>.");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Title with HTML and formatting", refined.getTitle());
        assertEquals("Summary with links and emphasis.", refined.getSummary());
    }
    
    @Test
    void shouldRemoveMarkdownLinks() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Title with [link](http://example.com) and [another](http://test.com)");
        result.setSummary("Summary with [markdown](http://markdown.com) links [text](http://text.com). ");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Title with link and another", refined.getTitle());
        assertEquals("Summary with markdown links text. ", refined.getSummary());
    }
    
    @Test
    void shouldTruncateLongTitles() {
        String longTitle = "This is a very long title that exceeds the maximum allowed length for titles in the system " +
                          "and should be truncated to fit within the specified limit to maintain consistency in the display.";
        
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle(longTitle);
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertTrue(refined.getTitle().length() <= 200);
        assertFalse(refined.getTitle().endsWith("...")); // Should break at word boundary
    }
    
    @Test
    void shouldTruncateLongSummaries() {
        String longSummary = "This is a very long summary that definitely exceeds the maximum allowed length for summaries " +
                           "in the system and should be truncated to fit within the specified limit to maintain consistency " +
                           "in the display and ensure proper formatting across different components of the application.";
        
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setSummary(longSummary);
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertTrue(refined.getSummary().length() <= 400);
        assertTrue(refined.getSummary().endsWith("...")); // Should add ellipsis
    }
    
    @Test
    void shouldCleanAndStandardizeTags() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTags("news, breaking; important|politics,NEWS,tech; <b>html</b>, [link](http://test.com)");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("news, breaking, important, politics, news, tech, html, link", refined.getTags());
    }
    
    @Test
    void shouldHandleTooManyTags() {
        // Create a list of many tags
        List<String> manyTags = Arrays.asList("tag1", "tag2", "tag3", "tag4", "tag5", 
                                             "tag6", "tag7", "tag8", "tag9", "tag10", "tag11");
        String tagsString = String.join(",", manyTags);
        
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTags(tagsString);
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        String[] refinedTags = refined.getTags().split(", ");
        assertEquals(10, refinedTags.length); // Should limit to 10 tags
    }
    
    @Test
    void shouldHandleEmptyTitle() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("");
        result.setSummary("Valid summary.");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Untitled News", refined.getTitle());
        assertEquals("Valid summary.", refined.getSummary());
    }
    
    @Test
    void shouldHandleEmptySummary() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Valid title");
        result.setSummary("");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Valid title", refined.getTitle());
        assertEquals("No summary available.", refined.getSummary());
    }
    
    @Test
    void shouldHandleNullLlmResult() {
        assertThrows(NullPointerException.class, () -> {
            responseRefiner.refine(null);
        });
    }
    
    @Test
    void shouldHandleNullLlmResultWithFallback() {
        LlmClient.LlmResult refined = responseRefiner.refine(null, sampleRawNews);
        
        assertEquals("Sample News Title", refined.getTitle());
        assertEquals("This is sample news content for testing purposes.", refined.getSummary());
        assertEquals("", refined.getTags());
    }
    
    @Test
    void shouldHandleNullRawNews() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTitle("Valid title");
        result.setSummary("Valid summary");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result, null);
        
        assertEquals("Valid title", refined.getTitle());
        assertEquals("Valid summary", refined.getSummary());
    }
    
    @Test
    void shouldCreateFallbackResultWhenBothAreNull() {
        LlmClient.LlmResult refined = responseRefiner.refine(null, null);
        
        assertEquals("Untitled News", refined.getTitle());
        assertEquals("No summary available.", refined.getSummary());
        assertEquals("", refined.getTags());
    }
    
    @Test
    void shouldNormalizeLineBreaksInSummary() {
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setSummary("Summary with\n\n\nmultiple line breaks\n\nand some text");
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertEquals("Summary with\n\nmultiple line breaks\n\nand some text", refined.getSummary());
    }
    
    @Test
    void shouldHandleLongTags() {
        String longTag = "this-is-a-very-long-tag-that-definitely-exceeds-the-maximum-allowed-length-for-tags-in-the-system";
        LlmClient.LlmResult result = new LlmClient.LlmResult();
        result.setTags(longTag);
        
        LlmClient.LlmResult refined = responseRefiner.refine(result);
        
        assertTrue(refined.getTags().length() <= 100);
    }
}