package org.example.wallet.service;

import org.example.user.entity.User;
import org.example.wallet.entity.Wallet;
import org.example.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.user.repository.UserRepository;
import org.example.user.entity.User;

import java.util.Locale;
import java.util.NoSuchElementException;

import org.example.transaction.entity.TransactionStatus;
import org.example.transaction.entity.TransactionType;
import org.example.transaction.entity.WalletTransaction;
import org.example.transaction.repository.WalletTransactionRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.example.wallet.dto.TransactionHistoryResponse;

import org.example.transaction.entity.IdempotencyRecord;
import org.example.transaction.repository.IdempotencyRecordRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository transactionRepository;
    private final IdempotencyRecordRepository idempotencyRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository, WalletTransactionRepository transactionRepository, IdempotencyRecordRepository idempotencyRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
    }

    @Transactional
    public Wallet createWallet(User user) {
        if (walletRepository.existsByUserId(user.getId())) {
            throw new IllegalStateException(
                    "Wallet already exists for this user"
            );
        }

        Wallet wallet = new Wallet(user);
        return walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Wallet getMyWallet(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found"));

        return walletRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new NoSuchElementException("Wallet not found"));
    }

    @Transactional
    public WalletTransaction deposit(String email, BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Wallet wallet = walletRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        WalletTransaction transaction = new WalletTransaction(
                wallet, amount, TransactionType.DEPOSIT);

        transactionRepository.save(transaction);

        wallet.credit(amount);
        transaction.markSuccessful();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public WalletTransaction withdraw(String email, BigDecimal amount) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException(
                    "Invalid withdrawal amount");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Wallet wallet = walletRepository.findByUserIdForUpdate(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        WalletTransaction transaction = new WalletTransaction(
                wallet, amount, TransactionType.WITHDRAWAL);

        transactionRepository.save(transaction);

        wallet.debit(amount);
        transaction.markSuccessful();

        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getMyTransactions(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Wallet not found"));

        return transactionRepository
                .findByWalletIdOrderByCreatedAtDesc(wallet.getId())
                .stream()
                .map(TransactionHistoryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public WalletTransaction transfer(
            String senderEmail,
            String recipientEmail,
            BigDecimal amount,
            String idempotencyKey) {

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.scale() > 2
                || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.trim().length() > 100) {
            throw new IllegalArgumentException("Invalid Idempotency-Key");
        }

        String senderNormalized = senderEmail.trim().toLowerCase(Locale.ROOT);
        String recipientNormalized = recipientEmail.trim().toLowerCase(Locale.ROOT);
        String key = idempotencyKey.trim();

        if (senderNormalized.equals(recipientNormalized)) {
            throw new IllegalArgumentException(
                    "Cannot transfer to your own account");
        }

        User sender = userRepository.findByEmail(senderNormalized)
                .orElseThrow(() ->
                        new IllegalArgumentException("Sender not found"));

        User recipient = userRepository.findByEmail(recipientNormalized)
                .orElseThrow(() ->
                        new IllegalArgumentException("Recipient not found"));

        String requestHash = createRequestHash(recipientNormalized, amount);

        // Atomically claim the key. A concurrent duplicate waits on MySQL's
        // unique constraint and then reuses the existing record.
        idempotencyRepository.insertIfAbsent(
                senderNormalized, key, requestHash);

        IdempotencyRecord record = idempotencyRepository
                .findForUpdate(senderNormalized, key)
                .orElseThrow(() ->
                        new IllegalStateException("Idempotency record not found"));

        if (!record.getRequestHash().equals(requestHash)) {
            throw new IllegalArgumentException(
                    "Idempotency key reused with a different request");
        }

        // Return the original transaction for a successful retry.
        if (record.getResponseReferenceId() != null) {
            return transactionRepository
                    .findByReferenceId(record.getResponseReferenceId())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Original transfer transaction not found"));
        }

        Long senderWalletId = walletRepository.findByUserId(sender.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Sender wallet not found"))
                .getId();

        Long recipientWalletId = walletRepository.findByUserId(recipient.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Recipient wallet not found"))
                .getId();

        Wallet senderWallet;
        Wallet recipientWallet;

        // Lock wallets in ascending ID order to reduce deadlock risk.
        if (senderWalletId < recipientWalletId) {
            senderWallet = walletRepository.findByUserIdForUpdate(sender.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Sender wallet not found"));

            recipientWallet = walletRepository.findByUserIdForUpdate(recipient.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Recipient wallet not found"));
        } else {
            recipientWallet = walletRepository.findByUserIdForUpdate(recipient.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Recipient wallet not found"));

            senderWallet = walletRepository.findByUserIdForUpdate(sender.getId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Sender wallet not found"));
        }

        // Any exception rolls back both balance changes and the idempotency record.
        senderWallet.debit(amount);
        recipientWallet.credit(amount);

        String transferId = UUID.randomUUID().toString();

        WalletTransaction sent = new WalletTransaction(
                senderWallet,
                amount,
                TransactionType.TRANSFER_SENT,
                UUID.randomUUID().toString(),
                transferId
        );

        WalletTransaction received = new WalletTransaction(
                recipientWallet,
                amount,
                TransactionType.TRANSFER_RECEIVED,
                UUID.randomUUID().toString(),
                transferId
        );

        sent.markSuccessful();
        received.markSuccessful();

        transactionRepository.save(sent);
        transactionRepository.save(received);

        // Store the original sender-side transaction for future retries.
        record.setResponseReferenceId(sent.getReferenceId());
        idempotencyRepository.save(record);

        return sent;
    }

    private String createRequestHash(String recipientEmail, BigDecimal amount) {
        String canonicalRequest =
                recipientEmail.trim().toLowerCase(Locale.ROOT)
                        + "|"
                        + amount.stripTrailingZeros().toPlainString();

        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}