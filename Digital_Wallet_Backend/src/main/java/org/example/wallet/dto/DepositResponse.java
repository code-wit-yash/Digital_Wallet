package org.example.wallet.dto;

import org.example.transaction.entity.WalletTransaction;

import java.math.BigDecimal;

public class DepositResponse {

    private final Long transactionId;
    private final String referenceId;
    private final String status;
    private final BigDecimal amount;

    public DepositResponse(WalletTransaction transaction) {
        this.transactionId = transaction.getId();
        this.referenceId = transaction.getReferenceId();
        this.status = transaction.getStatus().name();
        this.amount = transaction.getAmount();
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}