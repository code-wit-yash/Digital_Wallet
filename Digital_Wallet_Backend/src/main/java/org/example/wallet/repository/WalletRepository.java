package org.example.wallet.repository;

import jakarta.persistence.LockModeType;
import org.example.wallet.dto.WalletBalanceResponse;
import org.example.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletRepository
        extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId")
    Optional<Wallet> findByUserIdForUpdate(
            @Param("userId") Long userId);

    @Query("""
    SELECT new org.example.wallet.dto.WalletBalanceResponse(
        w.id, w.balance, w.currency
    )
    FROM Wallet w
    WHERE w.user.id = :userId
    """)
    Optional<WalletBalanceResponse> findBalanceByUserId(
            @Param("userId") Long userId
    );
}
