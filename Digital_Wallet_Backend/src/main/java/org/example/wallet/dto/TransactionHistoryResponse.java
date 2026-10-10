package org.example.wallet.dto;

import org.example.transaction.entity.WalletTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionHistoryResponse {

    private final Long transactionId;
    private final String referenceId;
    private final BigDecimal amount;
    private final String type;
    private final String status;
    private final LocalDateTime createdAt;

    public TransactionHistoryResponse(WalletTransaction transaction) {
        this.transactionId = transaction.getId();
        this.referenceId = transaction.getReferenceId();
        this.amount = transaction.getAmount();
        this.type = transaction.getType().name();
        this.status = transaction.getStatus().name();
        this.createdAt = transaction.getCreatedAt();
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
