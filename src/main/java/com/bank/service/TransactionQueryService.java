package com.bank.service;

import com.bank.dto.TransactionResponse;
import com.bank.entity.Account;
import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import com.bank.repository.AccountRepository;
import com.bank.repository.TransactionHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<TransactionResponse> getTransactions(Long accountId, String username, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
        if (!account.isOwnedBy(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);   // 남의 내역 금지
        }
        return transactionHistoryRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TransactionResponse::from);   // 엔티티 Page → DTO Page
    }
}