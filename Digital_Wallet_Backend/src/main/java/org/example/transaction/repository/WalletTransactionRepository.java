package org.example.transaction.repository;

import org.example.transaction.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import org.example.transaction.entity.TransactionStatus;
import org.example.transaction.entity.TransactionType;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(
            Long walletId);

    Optional<WalletTransaction> findByReferenceId(
            String referenceId);

    List<WalletTransaction> findByStatusAndTypeIn(
            TransactionStatus status,
            List<TransactionType> types
    );
}