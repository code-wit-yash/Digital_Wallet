package org.example.transaction.service;

import org.example.transaction.entity.EntryType;
import org.example.transaction.entity.LedgerEntry;
import org.example.transaction.entity.WalletTransaction;
import org.example.transaction.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public void recordEntry(
            WalletTransaction transaction,
            String transferId,
            EntryType entryType,
            BigDecimal amount,
            String account) {

        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction cannot be null");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Ledger amount must be greater than zero");
        }

        if (account == null || account.isBlank()) {
            throw new IllegalArgumentException(
                    "Account cannot be blank");
        }

        LedgerEntry entry = new LedgerEntry(
                transaction,
                transferId,
                entryType,
                amount,
                account
        );

        ledgerEntryRepository.save(entry);
    }

    @Transactional
    public void recordTransfer(
            WalletTransaction sentTransaction,
            WalletTransaction receivedTransaction,
            String transferId,
            BigDecimal amount,
            Long senderWalletId,
            Long recipientWalletId) {

        if (sentTransaction == null || receivedTransaction == null) {
            throw new IllegalArgumentException(
                    "Both transfer transactions are required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Ledger amount must be greater than zero");
        }

        if (senderWalletId == null || recipientWalletId == null
                || senderWalletId.equals(recipientWalletId)) {
            throw new IllegalArgumentException(
                    "Valid, different wallet IDs are required");
        }

        LedgerEntry debit = new LedgerEntry(
                sentTransaction,
                transferId,
                EntryType.DEBIT,
                amount,
                "WALLET:" + senderWalletId
        );

        LedgerEntry credit = new LedgerEntry(
                receivedTransaction,
                transferId,
                EntryType.CREDIT,
                amount,
                "WALLET:" + recipientWalletId
        );

        ledgerEntryRepository.saveAll(java.util.List.of(debit, credit));
    }

    @Transactional
    public void recordWalletOperation(
            WalletTransaction transaction,
            BigDecimal amount,
            Long walletId,
            boolean isDeposit) {

        if (transaction == null) {
            throw new IllegalArgumentException(
                    "Transaction cannot be null");
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Ledger amount must be greater than zero");
        }

        if (walletId == null) {
            throw new IllegalArgumentException(
                    "Wallet ID cannot be null");
        }

        EntryType walletEntryType = isDeposit
                ? EntryType.CREDIT
                : EntryType.DEBIT;

        EntryType clearingEntryType = isDeposit
                ? EntryType.DEBIT
                : EntryType.CREDIT;

        LedgerEntry walletEntry = new LedgerEntry(
                transaction,
                null,
                walletEntryType,
                amount,
                "WALLET:" + walletId
        );

        LedgerEntry clearingEntry = new LedgerEntry(
                transaction,
                null,
                clearingEntryType,
                amount,
                "EXTERNAL_CLEARING"
        );

        ledgerEntryRepository.saveAll(
                java.util.List.of(walletEntry, clearingEntry));
    }
}