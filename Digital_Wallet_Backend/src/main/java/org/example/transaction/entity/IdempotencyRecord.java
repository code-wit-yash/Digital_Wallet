package org.example.transaction.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotency_sender_key",
                columnNames = {"sender_email", "idempotency_key"}
        )
)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_email", nullable = false, length = 255)
    private String senderEmail;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_reference_id", length = 36)
    private String responseReferenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected IdempotencyRecord() {}

    public IdempotencyRecord(
            String senderEmail,
            String idempotencyKey,
            String requestHash) {
        this.senderEmail = senderEmail;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResponseReferenceId() {
        return responseReferenceId;
    }

    public void setResponseReferenceId(String responseReferenceId) {
        this.responseReferenceId = responseReferenceId;
    }
}