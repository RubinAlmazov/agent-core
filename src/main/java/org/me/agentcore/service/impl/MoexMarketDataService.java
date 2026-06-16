package org.me.agentcore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.me.agentcore.config.ObjectMapperConfig;
import org.me.agentcore.domain.Candle;
import org.me.agentcore.domain.MarketSnapshot;
import org.me.agentcore.service.MarketDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("moex")
public class MoexMarketDataService implements MarketDataService {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public MoexMarketDataService(WebClient.Builder webClientBuilder, @Value("${moex.base-url}") String baseUrl,
                                 ObjectMapperConfig mapperConfig ) {
        webClient = webClientBuilder.baseUrl(baseUrl).build();
        objectMapper = mapperConfig.objectMapper();
    }

    @Override
    public MarketSnapshot getMarketSnapshot(String ticker) {
        JsonNode candles;

        String response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/engines/stock/markets/shares/securities/{ticker}/candles.json")
                        .queryParam("interval", 1)
                        .build(ticker))
                .retrieve()
                .bodyToMono(String.class)
                .block();


        try {
            candles = objectMapper.readTree(response);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        assert candles != null;
        return convertToMarketSnapshot(candles, ticker);
    }

    private MarketSnapshot convertToMarketSnapshot(JsonNode rawCandles, String ticker) {

        List<Candle> candles = convertToCandles(rawCandles, ticker);

        return new MarketSnapshot(
                ticker,
                candles.getLast().time(),
                candles
        );

    }

    private List<Candle> convertToCandles(JsonNode rawCandles, String ticker) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<Candle> candles = new ArrayList<>();

        JsonNode data = rawCandles.path("candles").path("data");

        for (JsonNode node : data) {

            String endTime = (node.path(7)).asText();
            LocalDateTime localDateTime = LocalDateTime.parse(endTime, formatter);
            Instant instantTime = localDateTime.toInstant(ZoneOffset.ofHours(3));

            Candle candle = new Candle(
                    ticker,
                    instantTime,
                    new BigDecimal(node.path(0).asText()),
                    new BigDecimal(node.path(2).asText()),
                    new BigDecimal(node.path(3).asText()),
                    new BigDecimal(node.path(1).asText()),
                    new BigDecimal(node.path(5).asText())
            );

            candles.add(candle);
        }

        candles = candles.subList(
                Math.max(0, candles.size() - 100),
                candles.size()
        );

        return candles;
    }
}
