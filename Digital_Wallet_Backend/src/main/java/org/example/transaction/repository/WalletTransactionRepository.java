package org.example.transaction.repository;

import org.example.transaction.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(
            Long walletId);

    Optional<WalletTransaction> findByReferenceId(
            String referenceId);
}