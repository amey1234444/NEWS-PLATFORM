package com.example.airefiner.client;

import com.example.airefiner.dto.NewsRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simple stub implementation of the LLM client used for unit testing and to avoid pulling in reactive WebClient dependencies.
 * It returns a basic {@link LlmResult} based on the incoming {@link NewsRaw}.
 */
@Component
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    public LlmResult refine(NewsRaw news) {
        // In a real implementation this would call an external LLM service.
        // For now we simply echo the title and content as the refined result.
        String title = news != null && news.getTitle() != null ? news.getTitle() : "Untitled";
        String summary = news != null && news.getContent() != null ? news.getContent() : "";
        log.info("Stub LlmClient returning title='{}' summary length={}", title, summary.length());
        return new LlmResult(title, summary, "");
    }

    public static class LlmResult {
        private final String title;
        private final String summary;
        private final String tags;

        public LlmResult(String title, String summary, String tags) {
            this.title = title;
            this.summary = summary;
            this.tags = tags;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }

        public String getTags() {
            return tags;
        }
    }
}
