package org.me.agentcore.repository;

import org.me.agentcore.domain.PortfolioState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Optional;

@Repository
public class PortfolioSnapshotJournalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PortfolioSnapshotJournalRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public long save(Long agentRunId, PortfolioState portfolioState) {
        String sql = """
                insert into portfolio_snapshots (
                    agent_run_id,
                    cash,
                    total_value,
                    positions_json,
                    snapshot_json
                )
                values (?, ?, ?, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            setNullableLong(statement, 1, agentRunId);
            statement.setBigDecimal(2, portfolioState.cash());
            statement.setBigDecimal(3, portfolioState.totalValue());
            statement.setString(4, writeJson(portfolioState.positions()));
            statement.setString(5, writeJson(portfolioState));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Optional<String> findLatestSnapshotJson() {
        String sql = """
                select snapshot_json::text
                from portfolio_snapshots
                order by created_at desc
                limit 1
                """;

        return jdbcTemplate.query(sql, resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(resultSet.getString("snapshot_json"));
        });
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
