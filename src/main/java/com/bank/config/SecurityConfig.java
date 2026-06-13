package com.bank.config;

import com.bank.security.CustomUserDetailsService;
import com.bank.security.JwtAuthenticationFilter;
import com.bank.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity   // @PreAuthorize 같은 메서드 단위 보안 사용 가능
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /** 비밀번호 암호화기: BCrypt */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** 로그인 인증 매니저 (필요 시 사용) */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);

        http
            .csrf(csrf -> csrf.disable())               // REST API라 CSRF 불필요
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 안 씀(JWT)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()  // 회원가입/로그인은 누구나
                .requestMatchers("/api/admin/**").hasRole("ADMIN")              // 관리자 전용
                .anyRequest().authenticated())                                  // 나머지는 로그인 필요
            .headers(h -> h.frameOptions(f -> f.disable()))                     // h2-console 화면 허용
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class); // JWT 필터 끼우기

        return http.build();
    }
}