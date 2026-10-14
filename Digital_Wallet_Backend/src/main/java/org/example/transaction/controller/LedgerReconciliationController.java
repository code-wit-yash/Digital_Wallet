package org.example.transaction.controller;

import org.example.transaction.service.LedgerReconciliationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@RestController
@RequestMapping("/api/reconciliation")
public class LedgerReconciliationController {

    private final LedgerReconciliationService reconciliationService;

    public LedgerReconciliationController(
            LedgerReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/missing-ledger-entries")
    public Map<String, Object> findMissingLedgerEntries() {
        List<Long> missingIds =
                reconciliationService.findTransactionsMissingLedgerEntries();

        return Map.of(
                "count", missingIds.size(),
                "transactionIds", missingIds
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/unbalanced-transfers")
    public Map<String, Object> findUnbalancedTransfers() {
        List<String> unbalancedTransfers =
                reconciliationService.findUnbalancedTransfers();

        return Map.of(
                "count", unbalancedTransfers.size(),
                "transferIds", unbalancedTransfers
        );
    }
}