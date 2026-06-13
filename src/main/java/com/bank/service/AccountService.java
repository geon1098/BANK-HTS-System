package com.bank.service;

import com.bank.dto.AccountResponse;
import com.bank.entity.Account;
import com.bank.entity.Member;
import com.bank.entity.TransactionHistory;
import com.bank.entity.TransactionType;
import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import com.bank.repository.AccountRepository;
import com.bank.repository.MemberRepository;
import com.bank.repository.TransactionHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;

    public AccountService(AccountRepository accountRepository,
                          MemberRepository memberRepository,
                          TransactionHistoryRepository transactionHistoryRepository) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    /** 계좌 생성: 로그인한 사용자의 새 계좌를 만든다 */
    @Transactional
    public AccountResponse createAccount(String username) {
        Member owner = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Account account = Account.open(owner, generateAccountNumber());
        return AccountResponse.from(accountRepository.save(account));
    }

    /** 계좌 조회: 본인 계좌만 (소유권 검증) */
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId, String username) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateOwner(account, username);
        return AccountResponse.from(account);
    }

    /** 입금 */
    @Transactional
    public AccountResponse deposit(Long accountId, BigDecimal amount, String username) {
        Account account = accountRepository.findByIdForUpdate(accountId)   // 비관적 락
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateOwner(account, username);

        account.deposit(amount);                                          // 도메인이 검증+증가
        saveHistory(account, TransactionType.DEPOSIT, amount, username);
        return AccountResponse.from(account);
    }

    /** 출금 */
    @Transactional
    public AccountResponse withdraw(Long accountId, BigDecimal amount, String username) {
        Account account = accountRepository.findByIdForUpdate(accountId)   // 비관적 락
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        validateOwner(account, username);

        account.withdraw(amount);                                         // 잔액부족 시 예외→롤백
        saveHistory(account, TransactionType.WITHDRAW, amount, username);
        return AccountResponse.from(account);
    }

    // ===== 내부 헬퍼 (한 가지 책임씩) =====

    private void validateOwner(Account account, String username) {
        if (!account.isOwnedBy(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);   // 남의 계좌 금지
        }
    }

    private void saveHistory(Account account, TransactionType type,
                             BigDecimal amount, String username) {
        transactionHistoryRepository.save(
                TransactionHistory.of(account, type, amount, username));
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "110-" + (int) (Math.random() * 9000 + 1000)
                    + "-" + UUID.randomUUID().toString().substring(0, 6);
        } while (accountRepository.existsByAccountNumber(number));   // 중복 방지
        return number;
    }
}