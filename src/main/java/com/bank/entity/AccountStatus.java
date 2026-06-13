package com.bank.entity;

public enum AccountStatus {
    ACTIVE,      // 정상 (거래 가능)
    SUSPENDED,   // 정지 (거래 불가)
    CLOSED       // 해지 (거래 불가)
}
//ACTIVE일 때만 거래를 허용하고, 정지/해지된 계좌는 막기 위해서입니다.