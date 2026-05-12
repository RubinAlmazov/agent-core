package org.me.agentcore.service.impl;

import org.me.agentcore.config.RiskProperties;
import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.Position;
import org.me.agentcore.domain.RiskCheckResult;
import org.me.agentcore.domain.TradeAction;
import org.me.agentcore.domain.TradeDecision;
import org.me.agentcore.service.RiskManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DefaultRiskManager implements RiskManager {

    private final RiskProperties riskProperties;

    public DefaultRiskManager(RiskProperties riskProperties) {
        this.riskProperties = riskProperties;
    }

    @Override
    public RiskCheckResult check(TradeDecision decision, DecisionContext context) {
        if (decision.action() == TradeAction.HOLD) {
            return approved(TradeAction.HOLD, "Decision is HOLD, no order is required");
        }

        if (decision.confidence().compareTo(riskProperties.getMinConfidence()) < 0) {
            return blocked("Blocked: confidence is below minimum threshold");
        }

        if (decision.action() == TradeAction.BUY) {
            return checkBuyDecision(decision, context);
        }

        if (decision.action() == TradeAction.SELL) {
            return checkSellDecision(decision, context);
        }

        return blocked("Blocked: unsupported trade action");
    }

    private RiskCheckResult checkBuyDecision(TradeDecision decision, DecisionContext context) {
        BigDecimal currentPrice = currentPrice(context);
        BigDecimal requiredCash = decision.quantity().multiply(currentPrice);
        BigDecimal availableCash = context.portfolioState().cash();

        if (requiredCash.compareTo(availableCash) > 0) {
            return blocked("Blocked: insufficient cash for buy order");
        }

        return approved(TradeAction.BUY, "Approved: buy order passed risk checks");
    }

    private RiskCheckResult checkSellDecision(TradeDecision decision, DecisionContext context) {
        Optional<Position> position = findPosition(decision, context);

        if (position.isEmpty()) {
            return blocked("Blocked: no position available for sell order");
        }

        if (position.get().quantity().compareTo(decision.quantity()) < 0) {
            return blocked("Blocked: sell quantity is greater than current position");
        }

        return approved(TradeAction.SELL, "Approved: sell order passed risk checks");
    }

    private BigDecimal currentPrice(DecisionContext context) {
        Candle lastCandle = context.marketSnapshot().candles().getLast();
        return lastCandle.close();
    }

    private Optional<Position> findPosition(TradeDecision decision, DecisionContext context) {
        return context.portfolioState().positions().stream()
                .filter(position -> position.ticker().equals(decision.ticker()))
                .findFirst();
    }

    private RiskCheckResult approved(TradeAction finalAction, String reason) {
        return new RiskCheckResult(true, finalAction, reason);
    }

    private RiskCheckResult blocked(String reason) {
        return new RiskCheckResult(false, TradeAction.HOLD, reason);
    }
}
