package org.me.agentcore.service.llm;

import org.me.agentcore.domain.TradeAction;
import org.me.agentcore.domain.TradeDecision;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

@Component
public class LlmResponseParser {

    private static final BigDecimal MIN_CONFIDENCE = BigDecimal.ZERO;
    private static final BigDecimal MAX_CONFIDENCE = BigDecimal.ONE;

    private final ObjectMapper objectMapper;

    public LlmResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TradeDecision parse(String ticker, String rawLlmResponse) {
        if (rawLlmResponse == null || rawLlmResponse.isBlank()) {
            return hold(ticker, "LLM response is empty");
        }

        try {
            JsonNode root = objectMapper.readTree(rawLlmResponse);
            TradeAction action = parseAction(root.path("action"));
            BigDecimal quantity = parseQuantity(root.path("quantity"));
            BigDecimal confidence = parseConfidence(root.path("confidence"));
            String reason = parseReason(root.path("reason"));

            if (action == TradeAction.HOLD) {
                return new TradeDecision(ticker, TradeAction.HOLD, BigDecimal.ZERO, confidence, reason);
            }

            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                return hold(ticker, "LLM response is invalid: BUY or SELL quantity must be greater than zero");
            }

            return new TradeDecision(ticker, action, quantity, confidence, reason);
        } catch (RuntimeException exception) {
            return hold(ticker, "LLM response is invalid: " + exception.getMessage());
        }
    }

    private TradeAction parseAction(JsonNode actionNode) {
        if (!actionNode.isTextual()) {
            throw new IllegalArgumentException("action is missing or not a string");
        }

        try {
            return TradeAction.valueOf(actionNode.asString().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("action must be BUY, SELL, or HOLD");
        }
    }

    private BigDecimal parseQuantity(JsonNode quantityNode) {
        if (!quantityNode.isNumber()) {
            throw new IllegalArgumentException("quantity is missing or not a number");
        }

        if (quantityNode.decimalValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }

        return quantityNode.decimalValue();
    }

    private BigDecimal parseConfidence(JsonNode confidenceNode) {
        if (!confidenceNode.isNumber()) {
            throw new IllegalArgumentException("confidence is missing or not a number");
        }

        BigDecimal confidence = confidenceNode.decimalValue();
        if (confidence.compareTo(MIN_CONFIDENCE) < 0 || confidence.compareTo(MAX_CONFIDENCE) > 0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }

        return confidence;
    }

    private String parseReason(JsonNode reasonNode) {
        if (!reasonNode.isTextual() || reasonNode.asString().isBlank()) {
            throw new IllegalArgumentException("reason is missing or not a string");
        }

        return reasonNode.asString();
    }

    private TradeDecision hold(String ticker, String reason) {
        return new TradeDecision(ticker, TradeAction.HOLD, BigDecimal.ZERO, BigDecimal.ZERO, reason);
    }
}
