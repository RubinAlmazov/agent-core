package org.me.agentcore.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Candle(
        String ticker,
        Instant time,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}
