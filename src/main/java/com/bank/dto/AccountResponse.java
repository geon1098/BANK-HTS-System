package com.bank.dto;

import java.math.BigDecimal;

import com.bank.entity.Account;

public record AccountResponse(
		Long accountId,
		String accountNumber,
		String ownerUsername,
		BigDecimal balance,
		String status
		) {

	public static AccountResponse from(Account account) {
		return new AccountResponse(
				account.getId(),
				account.getAccountNumber(),
				account.getOwner().getUsername(),
				account.getBalance(),
				account.getStatus().name());
	}
}
