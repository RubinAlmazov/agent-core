package org.me.agentcore.repository;

import org.me.agentcore.domain.RiskCheckResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

@Repository
public class RiskCheckJournalRepository {

    private static final List<String> RULES_CHECKED = List.of(
            "min-confidence",
            "cash-for-buy",
            "position-for-sell"
    );

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public RiskCheckJournalRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public long save(long decisionId, RiskCheckResult result) {
        String sql = """
                insert into risk_checks (
                    decision_id,
                    approved,
                    rejection_reason,
                    rules_checked_json,
                    risk_result_json
                )
                values (?, ?, ?, ?::jsonb, ?::jsonb)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setLong(1, decisionId);
            statement.setBoolean(2, result.approved());
            statement.setString(3, rejectionReason(result));
            statement.setString(4, writeJson(Map.of("rules", RULES_CHECKED)));
            statement.setString(5, writeJson(result));
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private String rejectionReason(RiskCheckResult result) {
        if (result.approved()) {
            return null;
        }

        return result.reason();
    }

    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
