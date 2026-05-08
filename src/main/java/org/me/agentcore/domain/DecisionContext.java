package org.me.agentcore.domain;

import java.time.Instant;

public record DecisionContext(
        String ticker,
        Instant time,
        MarketSnapshot marketSnapshot,
        IndicatorSnapshot indicatorSnapshot,
        PortfolioState portfolioState
) {
}
