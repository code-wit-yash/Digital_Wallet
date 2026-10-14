package org.example.transaction.repository;

import org.example.transaction.entity.LedgerEntry;
import org.example.transaction.entity.TransactionStatus;
import org.example.transaction.entity.TransactionType;
import org.example.transaction.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransferId(String transferId);

    @Query("""
        SELECT wt.id
        FROM WalletTransaction wt
        WHERE wt.status = :status
          AND wt.type IN :types
          AND NOT EXISTS (
              SELECT le.id
              FROM LedgerEntry le
              WHERE le.transaction.id = wt.id
          )
        """)
    List<Long> findTransactionIdsWithoutLedgerEntries(
            @Param("status") TransactionStatus status,
            @Param("types") List<TransactionType> types
    );

    @Query(value = """
    SELECT transfer_id
    FROM ledger_entries
    WHERE transfer_id IS NOT NULL
    GROUP BY transfer_id
    HAVING SUM(CASE WHEN entry_type = 'DEBIT' THEN amount ELSE 0 END)
        <> SUM(CASE WHEN entry_type = 'CREDIT' THEN amount ELSE 0 END)
    """, nativeQuery = true)
    List<String> findUnbalancedTransfers();
}