package org.example.wallet.dto;
import java.math.BigDecimal;

public class TransferResponse {

    private final String referenceId;
    private final String status;
    private final BigDecimal amount;

    public TransferResponse(
            String referenceId,
            String status,
            BigDecimal amount) {
        this.referenceId = referenceId;
        this.status = status;
        this.amount = amount;
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