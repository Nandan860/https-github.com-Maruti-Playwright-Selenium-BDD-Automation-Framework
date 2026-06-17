-- =============================================================================
-- Source (Legacy) Database Seed Script
-- Runs automatically when postgres-source container starts
-- =============================================================================

CREATE TABLE IF NOT EXISTS accounts (
    account_id      VARCHAR(20)     PRIMARY KEY,
    account_type    VARCHAR(20)     NOT NULL,
    status          VARCHAR(10)     NOT NULL DEFAULT 'ACTIVE',
    balance         DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    currency        CHAR(3)         NOT NULL DEFAULT 'GBP',
    owner_name      VARCHAR(100),
    created_date    TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id  VARCHAR(30)     PRIMARY KEY,
    account_id      VARCHAR(20)     NOT NULL REFERENCES accounts(account_id),
    amount          DECIMAL(15,2)   NOT NULL,
    currency        CHAR(3)         NOT NULL DEFAULT 'GBP',
    status          VARCHAR(20)     NOT NULL,
    transaction_date TIMESTAMP      NOT NULL DEFAULT NOW(),
    payee           VARCHAR(100),
    reference       VARCHAR(50)
);

-- Seed accounts
INSERT INTO accounts VALUES
('ACC001','CURRENT','ACTIVE',  15234.50,'GBP','Alice Johnson', NOW()),
('ACC002','SAVINGS','ACTIVE',  87000.00,'GBP','Bob Smith',     NOW()),
('ACC003','CREDIT', 'ACTIVE', -1200.00,'GBP','Carol White',   NOW()),
('ACC004','CURRENT','INACTIVE',0.00,    'GBP','Dave Brown',    NOW()),
('ACC005','SAVINGS','ACTIVE',  250000.00,'GBP','Eve Davis',    NOW())
ON CONFLICT DO NOTHING;

-- Seed transactions
INSERT INTO transactions VALUES
('TXN001','ACC001',500.00,'GBP','COMPLETED',NOW(),'ACME Corp','REF001'),
('TXN002','ACC001',250.00,'GBP','COMPLETED',NOW(),'Test Vendor','REF002'),
('TXN003','ACC002',1000.00,'GBP','PENDING',  NOW(),'Big Corp','REF003'),
('TXN004','ACC003',75.00, 'GBP','FAILED',    NOW(),'Small Ltd','REF004'),
('TXN005','ACC005',5000.00,'GBP','COMPLETED',NOW(),'Mega Corp','REF005')
ON CONFLICT DO NOTHING;
