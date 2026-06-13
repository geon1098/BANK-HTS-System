package com.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
		@NotNull(message = "출금 계좌는 필수입니다.") Long fromAccountId,
		@NotNull(message = "입금 계좌는 필수입니다.") Long toAccountId,
		@NotNull @Positive(message = "금액은 0보다 커야 합니다.") BigDecimal amount,
		@NotNull(message = "멱등성 키는 필수입니다.") String idempotencyKey// 중복 방지 키
		) {}