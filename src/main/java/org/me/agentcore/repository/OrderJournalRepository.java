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
import java.sql.Types;
import java.util.List;

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

    public long save(Long agentRunId, long decisionId, long riskCheckId, OrderRequest request, OrderResult result) {
        String sql = """
                insert into orders (
                    agent_run_id,
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
                values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            setNullableLong(statement, 1, agentRunId);
            statement.setLong(2, decisionId);
            statement.setLong(3, riskCheckId);
            statement.setString(4, result.ticker());
            statement.setString(5, result.side().name());
            statement.setBigDecimal(6, result.quantity());
            statement.setBigDecimal(7, result.requestedPrice());
            statement.setBigDecimal(8, result.executedPrice());
            statement.setString(9, result.status().name());
            statement.setString(10, tradingProperties.getMode().name());
            statement.setString(11, writeJson(request));
            statement.setString(12, writeJson(result));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public List<String> findLatestOrderJson(int limit) {
        String sql = """
                select order_result_json::text
                from orders
                order by created_at desc
                limit ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> resultSet.getString("order_result_json"),
                limit
        );
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private void setNullableLong(PreparedStatement statement, int parameterIndex, Long value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.BIGINT);
            return;
        }

        statement.setLong(parameterIndex, value);
    }
}
