package com.bank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_history")
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;                              // 어느 계좌의 거래인지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;                            // 거래 금액

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;                      // 거래 직후 잔액(스냅샷)

    @Column(length = 20)
    private String counterpartyAccountNumber;            // 상대 계좌(송금일 때 "어디로")

    @Column(length = 36)
    private String transferGroupId;                      // 송금 보냄/받음/취소 묶음 ID

    @Column(nullable = false, length = 50)
    private String performedBy;                          // 누가 (로그인 아이디)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;                     // 언제

    protected TransactionHistory() {}

    private TransactionHistory(Account account, TransactionType type, BigDecimal amount,
                               BigDecimal balanceAfter, String counterpartyAccountNumber,
                               String transferGroupId, String performedBy) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
        this.transferGroupId = transferGroupId;
        this.performedBy = performedBy;
        this.createdAt = LocalDateTime.now();
    }

    /** 입출금용 (상대 계좌·그룹 없음) */
    public static TransactionHistory of(Account account, TransactionType type,
                                        BigDecimal amount, String performedBy) {
        return new TransactionHistory(account, type, amount, account.getBalance(),
                null, null, performedBy);
    }

    /** 송금/취소용 (상대 계좌·그룹 ID 포함) */
    public static TransactionHistory ofTransfer(Account account, TransactionType type,
                                                BigDecimal amount, String counterpartyAccountNumber,
                                                String transferGroupId, String performedBy) {
        return new TransactionHistory(account, type, amount, account.getBalance(),
                counterpartyAccountNumber, transferGroupId, performedBy);
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getCounterpartyAccountNumber() { return counterpartyAccountNumber; }
    public String getTransferGroupId() { return transferGroupId; }
    public String getPerformedBy() { return performedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}