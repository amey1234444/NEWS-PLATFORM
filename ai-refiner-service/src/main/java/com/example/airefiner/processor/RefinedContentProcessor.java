package com.example.airefiner.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Processor that validates and cleans AI generated summary content.
 * It trims whitespace, collapses multiple spaces, removes simple markdown symbols
 * and provides a fallback when the input is null or empty.
 */
@Component
public class RefinedContentProcessor {

    private static final Logger log = LoggerFactory.getLogger(RefinedContentProcessor.class);
    private static final String FALLBACK_SUMMARY = "No summary available";

    /**
     * Clean and validate the given summary.
     *
     * @param raw the raw summary from the LLM
     * @return a cleaned summary or a fallback string when the input is invalid
     */
    public String process(String raw) {
        if (raw == null || raw.isBlank()) {
            log.warn("Received empty or null summary from LLM; applying fallback.");
            return FALLBACK_SUMMARY;
        }
        // Trim leading/trailing whitespace
        String cleaned = raw.trim();
        // Collapse multiple whitespace characters into a single space
        cleaned = cleaned.replaceAll("\\s+", " ");
        // Remove simple markdown artifacts: *, #, `, > symbols
        cleaned = cleaned.replaceAll("[\\*#`>]", "");
        // Additional cleaning can be added here
        return cleaned;
    }
}
