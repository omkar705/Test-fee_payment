-- ============================================================================
-- FEE PAYMENT MANAGEMENT PLATFORM - POSTGRESQL DDL SCRIPT
-- ============================================================================

-- Clean up existing tables in reverse dependency order
DROP TABLE IF EXISTS "PaymentGatewayLogs" CASCADE;
DROP TABLE IF EXISTS "Receipts" CASCADE;
DROP TABLE IF EXISTS "Transactions" CASCADE;
DROP TABLE IF EXISTS "FeePayments" CASCADE;
DROP TABLE IF EXISTS "FeeInstallment" CASCADE;
DROP TABLE IF EXISTS "FeeAssignment" CASCADE;
DROP TABLE IF EXISTS "FeeStructure" CASCADE;
DROP TABLE IF EXISTS "BackupHistory" CASCADE;
DROP TABLE IF EXISTS "SecurityLogs" CASCADE;
DROP TABLE IF EXISTS "AuditLogs" CASCADE;
DROP TABLE IF EXISTS "Reports" CASCADE;
DROP TABLE IF EXISTS "Users" CASCADE;
DROP TABLE IF EXISTS "Students" CASCADE;
DROP TABLE IF EXISTS "Roles" CASCADE;

-- ----------------------------------------------------------------------------
-- 1. Roles Table
-- ----------------------------------------------------------------------------
CREATE TABLE "Roles" (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- ----------------------------------------------------------------------------
-- 2. Students Table
-- ----------------------------------------------------------------------------
CREATE TABLE "Students" (
    student_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    course VARCHAR(100) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_number VARCHAR(15) NOT NULL
);

-- ----------------------------------------------------------------------------
-- 3. Users Table
-- ----------------------------------------------------------------------------
CREATE TABLE "Users" (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    student_id INT UNIQUE,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) 
        REFERENCES "Roles"(role_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_users_student FOREIGN KEY (student_id) 
        REFERENCES "Students"(student_id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 4. FeeStructure Table
-- ----------------------------------------------------------------------------
CREATE TABLE "FeeStructure" (
    fee_id SERIAL PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    fee_type VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    due_date DATE NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------------
-- 5. FeeAssignment Table
-- ----------------------------------------------------------------------------
CREATE TABLE "FeeAssignment" (
    assignment_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL,
    fee_structure_id INT NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL CHECK (total_amount >= 0),
    paid_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (paid_amount >= 0),
    outstanding_amount NUMERIC(10, 2) NOT NULL CHECK (outstanding_amount >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_assignment_student FOREIGN KEY (student_id) 
        REFERENCES "Students"(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_assignment_structure FOREIGN KEY (fee_structure_id) 
        REFERENCES "FeeStructure"(fee_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 6. FeeInstallment Table
-- ----------------------------------------------------------------------------
CREATE TABLE "FeeInstallment" (
    installment_id SERIAL PRIMARY KEY,
    assignment_id INT NOT NULL,
    installment_no INT NOT NULL CHECK (installment_no > 0),
    due_date DATE NOT NULL,
    installment_amount NUMERIC(10, 2) NOT NULL CHECK (installment_amount > 0),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_installment_assignment FOREIGN KEY (assignment_id) 
        REFERENCES "FeeAssignment"(assignment_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 7. FeePayments Table
-- ----------------------------------------------------------------------------
CREATE TABLE "FeePayments" (
    payment_id SERIAL PRIMARY KEY,
    installment_id INT,
    student_id INT NOT NULL,
    amount_paid NUMERIC(10, 2) NOT NULL CHECK (amount_paid > 0),
    payment_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
    CONSTRAINT fk_payments_installment FOREIGN KEY (installment_id) 
        REFERENCES "FeeInstallment"(installment_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_payments_student FOREIGN KEY (student_id) 
        REFERENCES "Students"(student_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 8. Transactions Table
-- ----------------------------------------------------------------------------
CREATE TABLE "Transactions" (
    transaction_id SERIAL PRIMARY KEY,
    payment_id INT NOT NULL,
    transaction_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    gateway_reference VARCHAR(100) NOT NULL UNIQUE,
    CONSTRAINT fk_transactions_payment FOREIGN KEY (payment_id) 
        REFERENCES "FeePayments"(payment_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 9. Receipts Table
-- ----------------------------------------------------------------------------
CREATE TABLE "Receipts" (
    receipt_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL UNIQUE,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    generated_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_receipts_transaction FOREIGN KEY (transaction_id) 
        REFERENCES "Transactions"(transaction_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 10. PaymentGatewayLogs Table
-- ----------------------------------------------------------------------------
CREATE TABLE "PaymentGatewayLogs" (
    log_id SERIAL PRIMARY KEY,
    transaction_id INT NOT NULL,
    request_payload TEXT NOT NULL,
    response_payload TEXT NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gateway_logs_transaction FOREIGN KEY (transaction_id) 
        REFERENCES "Transactions"(transaction_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 11. Reports Table
-- ----------------------------------------------------------------------------
CREATE TABLE "Reports" (
    report_id SERIAL PRIMARY KEY,
    generated_by INT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    date_range VARCHAR(100) NOT NULL,
    generated_on TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reports_user FOREIGN KEY (generated_by) 
        REFERENCES "Users"(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 12. AuditLogs Table
-- ----------------------------------------------------------------------------
CREATE TABLE "AuditLogs" (
    audit_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    action VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) 
        REFERENCES "Users"(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 13. SecurityLogs Table
-- ----------------------------------------------------------------------------
CREATE TABLE "SecurityLogs" (
    log_id SERIAL PRIMARY KEY,
    user_id INT,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_security_logs_user FOREIGN KEY (user_id) 
        REFERENCES "Users"(user_id) ON DELETE SET NULL ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 14. BackupHistory Table
-- ----------------------------------------------------------------------------
CREATE TABLE "BackupHistory" (
    backup_id SERIAL PRIMARY KEY,
    performed_by INT NOT NULL,
    backup_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_backup_user FOREIGN KEY (performed_by) 
        REFERENCES "Users"(user_id) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- ============================================================================
-- PERFORMANCE & FOREIGN KEY INDICES
-- ============================================================================

-- Users Indices
CREATE INDEX idx_users_role_id ON "Users"(role_id);
CREATE INDEX idx_users_student_id ON "Users"(student_id);

-- FeeAssignment Indices
CREATE INDEX idx_fee_assignment_student_id ON "FeeAssignment"(student_id);
CREATE INDEX idx_fee_assignment_structure_id ON "FeeAssignment"(fee_structure_id);
CREATE INDEX idx_fee_assignment_status ON "FeeAssignment"(status);

-- FeeInstallment Indices
CREATE INDEX idx_fee_installment_assignment_id ON "FeeInstallment"(assignment_id);

-- FeePayments Indices
CREATE INDEX idx_fee_payments_student_id ON "FeePayments"(student_id);
CREATE INDEX idx_fee_payments_installment_id ON "FeePayments"(installment_id);
CREATE INDEX idx_fee_payments_status ON "FeePayments"(status);

-- Transactions Indices
CREATE INDEX idx_transactions_payment_id ON "Transactions"(payment_id);
CREATE INDEX idx_transactions_gateway_ref ON "Transactions"(gateway_reference);

-- PaymentGatewayLogs Indices
CREATE INDEX idx_gateway_logs_transaction_id ON "PaymentGatewayLogs"(transaction_id);

-- Reports & Logs Indices
CREATE INDEX idx_reports_generated_by ON "Reports"(generated_by);
CREATE INDEX idx_audit_logs_user_id ON "AuditLogs"(user_id);
CREATE INDEX idx_security_logs_user_id ON "SecurityLogs"(user_id);
CREATE INDEX idx_backup_history_performed_by ON "BackupHistory"(performed_by);
