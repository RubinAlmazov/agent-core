package org.me.agentcore.domain;

import java.time.Instant;
import java.util.List;

public record MarketSnapshot(
        String ticker,
        Instant time,
        List<Candle> candles
) {

    public MarketSnapshot {
        candles = List.copyOf(candles);
    }
}
