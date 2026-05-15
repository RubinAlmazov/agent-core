package org.me.agentcore.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.me.agentcore.config.RiskProperties;
import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.Position;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeAction;
import org.me.agentcore.domain.TradeDecision;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRiskManagerTest {

    private DefaultRiskManager riskManager;

    @BeforeEach
    void setUp() {
        RiskProperties riskProperties = new RiskProperties();
        riskProperties.setMinConfidence(new BigDecimal("0.65"));
        riskManager = new DefaultRiskManager(riskProperties);
    }

    @Test
    void approvesHoldWithoutAdditionalChecks() {
        RiskCheckResult result = riskManager.check(decision(TradeAction.HOLD, "0", "0.10"), contextWithCashAndPositions("0"));

        assertThat(result.approved()).isTrue();
        assertThat(result.finalAction()).isEqualTo(TradeAction.HOLD);
    }

    @Test
    void blocksTradeWhenConfidenceIsBelowMinimumThreshold() {
        RiskCheckResult result = riskManager.check(decision(TradeAction.BUY, "1", "0.64"), contextWithCashAndPositions("1000"));

        assertThat(result.approved()).isFalse();
        assertThat(result.finalAction()).isEqualTo(TradeAction.HOLD);
        assertThat(result.reason()).contains("confidence");
    }

    @Test
    void blocksBuyWhenCashIsInsufficient() {
        RiskCheckResult result = riskManager.check(decision(TradeAction.BUY, "11", "0.80"), contextWithCashAndPositions("1000"));

        assertThat(result.approved()).isFalse();
        assertThat(result.finalAction()).isEqualTo(TradeAction.HOLD);
        assertThat(result.reason()).contains("insufficient cash");
    }

    @Test
    void approvesBuyWhenCashIsEnough() {
        RiskCheckResult result = riskManager.check(decision(TradeAction.BUY, "10", "0.80"), contextWithCashAndPositions("1000"));

        assertThat(result.approved()).isTrue();
        assertThat(result.finalAction()).isEqualTo(TradeAction.BUY);
    }

    @Test
    void blocksSellWhenPositionDoesNotExist() {
        RiskCheckResult result = riskManager.check(decision("LKOH", TradeAction.SELL, "1", "0.80"), contextWithCashAndPositions("1000"));

        assertThat(result.approved()).isFalse();
        assertThat(result.finalAction()).isEqualTo(TradeAction.HOLD);
        assertThat(result.reason()).contains("no position");
    }

    @Test
    void blocksSellWhenQuantityExceedsCurrentPosition() {
        RiskCheckResult result = riskManager.check(decision(TradeAction.SELL, "6", "0.80"), contextWithCashAndPositions("1000"));

        assertThat(result.approved()).isFalse();
        assertThat(result.finalAction()).isEqualTo(TradeAction.HOLD);
        assertThat(result.reason()).contains("greater than current position");
    }

    @Test
    void approvesSellWhenPositionIsEnough() {
        RiskCheckResult result = riskManager.check(decision(TradeAction.SELL, "5", "0.80"), contextWithCashAndPositions("1000"));

        assertThat(result.approved()).isTrue();
        assertThat(result.finalAction()).isEqualTo(TradeAction.SELL);
    }

    private TradeDecision decision(TradeAction action, String quantity, String confidence) {
        return decision("SBER", action, quantity, confidence);
    }

    private TradeDecision decision(String ticker, TradeAction action, String quantity, String confidence) {
        return new TradeDecision(
                ticker,
                action,
                new BigDecimal(quantity),
                new BigDecimal(confidence),
                "test decision"
        );
    }

    private DecisionContext contextWithCashAndPositions(String cash) {
        Instant time = Instant.parse("2026-05-15T10:00:00Z");
        MarketSnapshot marketSnapshot = new MarketSnapshot(
                "SBER",
                time,
                List.of(new Candle(
                        "SBER",
                        time,
                        new BigDecimal("100.00"),
                        new BigDecimal("101.00"),
                        new BigDecimal("99.00"),
                        new BigDecimal("100.00"),
                        new BigDecimal("1000.00")
                ))
        );
        IndicatorSnapshot indicatorSnapshot = new IndicatorSnapshot(
                "SBER",
                time,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
        PortfolioState portfolioState = new PortfolioState(
                time,
                new BigDecimal(cash),
                List.of(new Position("SBER", new BigDecimal("5"), new BigDecimal("95.00"), new BigDecimal("100.00"))),
                new BigDecimal(cash).add(new BigDecimal("500.00"))
        );

        return new DecisionContext("SBER", time, marketSnapshot, indicatorSnapshot, portfolioState);
    }
}
