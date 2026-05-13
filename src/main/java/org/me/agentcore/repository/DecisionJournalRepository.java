package org.me.agentcore.repository;

import org.me.agentcore.domain.DecisionContext;
import org.me.agentcore.domain.TradeDecision;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;

@Repository
public class DecisionJournalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DecisionJournalRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public long save(Long agentRunId, DecisionContext context, TradeDecision decision) {
        String sql = """
                insert into decisions (
                    agent_run_id,
                    ticker,
                    action,
                    confidence,
                    reason,
                    parsed_decision_json,
                    market_snapshot_json,
                    portfolio_snapshot_json
                )
                values (?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            setNullableLong(statement, 1, agentRunId);
            statement.setString(2, decision.ticker());
            statement.setString(3, decision.action().name());
            statement.setBigDecimal(4, decision.confidence());
            statement.setString(5, decision.reason());
            statement.setString(6, writeJson(decision));
            statement.setString(7, writeJson(context.marketSnapshot()));
            statement.setString(8, writeJson(context.portfolioState()));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public List<String> findLatestDecisionJson(int limit) {
        String sql = """
                select parsed_decision_json::text
                from decisions
                order by created_at desc
                limit ?
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> resultSet.getString("parsed_decision_json"),
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
