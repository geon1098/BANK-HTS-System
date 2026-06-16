package com.bank.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 회원/인증
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 계좌
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다."),
    ACCOUNT_NOT_ACTIVE(HttpStatus.CONFLICT, "활성 상태(ACTIVE)인 계좌만 거래할 수 있습니다."),

    // 금액/거래
    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다."),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "잔액이 부족합니다."),

    // 송금
    SAME_ACCOUNT_TRANSFER(HttpStatus.BAD_REQUEST, "출금 계좌와 입금 계좌가 동일합니다."),
    DUPLICATE_TRANSFER_REQUEST(HttpStatus.CONFLICT, "이미 처리된 송금 요청입니다."),
    TRANSFER_NOT_FOUND(HttpStatus.NOT_FOUND, "송금 내역을 찾을 수 없습니다."),
    TRANSFER_ALREADY_CANCELED(HttpStatus.CONFLICT, "이미 취소된 송금입니다."),
	EXCEED_PER_TRANSFER_LIMIT(HttpStatus.BAD_REQUEST, "1회 이체 한도를 초과했습니다."),
	EXTERNAL_BANK_FAILED(HttpStatus.BAD_GATEWAY, "타행 서버 연동에 실패했습니다."),
	EXCEED_DAILY_LIMIT(HttpStatus.BAD_REQUEST, "일일 이체 한도를 초과했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}