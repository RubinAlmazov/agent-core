package org.me.agentcore.service.impl;

import org.junit.jupiter.api.Test;
import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.IndicatorSnapshot;
import org.me.agentcore.domain.MarketSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultIndicatorServiceTest {

    private final DefaultIndicatorService indicatorService = new DefaultIndicatorService();

    @Test
    void calculatesIndicatorsForFlatMarket() {
        MarketSnapshot marketSnapshot = marketSnapshotWithFlatCandles();

        IndicatorSnapshot indicators = indicatorService.calculate(marketSnapshot);

        assertThat(indicators.ticker()).isEqualTo("SBER");
        assertThat(indicators.sma5()).isEqualByComparingTo("100.00");
        assertThat(indicators.sma20()).isEqualByComparingTo("100.00");
        assertThat(indicators.rsi14()).isEqualByComparingTo("100");
        assertThat(indicators.priceChange()).isEqualByComparingTo("0");
        assertThat(indicators.volumeChange()).isEqualByComparingTo("0");
        assertThat(indicators.volatility()).isEqualByComparingTo("2.00");
    }

    @Test
    void calculatesMovingAveragesForRisingMarket() {
        MarketSnapshot marketSnapshot = marketSnapshotWithRisingCandles();

        IndicatorSnapshot indicators = indicatorService.calculate(marketSnapshot);

        assertThat(indicators.sma5()).isEqualByComparingTo("28");
        assertThat(indicators.sma20()).isEqualByComparingTo("20.5");
        assertThat(indicators.rsi14()).isEqualByComparingTo("100");
        assertThat(indicators.priceChange()).isEqualByComparingTo("2900");
    }

    private MarketSnapshot marketSnapshotWithFlatCandles() {
        Instant start = Instant.parse("2026-05-15T10:00:00Z");
        List<Candle> candles = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            candles.add(new Candle(
                    "SBER",
                    start.plusSeconds(i * 60L),
                    new BigDecimal("100.00"),
                    new BigDecimal("101.00"),
                    new BigDecimal("99.00"),
                    new BigDecimal("100.00"),
                    new BigDecimal("1000.00")
            ));
        }

        return new MarketSnapshot("SBER", start.plusSeconds(29 * 60L), candles);
    }

    private MarketSnapshot marketSnapshotWithRisingCandles() {
        Instant start = Instant.parse("2026-05-15T10:00:00Z");
        List<Candle> candles = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            BigDecimal close = BigDecimal.valueOf(i);
            candles.add(new Candle(
                    "SBER",
                    start.plusSeconds((long) (i - 1) * 60),
                    close,
                    close.add(BigDecimal.ONE),
                    close.subtract(BigDecimal.ONE),
                    close,
                    new BigDecimal("1000.00")
            ));
        }

        return new MarketSnapshot("SBER", start.plusSeconds(29 * 60L), candles);
    }
}
