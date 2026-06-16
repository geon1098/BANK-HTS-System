package com.bank.service;

import com.bank.dto.TransactionResponse;
import com.bank.entity.Account;
import com.bank.entity.TransactionType;                          // ← 추가
import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;        // ← 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;                                  // ← 추가
import static com.bank.repository.TransactionSpecs.*;   

@Service
public class TransactionQueryService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final AccountRepository accountRepository;

    public TransactionQueryService(TransactionHistoryRepository transactionHistoryRepository,
                                   AccountRepository accountRepository) {
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(
            Long accountId, String username,
            TransactionType type, LocalDateTime from,            // ← 필터 파라미터 추가
            Pageable pageable) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!account.isOwnedBy(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        var spec = Specification.where(ofAccount(accountId))
                .and(typeEq(type))
                .and(createdAfter(from));
        return transactionHistoryRepository.findAll(spec, pageable)
                .map(TransactionResponse::from);
    }
}