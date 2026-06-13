package com.bank.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.dto.AccountResponse;
import com.bank.repository.AccountRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final AccountRepository accountRepository;

	public AdminController(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}
	
	/** 전체 계좌 조회 (관리자 전용) */
	@GetMapping("/accounts")
	public ResponseEntity<List<AccountResponse>> allAccounts(){
		List<AccountResponse> result = accountRepository.findAll().stream()
				.map(AccountResponse::from)
				.toList();
		return ResponseEntity.ok(result);
	}
	
}
