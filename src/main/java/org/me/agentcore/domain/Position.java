package org.me.agentcore.domain;

import java.math.BigDecimal;

public record Position(
        String ticker,
        BigDecimal quantity,
        BigDecimal averageEntryPrice,
        BigDecimal currentPrice
) {
}
