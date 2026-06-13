package com.bank.service;

import com.bank.entity.Account;
import com.bank.entity.Member;
import com.bank.entity.Role;
import com.bank.repository.AccountRepository;
import com.bank.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConcurrencyTest {
//시나리오: 잔액 5000. 10명이 동시에 1000원씩 출금. 성공은 정확히 5번이어야 하고 잔액은 0, 절대 음수가 되면 안 됩니다.
    @Autowired AccountService accountService;
    @Autowired MemberRepository memberRepository;
    @Autowired AccountRepository accountRepository;

    @Test
    void 동시에_출금해도_잔액은_절대_음수가_되지_않는다() throws InterruptedException {
        // given: 잔액 5000짜리 계좌
        Member member = memberRepository.save(Member.create("racer", "hash", Role.ROLE_USER));
        accountService.createAccount("racer");
        Account account = accountRepository.findByOwnerId(member.getId()).get(0);
        accountService.deposit(account.getId(), BigDecimal.valueOf(5000), "racer");
        Long accountId = account.getId();

        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // when: 10개의 스레드가 동시에 1000원씩 출금
        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                try {
                    accountService.withdraw(accountId, BigDecimal.valueOf(1000), "racer");
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();   // 잔액부족 예외
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();   // 모든 스레드 끝날 때까지 대기
        pool.shutdown();

        // then: 성공 5번, 실패 5번, 잔액 0 (음수 아님)
        Account result = accountRepository.findById(accountId).orElseThrow();
        assertThat(success.get()).isEqualTo(5);
        assertThat(fail.get()).isEqualTo(5);
        assertThat(result.getBalance()).isEqualByComparingTo("0");
    }
}