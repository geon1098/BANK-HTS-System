package com.bank.controller;

import com.bank.dto.TransactionResponse;
import com.bank.entity.TransactionType;
import com.bank.service.TransactionQueryService;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
            @PathVariable("accountId") Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            Authentication auth) {
        return ResponseEntity.ok(
                transactionQueryService.getTransactions(accountId, auth.getName(),type,from, pageable));
    }
}//호출예시: GET /api/accounts/1/transactions?type=DEPOSIT&from=2026-06-01T00:00:00&page=0&size=10
//"1번 계좌의, 입금만, 6월1일 이후, 첫 페이지 10개"