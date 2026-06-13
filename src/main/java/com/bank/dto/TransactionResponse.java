package com.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.entity.TransactionHistory;

public record TransactionResponse(
		Long transactionId,
		String type,
		BigDecimal amount,
		BigDecimal balanceAfter,
		String counterPartyAccountNumber,
		String performedBy,
		LocalDateTime createdAt
		) {

	public static TransactionResponse from(TransactionHistory h) {
		return new TransactionResponse(
				h.getId(), h.getType().name(), h.getAmount(),
		h.getBalanceAfter(), h.getCounterpartyAccountNumber(),
		h.getPerformedBy(), h.getCreatedAt());
	}
}
