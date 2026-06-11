package org.me.agentcore.service.fake;

import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.service.MarketDataService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("dev")
public class FakeMarketDataService implements MarketDataService {


    private static final int CANDLE_COUNT = 30;

    @Override
    public MarketSnapshot getMarketSnapshot(String ticker) {
        Instant now = Instant.now();
        List<Candle> candles = new ArrayList<>();
        BigDecimal basePrice = basePriceFor(ticker);

        for (int i = 0; i < CANDLE_COUNT; i++) {
            BigDecimal trend = BigDecimal.valueOf(i).multiply(new BigDecimal("0.35"));
            BigDecimal wave = BigDecimal.valueOf((i % 5) - 2).multiply(new BigDecimal("0.20"));
            BigDecimal close = basePrice.add(trend).add(wave);
            BigDecimal open = close.subtract(new BigDecimal("0.15"));
            BigDecimal high = close.add(new BigDecimal("0.40"));
            BigDecimal low = open.subtract(new BigDecimal("0.35"));
            BigDecimal volume = BigDecimal.valueOf(10_000L + i * 350L);
            Instant candleTime = now.minusSeconds((long) (CANDLE_COUNT - 1 - i) * 60);

            candles.add(new Candle(ticker, candleTime, open, high, low, close, volume));
        }

        return new MarketSnapshot(ticker, now, candles);
    }

    private BigDecimal basePriceFor(String ticker) {
        return switch (ticker) {
            case "SBER" -> new BigDecimal("300.00");
            case "GAZP" -> new BigDecimal("170.00");
            case "LKOH" -> new BigDecimal("7500.00");
            default -> new BigDecimal("100.00");
        };
    }
}
