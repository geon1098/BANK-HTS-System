package com.bank.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_key")
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String keyValue;                  // 클라이언트가 보낸 고유 키

    @Column(length = 36)
    private String transferGroupId;           // 이 키로 처리된 송금의 그룹 ID

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected IdempotencyKey() {}

    private IdempotencyKey(String keyValue, String transferGroupId) {
        this.keyValue = keyValue;
        this.transferGroupId = transferGroupId;
        this.createdAt = LocalDateTime.now();
    }

    public static IdempotencyKey of(String keyValue, String transferGroupId) {
        return new IdempotencyKey(keyValue, transferGroupId);
    }

    public Long getId() { return id; }
    public String getKeyValue() { return keyValue; }
    public String getTransferGroupId() { return transferGroupId; }
}