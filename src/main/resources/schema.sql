-- =========================================================================
-- Native PostgreSQL Database Schema DDL (Pure SQL - No ORM / Hibernate)
-- Target Database: Supabase PostgreSQL
-- =========================================================================

-- Table 1: transactions
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_id BIGINT NOT NULL,
    transaction_reference VARCHAR(255) NOT NULL UNIQUE,
    gateway_name VARCHAR(100) NOT NULL,
    transaction_status VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    amount NUMERIC(12, 2) NOT NULL,
    verified_by VARCHAR(100),
    version BIGINT DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table 2: payment_gateway_logs
CREATE TABLE IF NOT EXISTS payment_gateway_logs (
    gateway_log_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    gateway_name VARCHAR(100) NOT NULL,
    request_data TEXT,
    response_data TEXT,
    status VARCHAR(50) NOT NULL,
    log_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table 3: receipts
CREATE TABLE IF NOT EXISTS receipts (
    receipt_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE REFERENCES transactions(transaction_id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL,
    generated_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    receipt_url VARCHAR(500)
);

-- Table 4: payment_settlements
CREATE TABLE IF NOT EXISTS payment_settlements (
    settlement_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    merchant_account VARCHAR(100) NOT NULL,
    settled_amount NUMERIC(12, 2) NOT NULL,
    commission_fee NUMERIC(10, 2) DEFAULT 0.00,
    settlement_status VARCHAR(50) DEFAULT 'PENDING' CHECK (settlement_status IN ('PENDING', 'PROCESSED', 'FAILED', 'ON_HOLD')),
    settlement_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table 5: users
CREATE TABLE IF NOT EXISTS users (
    user_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_transactions_reference ON transactions (transaction_reference);
CREATE INDEX IF NOT EXISTS idx_gateway_logs_txn ON payment_gateway_logs (transaction_id);
CREATE INDEX IF NOT EXISTS idx_receipts_student ON receipts (student_id);
CREATE INDEX IF NOT EXISTS idx_receipts_txn ON receipts (transaction_id);
CREATE INDEX IF NOT EXISTS idx_settlements_transaction ON payment_settlements (transaction_id);
CREATE INDEX IF NOT EXISTS idx_settlements_status ON payment_settlements (settlement_status);
