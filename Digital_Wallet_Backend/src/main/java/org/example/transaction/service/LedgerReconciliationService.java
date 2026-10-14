package org.example.transaction.service;

import org.example.transaction.entity.TransactionStatus;
import org.example.transaction.entity.TransactionType;
import org.example.transaction.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LedgerReconciliationService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerReconciliationService(
            LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<Long> findTransactionsMissingLedgerEntries() {
        return ledgerEntryRepository
                .findTransactionIdsWithoutLedgerEntries(
                        TransactionStatus.SUCCESS,
                        List.of(
                                TransactionType.DEPOSIT,
                                TransactionType.WITHDRAWAL,
                                TransactionType.TRANSFER_SENT,
                                TransactionType.TRANSFER_RECEIVED
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<String> findUnbalancedTransfers() {
        return ledgerEntryRepository.findUnbalancedTransfers();
    }
}