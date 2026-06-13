package com.bank.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // PK 자동 증가
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;                              // 로그인 아이디(중복 불가)

    @Column(nullable = false)
    private String password;                              // BCrypt 해시값

    @Enumerated(EnumType.STRING)                          // enum을 문자열로 저장
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Member() {}                                 // JPA 전용 기본 생성자

    private Member(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    /** 정적 팩토리: 생성 통로를 하나로 통일 (항상 유효한 상태로 생성) */
    public static Member create(String username, String encodedPassword, Role role) {
        return new Member(username, encodedPassword, role);
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}