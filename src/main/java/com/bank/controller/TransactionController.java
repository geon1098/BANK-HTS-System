package com.bank.controller;

import com.bank.dto.TransactionResponse;
import com.bank.service.TransactionQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
public class TransactionController {

    private final TransactionQueryService transactionQueryService;

    public TransactionController(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> list(
            @PathVariable Long accountId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication auth) {
        return ResponseEntity.ok(
                transactionQueryService.getTransactions(accountId, auth.getName(), pageable));
    }
}