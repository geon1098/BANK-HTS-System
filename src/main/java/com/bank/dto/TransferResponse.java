// dto/TransferResponse.java
package com.bank.dto;

import java.math.BigDecimal;

public record TransferResponse(
        String transferGroupId,    // 이 송금의 묶음 ID (취소할 때 사용)
        Long fromAccountId, BigDecimal fromBalance,
        Long toAccountId,   BigDecimal toBalance
) {}