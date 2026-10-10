package org.example.transaction.repository;


import org.example.transaction.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO idempotency_records
        (sender_email, idempotency_key, request_hash, created_at)
        VALUES (:senderEmail, :idempotencyKey, :requestHash, CURRENT_TIMESTAMP)
        """, nativeQuery = true)
    int insertIfAbsent(
            @Param("senderEmail") String senderEmail,
            @Param("idempotencyKey") String idempotencyKey,
            @Param("requestHash") String requestHash
    );

    @Query(value = """
        SELECT * FROM idempotency_records
        WHERE sender_email = :senderEmail
          AND idempotency_key = :idempotencyKey
        FOR UPDATE
        """, nativeQuery = true)
    Optional<IdempotencyRecord> findForUpdate(
            @Param("senderEmail") String senderEmail,
            @Param("idempotencyKey") String idempotencyKey
    );
}