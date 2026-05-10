CREATE TABLE IF NOT EXISTS orders.idempotency_keys (
    key VARCHAR(255) PRIMARY KEY,
    response_body TEXT,
    status_code INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
