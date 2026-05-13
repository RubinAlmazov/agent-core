package org.me.agentcore.agent;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.me.agentcore.domain.OrderSide;
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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TradingAgent {

    private final MarketDataService marketDataService;
    private final PortfolioService portfolioService;
    private final IndicatorService indicatorService;
    private final DecisionService decisionService;
    private final RiskManager riskManager;
    private final BrokerService brokerService;
    private final JournalService journalService;

    public TradingAgent(
            MarketDataService marketDataService,
            PortfolioService portfolioService,
            IndicatorService indicatorService,
            DecisionService decisionService,
            RiskManager riskManager,
            BrokerService brokerService,
            JournalService journalService
    ) {
        this.marketDataService = marketDataService;
        this.portfolioService = portfolioService;
        this.indicatorService = indicatorService;
        this.decisionService = decisionService;
        this.riskManager = riskManager;
        this.brokerService = brokerService;
        this.journalService = journalService;
    }

    public void runOnce(String ticker) {
        runOnce(ticker, null);
    }

    public void runOnce(String ticker, Long agentRunId) {
        MarketSnapshot marketSnapshot = marketDataService.getMarketSnapshot(ticker);
        PortfolioState portfolioState = portfolioService.getPortfolioState();
        DecisionContext context = new DecisionContext(
                ticker,
                Instant.now(),
                marketSnapshot,
                indicatorService.calculate(marketSnapshot),
                portfolioState
        );

        journalService.recordDecisionContext(agentRunId, context);

        TradeDecision decision = decisionService.decide(context);
        long decisionId = journalService.recordTradeDecision(agentRunId, context, decision);

        RiskCheckResult riskCheckResult = riskManager.check(decision, context);
        long riskCheckId = journalService.recordRiskCheck(agentRunId, decisionId, riskCheckResult);

        if (!riskCheckResult.approved() || riskCheckResult.finalAction() == TradeAction.HOLD) {
            return;
        }

        OrderRequest orderRequest = createOrderRequest(decision, context);
        OrderResult orderResult = brokerService.placeOrder(orderRequest);
        journalService.recordOrderResult(agentRunId, decisionId, riskCheckId, orderRequest, orderResult);
    }

    private OrderRequest createOrderRequest(TradeDecision decision, DecisionContext context) {
        return new OrderRequest(
                decision.ticker(),
                toOrderSide(decision.action()),
                decision.quantity(),
                currentPrice(context)
        );
    }

    private OrderSide toOrderSide(TradeAction action) {
        return switch (action) {
            case BUY -> OrderSide.BUY;
            case SELL -> OrderSide.SELL;
            case HOLD -> throw new IllegalArgumentException("HOLD cannot be converted to order side");
        };
    }

    private BigDecimal currentPrice(DecisionContext context) {
        return context.marketSnapshot().candles().getLast().close();
    }
}
