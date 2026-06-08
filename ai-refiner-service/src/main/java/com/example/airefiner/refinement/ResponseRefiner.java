package com.example.airefiner.refinement;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
public class ResponseRefiner {
    
    private static final Logger log = LoggerFactory.getLogger(ResponseRefiner.class);
    
    // Maximum lengths for different fields
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_SUMMARY_LENGTH = 400;
    private static final int MAX_TAG_LENGTH = 100;
    private static final int MAX_TAGS_COUNT = 10;
    
    // Patterns for cleaning
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\s+");
    private static final Pattern EXTRA_LINE_BREAKS_PATTERN = Pattern.Pattern.compile("\n{2,}");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\[(.*?)\]\(.*?\)");
    
    /**
     * Refines an LLM result by validating, cleaning, and standardizing the content
     * @param result The LLM result to refine
     * @return The refined LLM result
     */
    public LlmClient.LlmResult refine(LlmClient.LlmResult result) {
        if (result == null) {
            log.warn("Received null LLM result, creating fallback");
            return createFallbackResult(null);
        }
        
        try {
            // Validate and clean title
            String title = cleanAndValidateTitle(result.getTitle());
            
            // Validate and clean summary
            String summary = cleanAndValidateSummary(result.getSummary());
            
            // Validate and clean tags
            String tags = cleanAndValidateTags(result.getTags());
            
            // Create refined result
            LlmClient.LlmResult refined = new LlmClient.LlmResult();
            refined.setTitle(title);
            refined.setSummary(summary);
            refined.setTags(tags);
            
            return refined;
        } catch (Exception e) {
            log.warn("Error refining LLM result: {}", e.getMessage());
            return createFallbackResult(result);
        }
    }
    
    /**
     * Refines an LLM result with associated raw news data
     * @param result The LLM result to refine
     * @param raw The raw news data
     * @return The refined LLM result
     */
    public LlmClient.LlmResult refine(LlmClient.LlmResult result, NewsRaw raw) {
        if (result == null && raw == null) {
            log.warn("Received null LLM result and raw news, creating fallback");
            return new LlmClient.LlmResult();
        }
        
        // Use fallback if result is null but raw is available
        if (result == null) {
            log.warn("Received null LLM result, using raw news data");
            return createFallbackResult(raw);
        }
        
        // Otherwise use normal refinement
        return refine(result);
    }
    
    /**
     * Cleans and validates the title field
     * @param title The original title
     * @return The cleaned and validated title
     */
    private String cleanAndValidateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            log.warn("Empty title received, using fallback");
            return "Untitled News";
        }
        
        // Remove HTML tags
        String cleaned = HTML_TAG_PATTERN.matcher(title).replaceAll("");
        
        // Clean whitespace
        cleaned = WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ").trim();
        
        // Remove markdown links
        cleaned = MARKDOWN_LINK_PATTERN.matcher(cleaned).replaceAll("$1");
        
        // Truncate if too long
        if (cleaned.length() > MAX_TITLE_LENGTH) {
            log.warn("Title too long ({} chars), truncating to {}", cleaned.length(), MAX_TITLE_LENGTH);
            cleaned = cleaned.substring(0, MAX_TITLE_LENGTH);
            // Try to break at word boundary
            int lastSpace = cleaned.lastIndexOf(' ');
            if (lastSpace > 0) {
                cleaned = cleaned.substring(0, lastSpace);
            }
        }
        
        return cleaned;
    }
    
    /**
     * Cleans and validates the summary field
     * @param summary The original summary
     * @return The cleaned and validated summary
     */
    private String cleanAndValidateSummary(String summary) {
        if (summary == null || summary.trim().isEmpty()) {
            log.warn("Empty summary received, using fallback");
            return "No summary available.";
        }
        
        // Remove HTML tags
        String cleaned = HTML_TAG_PATTERN.matcher(summary).replaceAll("");
        
        // Normalize line breaks
        cleaned = EXTRA_LINE_BREAKS_PATTERN.matcher(cleaned).replaceAll("\n\n");
        
        // Clean whitespace
        cleaned = WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ").trim();
        
        // Remove markdown links
        cleaned = MARKDOWN_LINK_PATTERN.matcher(cleaned).replaceAll("$1");
        
        // Truncate if too long
        if (cleaned.length() > MAX_SUMMARY_LENGTH) {
            log.warn("Summary too long ({} chars), truncating to {}", cleaned.length(), MAX_SUMMARY_LENGTH);
            cleaned = cleaned.substring(0, MAX_SUMMARY_LENGTH);
            // Add ellipsis if truncated
            if (!cleaned.endsWith("...")) {
                cleaned += "...";
            }
        }
        
        return cleaned;
    }
    
    /**
     * Cleans and validates the tags field
     * @param tags The original tags
     * @return The cleaned and validated tags
     */
    private String cleanAndValidateTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return "";
        }
        
        // Split by common delimiters
        String[] tagArray = tags.split(",|;|\\|");
        List<String> tagList = Arrays.asList(tagArray);
        
        // Clean each tag
        List<String> cleanedTags = tagList.stream()
            .map(tag -> tag.trim())
            .filter(tag -> !tag.isEmpty())
            .map(tag -> HTML_TAG_PATTERN.matcher(tag).replaceAll(""))
            .map(tag -> WHITESPACE_PATTERN.matcher(tag).replaceAll("-").toLowerCase())
            .filter(tag -> tag.length() <= MAX_TAG_LENGTH)
            .distinct()
            .limit(MAX_TAGS_COUNT)
            .toList();
        
        // Join back to string
        return String.join(", ", cleanedTags);
    }
    
    /**
     * Creates a fallback result from raw news data
     * @param raw The raw news data
     * @return A fallback LLM result
     */
    private LlmClient.LlmResult createFallbackResult(NewsRaw raw) {
        LlmClient.LlmResult fallback = new LlmClient.LlmResult();
        
        if (raw != null) {
            fallback.setTitle(raw.getTitle() != null ? cleanAndValidateTitle(raw.getTitle()) : "Untitled News");
            fallback.setSummary(raw.getContent() != null ? cleanAndValidateSummary(truncate(raw.getContent(), MAX_SUMMARY_LENGTH)) : "No summary available.");
        } else {
            fallback.setTitle("Untitled News");
            fallback.setSummary("No summary available.");
        }
        
        fallback.setTags("");
        log.info("Created fallback result");
        return fallback;
    }
    
    /**
     * Helper method to truncate text
     * @param text The text to truncate
     * @param maxLength The maximum length
     * @return The truncated text
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}