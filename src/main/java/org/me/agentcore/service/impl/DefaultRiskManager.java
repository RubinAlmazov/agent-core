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
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class DefaultRiskManager implements RiskManager {

    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal BUY_RSI_OVERBOUGHT_LIMIT = new BigDecimal("75");
    private static final BigDecimal SELL_RSI_OVERSOLD_LIMIT = new BigDecimal("25");

    private final RiskProperties riskProperties;
    private LocalDate currentRiskDate;
    private BigDecimal dailyStartPortfolioValue;
    private int approvedTradesToday;

    public DefaultRiskManager(RiskProperties riskProperties) {
        this.riskProperties = riskProperties;
    }

    @Override
    public synchronized RiskCheckResult check(TradeDecision decision, DecisionContext context) {
        refreshDailyState(context);

        if (decision.action() == TradeAction.HOLD) {
            return approved(TradeAction.HOLD, "Decision is HOLD, no order is required");
        }

        if (decision.confidence().compareTo(riskProperties.getMinConfidence()) < 0) {
            return blocked("Blocked: confidence is below minimum threshold");
        }

        RiskCheckResult dailyLossCheck = checkDailyLossLimit(decision, context);
        if (!dailyLossCheck.approved()) {
            return dailyLossCheck;
        }

        RiskCheckResult dailyTradesCheck = checkDailyTradesLimit(decision);
        if (!dailyTradesCheck.approved()) {
            return dailyTradesCheck;
        }

        RiskCheckResult signalCheck = checkSignalQuality(decision, context);
        if (!signalCheck.approved()) {
            return signalCheck;
        }

        RiskCheckResult orderSizeCheck = checkOrderSizeLimit(decision, context);
        if (!orderSizeCheck.approved()) {
            return orderSizeCheck;
        }

        RiskCheckResult actionCheck;
        if (decision.action() == TradeAction.BUY) {
            actionCheck = checkBuyDecision(decision, context);
        } else if (decision.action() == TradeAction.SELL) {
            actionCheck = checkSellDecision(decision, context);
        } else {
            actionCheck = blocked("Blocked: unsupported trade action");
        }

        if (actionCheck.approved()) {
            approvedTradesToday++;
        }

        return actionCheck;
    }

    private RiskCheckResult checkBuyDecision(TradeDecision decision, DecisionContext context) {
        BigDecimal currentPrice = currentPrice(context);
        BigDecimal requiredCash = decision.quantity().multiply(currentPrice);
        BigDecimal availableCash = context.portfolioState().cash();

        if (requiredCash.compareTo(availableCash) > 0) {
            return blocked("Blocked: insufficient cash for buy order");
        }

        RiskCheckResult positionSizeCheck = checkPositionSizeLimit(decision, context, currentPrice);
        if (!positionSizeCheck.approved()) {
            return positionSizeCheck;
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

    private void refreshDailyState(DecisionContext context) {
        LocalDate contextDate = LocalDate.ofInstant(context.time(), ZoneOffset.UTC);

        if (currentRiskDate != null && currentRiskDate.equals(contextDate)) {
            return;
        }

        currentRiskDate = contextDate;
        dailyStartPortfolioValue = context.portfolioState().totalValue();
        approvedTradesToday = 0;
    }

    private RiskCheckResult checkDailyLossLimit(TradeDecision decision, DecisionContext context) {
        if (dailyStartPortfolioValue == null || dailyStartPortfolioValue.signum() <= 0) {
            return approved(decision.action(), "Approved: daily loss check skipped because daily start value is not positive");
        }

        BigDecimal currentPortfolioValue = context.portfolioState().totalValue();
        BigDecimal loss = dailyStartPortfolioValue.subtract(currentPortfolioValue);

        if (loss.signum() <= 0) {
            return approved(decision.action(), "Approved: portfolio is not below daily start value");
        }

        BigDecimal lossShare = loss.divide(dailyStartPortfolioValue, MATH_CONTEXT);
        if (lossShare.compareTo(riskProperties.getDailyLossLimit()) > 0) {
            return blocked("Blocked: daily loss limit exceeded");
        }

        return approved(decision.action(), "Approved: daily loss is within limit");
    }

    private RiskCheckResult checkDailyTradesLimit(TradeDecision decision) {
        if (approvedTradesToday >= riskProperties.getMaxTradesPerDay()) {
            return blocked("Blocked: maximum trades per day reached");
        }

        return approved(decision.action(), "Approved: daily trades count is within limit");
    }

    private RiskCheckResult checkSignalQuality(TradeDecision decision, DecisionContext context) {
        BigDecimal sma5 = context.indicatorSnapshot().sma5();
        BigDecimal sma20 = context.indicatorSnapshot().sma20();
        BigDecimal rsi14 = context.indicatorSnapshot().rsi14();

        if (decision.action() == TradeAction.BUY) {
            if (sma5.compareTo(sma20) <= 0) {
                return blocked("Blocked: buy signal is not confirmed by moving averages");
            }

            if (rsi14.compareTo(BUY_RSI_OVERBOUGHT_LIMIT) >= 0) {
                return blocked("Blocked: RSI is too high for buy order");
            }
        }

        if (decision.action() == TradeAction.SELL) {
            if (sma5.compareTo(sma20) >= 0) {
                return blocked("Blocked: sell signal is not confirmed by moving averages");
            }

            if (rsi14.compareTo(SELL_RSI_OVERSOLD_LIMIT) <= 0) {
                return blocked("Blocked: RSI is too low for sell order");
            }
        }

        return approved(decision.action(), "Approved: signal quality checks passed");
    }

    private RiskCheckResult checkOrderSizeLimit(TradeDecision decision, DecisionContext context) {
        BigDecimal orderValue = decision.quantity().multiply(currentPrice(context));
        BigDecimal maxOrderValue = context.portfolioState()
                .totalValue()
                .multiply(riskProperties.getMaxOrderShare());

        if (orderValue.compareTo(maxOrderValue) > 0) {
            return blocked("Blocked: order value is greater than maximum allowed order size");
        }

        return approved(decision.action(), "Approved: order size is within limit");
    }

    private RiskCheckResult checkPositionSizeLimit(
            TradeDecision decision,
            DecisionContext context,
            BigDecimal currentPrice
    ) {
        BigDecimal currentQuantity = findPosition(decision, context)
                .map(Position::quantity)
                .orElse(BigDecimal.ZERO);
        BigDecimal futureQuantity = currentQuantity.add(decision.quantity());
        BigDecimal futurePositionValue = futureQuantity.multiply(currentPrice);
        BigDecimal maxPositionValue = context.portfolioState()
                .totalValue()
                .multiply(riskProperties.getMaxPositionShare());

        if (futurePositionValue.compareTo(maxPositionValue) > 0) {
            return blocked("Blocked: future position value is greater than maximum allowed position size");
        }

        return approved(TradeAction.BUY, "Approved: position size is within limit");
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
