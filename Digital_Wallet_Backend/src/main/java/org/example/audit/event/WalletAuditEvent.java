package org.example.audit.event;

public record WalletAuditEvent(
        Long userId,
        String action,
        String status,
        String details
) {}