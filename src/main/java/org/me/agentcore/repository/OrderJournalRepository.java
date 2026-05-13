package org.me.agentcore.repository;

import org.me.agentcore.config.TradingProperties;
import org.me.agentcore.domain.OrderRequest;
import org.me.agentcore.domain.OrderResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;

@Repository
public class OrderJournalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final TradingProperties tradingProperties;

    public OrderJournalRepository(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            TradingProperties tradingProperties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.tradingProperties = tradingProperties;
    }

    public long save(long decisionId, long riskCheckId, OrderRequest request, OrderResult result) {
        String sql = """
                insert into orders (
                    decision_id,
                    risk_check_id,
                    ticker,
                    side,
                    quantity,
                    requested_price,
                    executed_price,
                    status,
                    mode,
                    order_request_json,
                    order_result_json
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, decisionId);
            statement.setLong(2, riskCheckId);
            statement.setString(3, result.ticker());
            statement.setString(4, result.side().name());
            statement.setBigDecimal(5, result.quantity());
            statement.setBigDecimal(6, result.requestedPrice());
            statement.setBigDecimal(7, result.executedPrice());
            statement.setString(8, result.status().name());
            statement.setString(9, tradingProperties.getMode().name());
            statement.setString(10, writeJson(request));
            statement.setString(11, writeJson(result));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
