alter table risk_checks
    add column agent_run_id bigint references agent_runs (id);

create index idx_risk_checks_agent_run_id_created_at
    on risk_checks (agent_run_id, created_at desc);
