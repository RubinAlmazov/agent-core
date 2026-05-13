package org.me.agentcore.repository;

import org.me.agentcore.domain.PortfolioState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;

@Repository
public class PortfolioSnapshotJournalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public PortfolioSnapshotJournalRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public long save(PortfolioState portfolioState) {
        String sql = """
                insert into portfolio_snapshots (
                    cash,
                    total_value,
                    positions_json,
                    snapshot_json
                )
                values (?, ?, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setBigDecimal(1, portfolioState.cash());
            statement.setBigDecimal(2, portfolioState.totalValue());
            statement.setString(3, writeJson(portfolioState.positions()));
            statement.setString(4, writeJson(portfolioState));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
