package com.bank.security;

import com.bank.entity.Member;
import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import com.bank.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 스프링 시큐리티가 이해하는 User 객체로 변환 (권한 포함)
        return User.builder()
                .username(member.getUsername())
                .password(member.getPassword())   // BCrypt 해시
                .authorities(List.of(new SimpleGrantedAuthority(member.getRole().name())))
                .build();
    }
}