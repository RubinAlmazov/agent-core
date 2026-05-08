package org.me.agentcore.domain;

public record RiskCheckResult(
        boolean approved,
        TradeAction finalAction,
        String reason
) {
}
