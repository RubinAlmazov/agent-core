package org.me.agentcore.service.impl;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.TradeAction;
import org.me.agentcore.domain.TradeDecision;
import org.me.agentcore.service.DecisionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SimpleDecisionService implements DecisionService {

    private static final BigDecimal DEFAULT_QUANTITY = new BigDecimal("1");
    private static final BigDecimal BUY_CONFIDENCE = new BigDecimal("0.70");
    private static final BigDecimal SELL_CONFIDENCE = new BigDecimal("0.70");
    private static final BigDecimal HOLD_CONFIDENCE = new BigDecimal("0.50");
    private static final BigDecimal RSI_OVERBOUGHT_THRESHOLD = new BigDecimal("70");
    private static final BigDecimal RSI_OVERSOLD_THRESHOLD = new BigDecimal("30");

    @Override
    public TradeDecision decide(DecisionContext context) {
        IndicatorSnapshot indicators = context.indicatorSnapshot();

        if (isUptrend(indicators) && isNotOverbought(indicators)) {
            return new TradeDecision(
                    context.ticker(),
                    TradeAction.BUY,
                    DEFAULT_QUANTITY,
                    BUY_CONFIDENCE,
                    "Simple rule: short moving average is above long moving average and RSI is not overbought"
            );
        }

        if (isDowntrend(indicators) && isNotOversold(indicators)) {
            return new TradeDecision(
                    context.ticker(),
                    TradeAction.SELL,
                    DEFAULT_QUANTITY,
                    SELL_CONFIDENCE,
                    "Simple rule: short moving average is below long moving average and RSI is not oversold"
            );
        }

        return new TradeDecision(
                context.ticker(),
                TradeAction.HOLD,
                BigDecimal.ZERO,
                HOLD_CONFIDENCE,
                "Simple rule: indicators do not provide a clear trade signal"
        );
    }

    private boolean isUptrend(IndicatorSnapshot indicators) {
        return indicators.sma5().compareTo(indicators.sma20()) > 0;
    }

    private boolean isDowntrend(IndicatorSnapshot indicators) {
        return indicators.sma5().compareTo(indicators.sma20()) < 0;
    }

    private boolean isNotOverbought(IndicatorSnapshot indicators) {
        return indicators.rsi14().compareTo(RSI_OVERBOUGHT_THRESHOLD) < 0;
    }

    private boolean isNotOversold(IndicatorSnapshot indicators) {
        return indicators.rsi14().compareTo(RSI_OVERSOLD_THRESHOLD) > 0;
    }
}
