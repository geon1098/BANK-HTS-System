package com.bank.entity;

import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)                    // 계좌(N) → 회원(1), 지연 로딩
    @JoinColumn(name = "member_id", nullable = false)
    private Member owner;                                 // 소유자

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;                         // 계좌번호(중복 불가)

    @Column(nullable = false, precision = 19, scale = 2)  // DECIMAL(19,2)
    private BigDecimal balance;                           // 잔액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Account() {}

    private Account(Member owner, String accountNumber) {
        this.owner = owner;
        this.accountNumber = accountNumber;
        this.balance = BigDecimal.ZERO;                  // 0원으로 시작
        this.status = AccountStatus.ACTIVE;              // 처음엔 정상 상태
        this.createdAt = LocalDateTime.now();
    }

    public static Account open(Member owner, String accountNumber) {
        return new Account(owner, accountNumber);
    }

    // ===== 도메인 행위: 잔액 규칙을 스스로 책임진다 =====

    /** 입금: 활성 상태 + 양수 검증 후 잔액 증가 */
    public void deposit(BigDecimal amount) {
        validateActive();
        validatePositive(amount);
        this.balance = this.balance.add(amount);         // 반환값을 반드시 다시 대입!
    }

    /** 출금: 활성 + 양수 + 잔액충분 검증 후 잔액 감소 */
    public void withdraw(BigDecimal amount) {
        validateActive();
        validatePositive(amount);
        if (this.balance.compareTo(amount) < 0) {        // 잔액 < 금액 → 부족
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        this.balance = this.balance.subtract(amount);    // 음수가 될 수 없음(위에서 막음)
    }

    public boolean isOwnedBy(String username) {
        return this.owner.getUsername().equals(username);
    }

    // ===== 내부 검증 =====

    private void validateActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }
    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }
    }

    public Long getId() { return id; }
    public Member getOwner() { return owner; }
    public String getAccountNumber() { return accountNumber; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}