package org.me.agentcore.domain;

import java.math.BigDecimal;

public record OrderRequest(
        String ticker,
        OrderSide side,
        BigDecimal quantity,
        BigDecimal price
) {
}
