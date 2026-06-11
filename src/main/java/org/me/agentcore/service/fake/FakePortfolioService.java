package org.me.agentcore.service.fake;

import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.Position;
import org.me.agentcore.service.PortfolioService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class FakePortfolioService implements PortfolioService {

    private static final BigDecimal CASH = new BigDecimal("100000.00");

    @Override
    public PortfolioState getPortfolioState() {
        List<Position> positions = List.of(
                new Position("BTC", new BigDecimal("10"), new BigDecimal("300.00"), new BigDecimal("312.00")),
                new Position("SOL", new BigDecimal("20"), new BigDecimal("170.00"), new BigDecimal("168.50"))
        );

        BigDecimal totalValue = CASH.add(calculatePositionsValue(positions));

        return new PortfolioState(
                Instant.now(),
                CASH,
                positions,
                totalValue
        );
    }

    private BigDecimal calculatePositionsValue(List<Position> positions) {
        return positions.stream()
                .map(this::calculatePositionValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculatePositionValue(Position position) {
        return position.quantity().multiply(position.currentPrice());
    }
}
