package com.bank.controller;

import com.bank.dto.AccountResponse;
import com.bank.dto.AmountRequest;
import com.bank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(auth.getName()));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> get(@PathVariable("accountId") Long accountId, Authentication auth) {
        return ResponseEntity.ok(accountService.getAccount(accountId, auth.getName()));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountResponse> deposit(@PathVariable("accountId") Long accountId,
                                                   @Valid @RequestBody AmountRequest request,
                                                   Authentication auth) {
        return ResponseEntity.ok(accountService.deposit(accountId, request.amount(), auth.getName()));
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable("accountId") Long accountId,
                                                    @Valid @RequestBody AmountRequest request,
                                                    Authentication auth) {
        return ResponseEntity.ok(accountService.withdraw(accountId, request.amount(), auth.getName()));
    }
}