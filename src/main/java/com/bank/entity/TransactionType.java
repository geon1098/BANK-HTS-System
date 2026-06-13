package com.bank.entity;

public enum TransactionType {//거래 종류 (감사/추적용)
    DEPOSIT,               // 입금
    WITHDRAW,              // 출금
    TRANSFER_OUT,          // 송금 보냄
    TRANSFER_IN,           // 송금 받음
    TRANSFER_CANCEL_OUT,   // 송금취소: 받았던 계좌에서 돈이 빠짐
    TRANSFER_CANCEL_IN     // 송금취소: 보냈던 계좌로 돈이 돌아옴
}