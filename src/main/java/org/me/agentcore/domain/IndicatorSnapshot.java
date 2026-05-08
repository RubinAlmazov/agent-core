package org.me.agentcore.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record IndicatorSnapshot(
        String ticker,
        Instant time,
        BigDecimal sma5,
        BigDecimal sma20,
        BigDecimal rsi14,
        BigDecimal priceChange,
        BigDecimal volumeChange,
        BigDecimal volatility
) {
}
