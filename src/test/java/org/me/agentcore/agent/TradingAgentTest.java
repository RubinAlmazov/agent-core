package org.me.agentcore.agent;

import org.junit.jupiter.api.Test;
import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.OrderSide;
import org.me.agentcore.domain.OrderStatus;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeAction;
import org.me.agentcore.domain.TradeDecision;
import org.me.agentcore.service.BrokerService;
import org.me.agentcore.service.DecisionService;
import org.me.agentcore.service.IndicatorService;
import org.me.agentcore.service.JournalService;
import org.me.agentcore.service.MarketDataService;
import org.me.agentcore.service.PortfolioService;
import org.me.agentcore.service.RiskManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TradingAgentTest {

    @Test
    void placesOrderAndRecordsJournalEntriesWhenRiskApprovesTrade() {
        MarketDataService marketDataService = ticker -> marketSnapshot();
        PortfolioService portfolioService = this::portfolioState;
        IndicatorService indicatorService = marketSnapshot -> indicatorSnapshot();
        TradeDecision decision = new TradeDecision("SBER", TradeAction.BUY, new BigDecimal("2"), new BigDecimal("0.80"), "test buy");
        DecisionService decisionService = context -> decision;
        RiskCheckResult riskCheckResult = new RiskCheckResult(true, TradeAction.BUY, "approved");
        RiskManager riskManager = (tradeDecision, context) -> riskCheckResult;
        RecordingBrokerService brokerService = new RecordingBrokerService(orderResult());
        RecordingJournalService journalService = new RecordingJournalService();
        TradingAgent tradingAgent = new TradingAgent(
                marketDataService,
                portfolioService,
                indicatorService,
                decisionService,
                riskManager,
                brokerService,
                journalService
        );

        tradingAgent.runOnce("SBER", 42L);

        assertThat(brokerService.placedOrder).isNotNull();
        assertThat(brokerService.placedOrder.ticker()).isEqualTo("SBER");
        assertThat(brokerService.placedOrder.side()).isEqualTo(OrderSide.BUY);
        assertThat(brokerService.placedOrder.quantity()).isEqualByComparingTo("2");
        assertThat(brokerService.placedOrder.price()).isEqualByComparingTo("102.00");

        assertThat(journalService.recordedAgentRunId).isEqualTo(42L);
        assertThat(journalService.recordedDecision).isEqualTo(decision);
        assertThat(journalService.recordedRiskCheck).isEqualTo(riskCheckResult);
        assertThat(journalService.recordedOrderRequest).isEqualTo(brokerService.placedOrder);
        assertThat(journalService.recordedOrderResult).isEqualTo(brokerService.orderResult);
    }

    @Test
    void doesNotPlaceOrderWhenRiskBlocksTrade() {
        MarketDataService marketDataService = ticker -> marketSnapshot();
        PortfolioService portfolioService = this::portfolioState;
        IndicatorService indicatorService = marketSnapshot -> indicatorSnapshot();
        TradeDecision decision = new TradeDecision("SBER", TradeAction.BUY, BigDecimal.ONE, new BigDecimal("0.50"), "test buy");
        DecisionService decisionService = context -> decision;
        RiskCheckResult riskCheckResult = new RiskCheckResult(false, TradeAction.HOLD, "blocked");
        RiskManager riskManager = (tradeDecision, context) -> riskCheckResult;
        RecordingBrokerService brokerService = new RecordingBrokerService(orderResult());
        RecordingJournalService journalService = new RecordingJournalService();
        TradingAgent tradingAgent = new TradingAgent(
                marketDataService,
                portfolioService,
                indicatorService,
                decisionService,
                riskManager,
                brokerService,
                journalService
        );

        tradingAgent.runOnce("SBER");

        assertThat(brokerService.placedOrder).isNull();
        assertThat(journalService.recordedRiskCheck).isEqualTo(riskCheckResult);
        assertThat(journalService.recordedOrderRequest).isNull();
        assertThat(journalService.recordedOrderResult).isNull();
    }

    private MarketSnapshot marketSnapshot() {
        Instant time = Instant.parse("2026-05-15T10:00:00Z");
        return new MarketSnapshot(
                "SBER",
                time,
                List.of(
                        candle(time, "100.00"),
                        candle(time.plusSeconds(60), "102.00")
                )
        );
    }

    private Candle candle(Instant time, String close) {
        BigDecimal closePrice = new BigDecimal(close);
        return new Candle(
                "SBER",
                time,
                closePrice.subtract(BigDecimal.ONE),
                closePrice.add(BigDecimal.ONE),
                closePrice.subtract(new BigDecimal("2")),
                closePrice,
                new BigDecimal("1000.00")
        );
    }

    private IndicatorSnapshot indicatorSnapshot() {
        Instant time = Instant.parse("2026-05-15T10:00:00Z");
        return new IndicatorSnapshot(
                "SBER",
                time,
                new BigDecimal("101.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private PortfolioState portfolioState() {
        return new PortfolioState(
                Instant.parse("2026-05-15T10:00:00Z"),
                new BigDecimal("10000.00"),
                List.of(),
                new BigDecimal("10000.00")
        );
    }

    private OrderResult orderResult() {
        return new OrderResult(
                "SBER",
                OrderSide.BUY,
                new BigDecimal("2"),
                new BigDecimal("102.00"),
                new BigDecimal("102.00"),
                OrderStatus.FILLED,
                Instant.parse("2026-05-15T10:01:00Z"),
                "filled"
        );
    }

    private static class RecordingBrokerService implements BrokerService {

        private final OrderResult orderResult;
        private OrderRequest placedOrder;

        private RecordingBrokerService(OrderResult orderResult) {
            this.orderResult = orderResult;
        }

        @Override
        public OrderResult placeOrder(OrderRequest request) {
            this.placedOrder = request;
            return orderResult;
        }
    }

    private static class RecordingJournalService implements JournalService {

        private Long recordedAgentRunId;
        private TradeDecision recordedDecision;
        private RiskCheckResult recordedRiskCheck;
        private OrderRequest recordedOrderRequest;
        private OrderResult recordedOrderResult;

        @Override
        public void recordDecisionContext(Long agentRunId, DecisionContext context) {
            this.recordedAgentRunId = agentRunId;
        }

        @Override
        public long recordTradeDecision(Long agentRunId, DecisionContext context, TradeDecision decision) {
            this.recordedAgentRunId = agentRunId;
            this.recordedDecision = decision;
            return 10L;
        }

        @Override
        public long recordRiskCheck(Long agentRunId, long decisionId, RiskCheckResult result) {
            this.recordedAgentRunId = agentRunId;
            this.recordedRiskCheck = result;
            return 20L;
        }

        @Override
        public long recordOrderResult(
                Long agentRunId,
                long decisionId,
                long riskCheckId,
                OrderRequest request,
                OrderResult result
        ) {
            this.recordedAgentRunId = agentRunId;
            this.recordedOrderRequest = request;
            this.recordedOrderResult = result;
            return 30L;
        }

        @Override
        public long recordPortfolioSnapshot(Long agentRunId, PortfolioState portfolioState) {
            this.recordedAgentRunId = agentRunId;
            return 40L;
        }
    }
}
