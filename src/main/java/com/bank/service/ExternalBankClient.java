package com.bank.service;

import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Component
public class ExternalBankClient {

    private final WebClient client;

    public ExternalBankClient(WebClient externalBankClient) {
        this.client = externalBankClient;
    }

    /** 타행에 입금 요청. 실패하면 예외 → 호출한 쪽 트랜잭션이 롤백됨 */
    public void sendToExternalBank(String toAccountNumber, BigDecimal amount) {
        try {
            client.post().uri("/external-bank/deposit")
                .bodyValue(Map.of("account", toAccountNumber, "amount", amount.intValue()))
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(3))   // 3초 안에 응답 없으면 실패 처리
                .block();
        } catch (Exception e) {
            // 타행이 죽었거나 타임아웃 → 비즈니스 예외로 바꿔 던짐
            throw new BusinessException(ErrorCode.EXTERNAL_BANK_FAILED);
        }
    }
}
