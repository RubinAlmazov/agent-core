package org.me.agentcore.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResult(
        String ticker,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal requestedPrice,
        BigDecimal executedPrice,
        OrderStatus status,
        Instant time,
        String message
) {
}
