create table agent_runs
(
    id           bigserial primary key,
    status       varchar(32) not null,
    mode         varchar(32) not null,
    started_at   timestamptz not null default now(),
    stopped_at   timestamptz,
    error_message text,
    created_at   timestamptz not null default now()
);

create table decisions
(
    id                      bigserial primary key,
    agent_run_id            bigint references agent_runs (id),
    ticker                  varchar(32) not null,
    action                  varchar(16) not null,
    confidence              numeric(5, 4) not null,
    reason                  text,
    prompt                  text,
    raw_llm_response        text,
    parsed_decision_json    jsonb,
    market_snapshot_json    jsonb not null,
    portfolio_snapshot_json jsonb not null,
    created_at              timestamptz not null default now()
);

create table risk_checks
(
    id                  bigserial primary key,
    decision_id         bigint references decisions (id),
    approved            boolean not null,
    rejection_reason    text,
    rules_checked_json  jsonb not null,
    risk_result_json    jsonb,
    created_at          timestamptz not null default now()
);

create table orders
(
    id                  bigserial primary key,
    agent_run_id        bigint references agent_runs (id),
    decision_id         bigint references decisions (id),
    risk_check_id       bigint references risk_checks (id),
    ticker              varchar(32) not null,
    side                varchar(16) not null,
    quantity            numeric(20, 8) not null,
    requested_price     numeric(20, 8),
    executed_price      numeric(20, 8),
    status              varchar(32) not null,
    mode                varchar(32) not null,
    external_order_id   varchar(128),
    order_request_json  jsonb not null,
    order_result_json   jsonb,
    created_at          timestamptz not null default now()
);

create table portfolio_snapshots
(
    id                  bigserial primary key,
    agent_run_id        bigint references agent_runs (id),
    cash                numeric(20, 8) not null,
    total_value         numeric(20, 8) not null,
    daily_pnl           numeric(20, 8),
    positions_json      jsonb not null,
    snapshot_json       jsonb not null,
    created_at          timestamptz not null default now()
);

create index idx_decisions_created_at on decisions (created_at desc);
create index idx_decisions_ticker_created_at on decisions (ticker, created_at desc);
create index idx_orders_created_at on orders (created_at desc);
create index idx_portfolio_snapshots_created_at on portfolio_snapshots (created_at desc);
