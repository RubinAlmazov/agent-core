package org.me.agentcore.repository;

import org.me.agentcore.domain.AgentStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Repository
public class AgentRunRepository {

    private final JdbcTemplate jdbcTemplate;

    public AgentRunRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long startRun(AgentStatus status, String mode) {
        String sql = """
                insert into agent_runs (
                    status,
                    mode,
                    started_at
                )
                values (?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, status.name());
            statement.setString(2, mode);
            statement.setObject(3, now());
            return statement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public void stopRun(long agentRunId, AgentStatus status) {
        String sql = """
                update agent_runs
                set status = ?,
                    stopped_at = ?
                where id = ?
                """;

        jdbcTemplate.update(sql, status.name(), now(), agentRunId);
    }

    public void failRun(long agentRunId, String errorMessage) {
        String sql = """
                update agent_runs
                set status = ?,
                    stopped_at = ?,
                    error_message = ?
                where id = ?
                """;

        jdbcTemplate.update(sql, AgentStatus.FAILED.name(), now(), errorMessage, agentRunId);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
