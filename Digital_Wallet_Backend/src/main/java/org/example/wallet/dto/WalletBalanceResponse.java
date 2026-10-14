package org.example.wallet.dto;
import java.math.BigDecimal;

public record WalletBalanceResponse( Long walletId, BigDecimal balance, String currency) {}
