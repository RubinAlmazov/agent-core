package org.me.agentcore.service.impl;

import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.service.IndicatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
public class DefaultIndicatorService implements IndicatorService {

    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Override
    public IndicatorSnapshot calculate(MarketSnapshot marketSnapshot) {
        List<Candle> candles = marketSnapshot.candles();
        BigDecimal sma5 = calculateSimpleMovingAverage(candles, 5);
        BigDecimal sma20 = calculateSimpleMovingAverage(candles, 20);
        BigDecimal rsi14 = calculateRelativeStrengthIndex(candles, 14);
        BigDecimal priceChange = changePercent(first(candles).close(), last(candles).close());
        BigDecimal volumeChange = changePercent(first(candles).volume(), last(candles).volume());
        BigDecimal volatility = volatility(candles);

        return new IndicatorSnapshot(
                marketSnapshot.ticker(),
                marketSnapshot.time(),
                sma5,
                sma20,
                rsi14,
                priceChange,
                volumeChange,
                volatility
        );
    }

    private BigDecimal calculateSimpleMovingAverage(List<Candle> candles, int period) {
        List<Candle> periodCandles = candles.subList(candles.size() - period, candles.size());
        BigDecimal sum = periodCandles.stream()
                .map(Candle::close)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(period), MATH_CONTEXT);
    }

    private BigDecimal calculateRelativeStrengthIndex(List<Candle> candles, int period) {
        List<Candle> periodCandles = candles.subList(candles.size() - period - 1, candles.size());
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;

        for (int i = 1; i < periodCandles.size(); i++) {
            BigDecimal previousClose = periodCandles.get(i - 1).close();
            BigDecimal currentClose = periodCandles.get(i).close();
            BigDecimal change = currentClose.subtract(previousClose);

            if (change.signum() >= 0) {
                gains = gains.add(change);
            } else {
                losses = losses.add(change.abs());
            }
        }

        if (losses.signum() == 0) {
            return ONE_HUNDRED;
        }

        BigDecimal averageGain = gains.divide(BigDecimal.valueOf(period), MATH_CONTEXT);
        BigDecimal averageLoss = losses.divide(BigDecimal.valueOf(period), MATH_CONTEXT);
        BigDecimal relativeStrength = averageGain.divide(averageLoss, MATH_CONTEXT);
        BigDecimal divisor = BigDecimal.ONE.add(relativeStrength);

        return ONE_HUNDRED.subtract(ONE_HUNDRED.divide(divisor, MATH_CONTEXT));
    }

    private BigDecimal changePercent(BigDecimal firstValue, BigDecimal lastValue) {
        if (firstValue.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return lastValue.subtract(firstValue)
                .divide(firstValue, MATH_CONTEXT)
                .multiply(ONE_HUNDRED);
    }

    private BigDecimal volatility(List<Candle> candles) {
        BigDecimal sum = BigDecimal.ZERO;

        for (Candle candle : candles) {
            BigDecimal range = candle.high().subtract(candle.low());
            BigDecimal rangePercent = range.divide(candle.close(), MATH_CONTEXT).multiply(ONE_HUNDRED);
            sum = sum.add(rangePercent);
        }

        return sum.divide(BigDecimal.valueOf(candles.size()), MATH_CONTEXT);
    }

    private Candle first(List<Candle> candles) {
        return candles.getFirst();
    }

    private Candle last(List<Candle> candles) {
        return candles.getLast();
    }
}
