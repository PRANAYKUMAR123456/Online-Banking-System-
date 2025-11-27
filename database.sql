-- Database schema for Online Banking System

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    holder_name    VARCHAR(100) NOT NULL,
    pin            CHAR(4)      NOT NULL,
    balance        DOUBLE       NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id   INT AUTO_INCREMENT PRIMARY KEY,
    account_number   VARCHAR(20) NOT NULL,
    type             VARCHAR(20) NOT NULL,
    amount           DOUBLE      NOT NULL,
    timestamp        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    description      VARCHAR(255),
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);
