CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE IF NOT EXISTS orders.orders (
    id UUID PRIMARY KEY,
    book_id UUID NOT NULL,
    user_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_snapshot NUMERIC(10, 2) NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_orders_book_id ON orders.orders (book_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders.orders (user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders.orders (status);
