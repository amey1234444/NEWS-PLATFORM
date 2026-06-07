package com.example.airefiner.service;

import com.example.airefiner.client.LlmClient;
import com.example.airefiner.dto.NewsRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Processes raw LLM results, trimming whitespace, validating required fields and providing a fallback
 * when the result is incomplete or malformed.
 */
@Component
public class RefinedResponseProcessor {

    private static final Logger log = LoggerFactory.getLogger(RefinedResponseProcessor.class);

    /**
     * Clean and validate the LLM result.
     *
     * @param result the raw result from {@link LlmClient#refine(com.example.airefiner.dto.NewsRaw)}
     * @param raw    the original raw news used for fallback values
     * @return a sanitized {@link LlmClient.LlmResult}
     */
    public LlmClient.LlmResult process(LlmClient.LlmResult result, NewsRaw raw) {
        if (result == null) {
            log.warn("LLM result is null, falling back to raw news content");
            return fallback(raw);
        }
        String title = safeTrim(result.getTitle());
        String summary = safeTrim(result.getSummary());
        String tags = safeTrim(result.getTags());

        boolean missing = title.isEmpty() || summary.isEmpty();
        if (missing) {
            log.warn("LLM result missing title or summary (title='{}', summary='{}'), using fallback", title, summary);
            return fallback(raw);
        }

        LlmClient.LlmResult cleaned = new LlmClient.LlmResult();
        cleaned.setTitle(title);
        cleaned.setSummary(summary);
        cleaned.setTags(tags);
        return cleaned;
    }

    private LlmClient.LlmResult fallback(NewsRaw raw) {
        LlmClient.LlmResult fallback = new LlmClient.LlmResult();
        fallback.setTitle(raw.getTitle() != null ? raw.getTitle().trim() : "");
        fallback.setSummary(raw.getContent() != null ? raw.getContent().trim() : "");
        fallback.setTags("");
        return fallback;
    }

    private String safeTrim(String s) {
        return (s == null) ? "" : s.strip();
    }
}
