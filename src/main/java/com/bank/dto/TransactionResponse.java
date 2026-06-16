package com.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.bank.entity.TransactionHistory;
import com.bank.entity.TransactionType;

public record TransactionResponse(
		Long transactionId,
		String type,	//데포짓, 윗드로우, 트랜스퍼_아웃 ...
		String direction, // In(들어옴) / OUT(나감) <- 추가함.
		BigDecimal amount,
		BigDecimal balanceAfter, // 거래 직후 잔액(잔액 변동 추적)
		String counterPartyAccountNumber,
		String performedBy,
		LocalDateTime createdAt
		) {

	public static TransactionResponse from(TransactionHistory h) {
		return new TransactionResponse(
				h.getId(), h.getType().name(), 
				resolveDirection(h.getType()), h.getAmount(),
		h.getBalanceAfter(), h.getCounterpartyAccountNumber(),
		h.getPerformedBy(), h.getCreatedAt());
	}
	
	private static String resolveDirection(TransactionType type) {
		return switch(type) {
		case DEPOSIT, TRANSFER_IN, TRANSFER_CANCEL_IN -> "IN";
		case WITHDRAW, TRANSFER_OUT, TRANSFER_CANCEL_OUT -> "OUT";
		};
	}
}
