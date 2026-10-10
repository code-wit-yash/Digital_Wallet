package org.example.transaction.entity;

import jakarta.persistence.*;
import org.example.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "wallet_transactions",
        indexes = {
                @Index(
                        name = "idx_transaction_wallet",
                        columnList = "wallet_id"
                ),
                @Index(
                        name = "idx_transaction_reference",
                        columnList = "reference_id"
                )
        }
)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, unique = true, length = 36)
    private String referenceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "transfer_id", length = 36)
    private String transferId;

    protected WalletTransaction() {
        // Required by JPA
    }

    public WalletTransaction(
            Wallet wallet,
            BigDecimal amount,
            TransactionType type
    ) {
        this(wallet, amount, type, UUID.randomUUID().toString(), null);
    }

    public WalletTransaction(
            Wallet wallet,
            BigDecimal amount,
            TransactionType type,
            String referenceId
    ) {
        this(wallet, amount, type, referenceId, null);
    }

    public WalletTransaction(
            Wallet wallet,
            BigDecimal amount,
            TransactionType type,
            String referenceId,
            String transferId
    ) {
        this.wallet = wallet;
        this.amount = amount;
        this.type = type;
        this.status = TransactionStatus.PENDING;
        this.referenceId = referenceId;
        this.transferId = transferId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void markSuccessful() {
        this.status = TransactionStatus.SUCCESS;
    }

    public String getTransferId() {
        return transferId;
    }
}