package org.me.agentcore.service.llm;

import org.me.agentcore.config.LlmProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GroqClient {

    private static final String GROQ_CHAT_COMPLETIONS_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final LlmProperties llmProperties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GroqClient(LlmProperties llmProperties, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.llmProperties = llmProperties;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String requestDecision(String prompt) {
        if (llmProperties.getApiKey() == null || llmProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("Groq API key is not configured");
        }

        String responseBody = webClient.post()
                .uri(GROQ_CHAT_COMPLETIONS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(llmProperties.getApiKey()))
                .bodyValue(buildRequestBody(prompt))
                .retrieve()
                .bodyToMono(String.class)
                .block(REQUEST_TIMEOUT);

        return extractAssistantContent(responseBody);
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", llmProperties.getModel(),
                "temperature", 0,
                "stream", false,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", "You return strict JSON trading decisions for a dry-run trading agent."
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );
    }

    private String extractAssistantContent(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Groq response body is empty");
        }

        JsonNode contentNode = objectMapper.readTree(responseBody)
                .path("choices")
                .path(0)
                .path("message")
                .path("content");

        if (!contentNode.isTextual() || contentNode.asString().isBlank()) {
            throw new IllegalStateException("Groq response does not contain assistant content");
        }

        return contentNode.asString();
    }
}
