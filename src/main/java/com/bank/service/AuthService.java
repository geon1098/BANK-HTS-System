package com.bank.service;

import com.bank.dto.LoginRequest;
import com.bank.dto.SignupRequest;
import com.bank.dto.TokenResponse;
import com.bank.entity.Member;
import com.bank.entity.Role;
import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import com.bank.repository.MemberRepository;
import com.bank.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(MemberRepository memberRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** 회원가입: 중복 체크 → 비밀번호 암호화 → 저장 */
    @Transactional
    public void signup(SignupRequest request) {
        if (memberRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        String encoded = passwordEncoder.encode(request.password());   // BCrypt 해시
        memberRepository.save(Member.create(request.username(), encoded, Role.ROLE_USER));
    }

    /** 로그인: 비밀번호 검증 → JWT 발급 */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);   // 비밀번호 불일치
        }

        String token = jwtTokenProvider.createToken(member.getUsername(), member.getRole());
        return new TokenResponse(token);
    }
}