package com.bank.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/external-bank")
public class MockExternalBankController {
//가짜 타행 서버 Mock
	
	/** 타행 입금 시뮬레이션: 금액이 9999면 일부러 실패시킴(테스트용) */
	@PostMapping("/deposit")
	public Map<String, Object> deposit(@RequestBody Map<String, Object> body){
		int amount = (int) body.get("amount");
		if(amount == 9999) throw new RuntimeException("타행 서버 장애!");
		return Map.of("status", "OK", "amount", amount);
	}
}
