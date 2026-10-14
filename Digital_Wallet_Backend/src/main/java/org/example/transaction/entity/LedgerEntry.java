package org.example.transaction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_transaction", columnList = "transaction_id"),
                @Index(name = "idx_ledger_transfer", columnList = "transfer_id")
        }
)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private WalletTransaction transaction;

    @Column(name = "transfer_id", length = 36)
    private String transferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String account;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(
            WalletTransaction transaction,
            String transferId,
            EntryType entryType,
            BigDecimal amount,
            String account) {
        this.transaction = transaction;
        this.transferId = transferId;
        this.entryType = entryType;
        this.amount = amount;
        this.account = account;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public WalletTransaction getTransaction() {
        return transaction;
    }

    public String getTransferId() {
        return transferId;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getAccount() {
        return account;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
