package com.bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient externalBankWebClient() {
		return WebClient.builder()
				.baseUrl("http://localhost:8092")// 데모: 우리 안의 가짜 타행
				
				.build();
	}
}
