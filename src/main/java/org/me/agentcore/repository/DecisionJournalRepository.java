package org.me.agentcore.repository;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.TradeDecision;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;

@Repository
public class DecisionJournalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DecisionJournalRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public long save(DecisionContext context, TradeDecision decision) {
        String sql = """
                insert into decisions (
                    ticker,
                    action,
                    confidence,
                    reason,
                    parsed_decision_json,
                    market_snapshot_json,
                    portfolio_snapshot_json
                )
                values (?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, decision.ticker());
            statement.setString(2, decision.action().name());
            statement.setBigDecimal(3, decision.confidence());
            statement.setString(4, decision.reason());
            statement.setString(5, writeJson(decision));
            statement.setString(6, writeJson(context.marketSnapshot()));
            statement.setString(7, writeJson(context.portfolioState()));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
