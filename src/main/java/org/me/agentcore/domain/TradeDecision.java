package org.me.agentcore.domain;

import java.math.BigDecimal;

public record TradeDecision(
        String ticker,
        TradeAction action,
        BigDecimal quantity,
        BigDecimal confidence,
        String reason
) {
}
