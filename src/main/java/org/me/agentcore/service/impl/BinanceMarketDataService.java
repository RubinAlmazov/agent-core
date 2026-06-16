//TODO: consider transfer to reactive (Mono\flux)
package org.me.agentcore.service.impl;

import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.service.MarketDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("binance")
public class BinanceMarketDataService implements MarketDataService {

    private final WebClient webClient;

    public BinanceMarketDataService(WebClient.Builder webClientBuilder, @Value("${binance.base-url}") String baseUrl) {
        webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public MarketSnapshot getMarketSnapshot(String ticker) {


        List<List<Object>> candles = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v3/klines")
                        .queryParam("symbol", ticker + "USDT")
                        .queryParam("interval", "1m")
                        .queryParam("limit", 200)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<List<Object>>>() {
                })
                .block();

        assert candles != null;
        String response = webClient.get()
                .uri("/api/v3/time")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println(response);

        return convertToMarketSnapshot(candles, ticker);
    }

    private MarketSnapshot convertToMarketSnapshot(List<List<Object>> rawCandles, String ticker) {

        List<Candle> candles = convertToCandles(rawCandles, ticker);

        return new MarketSnapshot(
                ticker,
                candles.getLast().time(),
                candles
                );

    }

    private List<Candle> convertToCandles(List<List<Object>> rawCandles, String ticker) {
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < rawCandles.size(); i++) {

            Candle candle = new Candle(
                    ticker,
                     Instant.ofEpochMilli(Long.parseLong(rawCandles.get(i).get(0).toString())),
                     new BigDecimal(rawCandles.get(i).get(1).toString()),
                     new BigDecimal(rawCandles.get(i).get(2).toString()),
                     new BigDecimal(rawCandles.get(i).get(3).toString()),
                     new BigDecimal(rawCandles.get(i).get(4).toString()),
                     new BigDecimal(rawCandles.get(i).get(5).toString())
            );

            candles.add(candle);
        }

        return candles;
    }
}
