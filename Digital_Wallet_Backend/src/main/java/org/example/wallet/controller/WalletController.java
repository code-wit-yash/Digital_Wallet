package org.example.wallet.controller;

import org.example.wallet.dto.WalletResponse;
import org.example.wallet.entity.Wallet;
import org.example.wallet.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.example.wallet.dto.DepositRequest;
import org.example.wallet.dto.DepositResponse;
import org.example.transaction.entity.WalletTransaction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.example.wallet.dto.TransactionHistoryResponse;
import java.util.List;

import org.example.wallet.dto.TransferRequest;
import org.example.wallet.dto.TransferResponse;

import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet(
            Authentication authentication) {

        Wallet wallet = walletService.getMyWallet(
                authentication.getName());

        WalletResponse response = new WalletResponse(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCurrency()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        WalletTransaction transaction = walletService.deposit(
                authentication.getName(),
                request.getAmount());

        return ResponseEntity.ok(
                new DepositResponse(transaction));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<DepositResponse> withdraw(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        WalletTransaction transaction = walletService.withdraw(
                authentication.getName(),
                request.getAmount());

        return ResponseEntity.ok(
                new DepositResponse(transaction));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionHistoryResponse>> getMyTransactions(
            Authentication authentication) {

        return ResponseEntity.ok(
                walletService.getMyTransactions(authentication.getName()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        WalletTransaction transaction = walletService.transfer(
                authentication.getName(),
                request.getRecipientEmail(),
                request.getAmount(),
                idempotencyKey
        );

        TransferResponse response = new TransferResponse(
                transaction.getReferenceId(),
                transaction.getStatus().name(),
                transaction.getAmount()
        );

        return ResponseEntity.ok(response);
    }

}
