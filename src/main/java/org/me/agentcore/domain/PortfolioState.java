package org.me.agentcore.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PortfolioState(
        Instant time,
        BigDecimal cash,
        List<Position> positions,
        BigDecimal totalValue
) {

    public PortfolioState {
        positions = List.copyOf(positions);
    }
}
