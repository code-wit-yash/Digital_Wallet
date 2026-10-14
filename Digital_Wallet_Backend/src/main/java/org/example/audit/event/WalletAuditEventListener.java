package org.example.audit.event;

import org.example.audit.service.AuditLogService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WalletAuditEventListener {

    private final AuditLogService auditLogService;

    public WalletAuditEventListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WalletAuditEvent event) {
        auditLogService.log(
                event.userId(),
                event.action(),
                event.status(),
                event.details()
        );
    }
}