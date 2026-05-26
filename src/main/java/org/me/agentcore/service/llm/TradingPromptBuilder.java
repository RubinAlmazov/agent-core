package org.me.agentcore.service.llm;

import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.PortfolioState;
import org.me.agentcore.domain.Position;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradingPromptBuilder {

    private static final int MAX_CANDLES_IN_PROMPT = 10;

    public String buildPrompt(DecisionContext context) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a trading decision assistant for a dry-run trading agent.\n");
        prompt.append("Use only the data provided below.\n");
        prompt.append("Return exactly one JSON object and no additional text.\n\n");

        appendDecisionRules(prompt);
        appendRequiredJsonFormat(prompt);
        appendMarketData(prompt, context);
        appendIndicators(prompt, context.indicatorSnapshot());
        appendPortfolio(prompt, context.portfolioState());

        return prompt.toString();
    }

    private void appendDecisionRules(StringBuilder prompt) {
        prompt.append("Decision rules:\n");
        prompt.append("- Allowed actions: BUY, SELL, HOLD.\n");
        prompt.append("- Prefer HOLD when the signal is weak, unclear, contradictory, or risky.\n");
        prompt.append("- BUY requires a clear bullish signal.\n");
        prompt.append("- SELL requires a clear bearish signal and an existing position.\n");
        prompt.append("- confidence must be a number from 0.0 to 1.0.\n");
        prompt.append("- quantity must be greater than 0 for BUY or SELL and 0 for HOLD.\n\n");
    }

    private void appendRequiredJsonFormat(StringBuilder prompt) {
        prompt.append("Required JSON format:\n");
        prompt.append("{\n");
        prompt.append("  \"action\": \"HOLD\",\n");
        prompt.append("  \"quantity\": 0,\n");
        prompt.append("  \"confidence\": 0.0,\n");
        prompt.append("  \"reason\": \"Short explanation of the decision\"\n");
        prompt.append("}\n\n");
    }

    private void appendMarketData(StringBuilder prompt, DecisionContext context) {
        prompt.append("Market data:\n");
        prompt.append("ticker: ").append(context.ticker()).append('\n');
        prompt.append("decisionTime: ").append(context.time()).append('\n');
        prompt.append("candles:\n");

        List<Candle> candles = context.marketSnapshot().candles();
        int startIndex = Math.max(0, candles.size() - MAX_CANDLES_IN_PROMPT);   
        for (int index = startIndex; index < candles.size(); index++) {
            Candle candle = candles.get(index);
            prompt.append("- time=").append(candle.time())
                    .append(", open=").append(candle.open())
                    .append(", high=").append(candle.high())
                    .append(", low=").append(candle.low())
                    .append(", close=").append(candle.close())
                    .append(", volume=").append(candle.volume())
                    .append('\n');
        }

        prompt.append('\n');
    }

    private void appendIndicators(StringBuilder prompt, IndicatorSnapshot indicators) {
        prompt.append("Indicators:\n");
        prompt.append("sma5: ").append(indicators.sma5()).append('\n');
        prompt.append("sma20: ").append(indicators.sma20()).append('\n');
        prompt.append("rsi14: ").append(indicators.rsi14()).append('\n');
        prompt.append("priceChange: ").append(indicators.priceChange()).append('\n');
        prompt.append("volumeChange: ").append(indicators.volumeChange()).append('\n');
        prompt.append("volatility: ").append(indicators.volatility()).append("\n\n");
    }

    private void appendPortfolio(StringBuilder prompt, PortfolioState portfolioState) {
        prompt.append("Portfolio:\n");
        prompt.append("time: ").append(portfolioState.time()).append('\n');
        prompt.append("cash: ").append(portfolioState.cash()).append('\n');
        prompt.append("totalValue: ").append(portfolioState.totalValue()).append('\n');
        prompt.append("positions:\n");

        if (portfolioState.positions().isEmpty()) {
            prompt.append("- none\n");
        } else {
            for (Position position : portfolioState.positions()) {
                prompt.append("- ticker=").append(position.ticker())
                        .append(", quantity=").append(position.quantity())
                        .append(", averageEntryPrice=").append(position.averageEntryPrice())
                        .append(", currentPrice=").append(position.currentPrice())
                        .append('\n');
            }
        }
    }
}
