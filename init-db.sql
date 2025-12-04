-- Criar tabelas de Payment
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    payer_id UUID NOT NULL,
    payer_email VARCHAR(255),
    payee_id UUID NOT NULL,
    amount NUMERIC(38, 2) NOT NULL,
    currency VARCHAR(255) NOT NULL DEFAULT 'BRL',
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payment_events (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    event_type VARCHAR(255) NOT NULL,
    status VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Criar tabelas de Merchant
CREATE TABLE IF NOT EXISTS merchants (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    balance NUMERIC(38, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(255) NOT NULL DEFAULT 'BRL'
);

CREATE TABLE IF NOT EXISTS merchant_events (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL REFERENCES merchants(id),
    event_type VARCHAR(255) NOT NULL,
    event_date_time TIMESTAMP NOT NULL,
    balance_change NUMERIC(38, 2),
    new_balance NUMERIC(38, 2),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Criar tabelas de Notification
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL REFERENCES payments(id),
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body TEXT,
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Grants para bank_user
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO bank_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO bank_user;
