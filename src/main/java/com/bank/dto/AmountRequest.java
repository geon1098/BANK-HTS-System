package com.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AmountRequest(
		@NotNull(message = "금액은 필수입니다.")
		@Positive(message = "금액은 0보다 커야 합니다.")
		BigDecimal amount
		) {}
