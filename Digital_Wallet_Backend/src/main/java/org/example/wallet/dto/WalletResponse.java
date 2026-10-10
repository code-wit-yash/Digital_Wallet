package org.example.wallet.dto;

import java.math.BigDecimal;

public class WalletResponse {

    private Long walletId;
    private BigDecimal balance;
    private String currency;

    public WalletResponse(Long walletId, BigDecimal balance, String currency) {
        this.walletId = walletId;
        this.balance = balance;
        this.currency = currency;
    }

    public Long getWalletId() {
        return walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
