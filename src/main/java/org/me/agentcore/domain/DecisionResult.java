package org.me.agentcore.domain;

public record DecisionResult(
        TradeDecision tradeDecision,
        String prompt,
        String rawLlmResponse
) {
}
