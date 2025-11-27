package com.bank.model;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private String accountNumber;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private String description;

    public Transaction() {}

    public Transaction(int transactionId, String accountNumber, String type, double amount,
                       LocalDateTime timestamp, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.description = description;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
