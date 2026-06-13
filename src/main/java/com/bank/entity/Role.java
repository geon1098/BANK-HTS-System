package com.bank.entity;

public enum Role {
    ROLE_USER,    // 일반 사용자
    ROLE_ADMIN    // 관리자
}
//스프링 시큐리티의 hasRole("ADMIN")은 내부적으로 ROLE_ADMIN 권한을 찾습니다. 규칙을 맞추려고 접두사를 붙입니다.