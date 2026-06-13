package com.bank.controller;

import com.bank.dto.LoginRequest;
import com.bank.dto.SignupRequest;
import com.bank.dto.TokenResponse;
import com.bank.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndSecurityTest {
//시나리오: 토큰 없이 보호된 API를 부르면 거부(401/403), 회원가입→로그인으로 받은 토큰을 넣으면 통과.
    @Autowired MockMvc mockMvc;
    @Autowired AuthService authService;
    @Autowired ObjectMapper objectMapper;

    @Test
    void 토큰_없이_계좌생성_요청하면_거부된다() throws Exception {
        mockMvc.perform(post("/api/accounts"))
                .andExpect(status().is4xxClientError());   // 401/403
    }

    @Test
    void 로그인으로_받은_토큰으로는_계좌생성에_성공한다() throws Exception {
        // given: 회원가입 + 로그인하여 토큰 획득
        authService.signup(new SignupRequest("jwtUser", "pw1234"));
        TokenResponse token = authService.login(new LoginRequest("jwtUser", "pw1234"));

        // when & then: Authorization 헤더에 토큰을 넣으면 201
        mockMvc.perform(post("/api/accounts")
                        .header("Authorization", "Bearer " + token.accessToken()))
                .andExpect(status().isCreated());
    }
}
//팔찌(토큰) 없이 놀이기구를 타려 하면 막혀요. 매표소에서 팔찌를 받아 차고 가면 통과돼요.
