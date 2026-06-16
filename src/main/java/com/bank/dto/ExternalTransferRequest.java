package com.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExternalTransferRequest(
		@NotNull(message = "출금 계좌는 필수입니다.") Long fromAccountId,
		@NotBlank(message = "타행 계좌번호는 필수입니다.") String toExternalAccount,
		@NotNull @Positive(message = "금액은 0보다 커야 합니다.") BigDecimal amount
		) {}
