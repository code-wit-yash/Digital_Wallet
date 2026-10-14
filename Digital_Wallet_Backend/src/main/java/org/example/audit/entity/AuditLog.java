package org.example.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_user", columnList = "user_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_created", columnList = "created_at")
        }
)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected AuditLog() {}

    public AuditLog(
            Long userId,
            String action,
            String status,
            String details) {
        this.userId = userId;
        this.action = action;
        this.status = status;
        this.details = details;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
