package com.example.airefiner.client;

import com.example.airefiner.dto.NewsRaw;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class LlmClient {

    private final Logger log = LoggerFactory.getLogger(LlmClient.class);
    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String model;

    public LlmClient(@Value("${llm.api.url:https://api.openai.com/v1/chat/completions}") String apiUrl,
                     @Value("${llm.api.key:}") String apiKey,
                     @Value("${llm.model:gpt-3.5-turbo}") String model) {
        this.model = model;
        // allow overriding the API URL via OPENROUTER_API_URL for OpenRouter users
        String envApi = System.getenv("OPENROUTER_API_URL");
        if (envApi != null && !envApi.isBlank()) {
            apiUrl = envApi;
            log.info("Using OpenRouter API URL from OPENROUTER_API_URL");
        }

        WebClient.Builder b = WebClient.builder().baseUrl(apiUrl).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // pick API key in this order: llm.api.key -> OPENROUTER_API_KEY -> OPENAI_API_KEY
        String key = apiKey;
        String orKey = System.getenv("OPENROUTER_API_KEY");
        if (orKey != null && !orKey.isBlank()) {
            key = orKey;
            log.info("Using OpenRouter API key from OPENROUTER_API_KEY");
        } else if (key == null || key.isBlank()) {
            String env = System.getenv("OPENAI_API_KEY");
            if (env != null && !env.isBlank()) key = env;
        }

        if (key != null && !key.isBlank()) {
            b.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key);
        } else {
            log.warn("No LLM API key configured (llm.api.key, OPENROUTER_API_KEY, or OPENAI_API_KEY); LLM requests may be unauthorized.");
        }

        this.webClient = b.build();
    }

    public LlmResult refine(NewsRaw raw) {
        try {
            String prompt = buildPrompt(raw);
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            // build messages safely as JSON nodes to avoid issues with unescaped characters
            ArrayNode messages = mapper.createArrayNode();
            ObjectNode sys = mapper.createObjectNode();
            sys.put("role", "system");
            sys.put("content", "You are a concise news formatter. Output only valid JSON with keys: title, summary, tags.");
            messages.add(sys);
            ObjectNode user = mapper.createObjectNode();
            user.put("role", "user");
            user.put("content", prompt);
            messages.add(user);
            body.set("messages", messages);
            body.put("temperature", 0.2);
            body.put("max_tokens", 600);

            String resp = webClient.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (resp == null) throw new RuntimeException("Empty LLM response");
            JsonNode root = mapper.readTree(resp);
            // extract chat result text (support common variants)
            JsonNode contentNode = root.at("/choices/0/message/content");
            if (contentNode.isMissingNode()) contentNode = root.at("/choices/0/text");
            String text = contentNode.isMissingNode() ? root.toString() : contentNode.asText();

            // attempt to find JSON in the text
            String json = extractJson(text);
            if (json == null) {
                // fallback: create minimal json
                json = mapper.createObjectNode()
                        .put("title", raw.getTitle() == null ? "" : raw.getTitle())
                        .put("summary", truncate(raw.getContent(), 400))
                        .put("tags", "")
                        .toString();
            }

            JsonNode parsed = mapper.readTree(json);
            LlmResult out = new LlmResult();
            out.setTitle(parsed.path("title").asText(""));
            out.setSummary(parsed.path("summary").asText(""));
            out.setTags(parsed.path("tags").asText(""));
            return out;
        } catch (Exception e) {
            log.warn("LLM refine failed: {}", e.getMessage());
            LlmResult fallback = new LlmResult();
            fallback.setTitle(raw.getTitle() == null ? "" : raw.getTitle());
            fallback.setSummary(raw.getContent() == null ? "" : truncate(raw.getContent(), 400));
            fallback.setTags("");
            return fallback;
        }
    }

    private static String buildPrompt(NewsRaw raw) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(raw.getTitle() == null ? "" : raw.getTitle()).append("\n\n");
        sb.append("Content: \n").append(raw.getContent() == null ? "" : raw.getContent()).append("\n\n");
        sb.append("Source: ").append(raw.getSource() == null ? "" : raw.getSource()).append("\n\n");
        sb.append("Produce a short title, a 2-3 sentence summary, and comma-separated tags in JSON format.");
        return sb.toString();
    }

    private static String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1).trim();
        return null;
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    public static class LlmResult {
        private String title;
        private String summary;
        private String tags;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }
}
