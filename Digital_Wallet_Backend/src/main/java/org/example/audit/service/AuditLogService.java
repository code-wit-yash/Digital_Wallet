package org.example.audit.service;

import org.example.audit.entity.AuditLog;
import org.example.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(
            Long userId,
            String action,
            String status,
            String details) {

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action cannot be blank");
        }

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit status cannot be blank");
        }

        AuditLog auditLog = new AuditLog(
                userId,
                action.trim(),
                status.trim(),
                sanitize(details)
        );

        auditLogRepository.save(auditLog);
    }

    private String sanitize(String details) {
        if (details == null) {
            return null;
        }

        String sanitized = details
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();

        return sanitized.length() > 500
                ? sanitized.substring(0, 500)
                : sanitized;
    }
}
