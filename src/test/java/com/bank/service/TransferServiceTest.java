package com.bank.service;

import com.bank.dto.TransferRequest;
import com.bank.entity.Account;
import com.bank.entity.Member;
import com.bank.entity.Role;
import com.bank.repository.AccountRepository;
import com.bank.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TransferServiceTest {
//왜 테스트? 송금은 눈으로 한 번 성공했다고 안심하면 안 됩니다. 실패할 때 정말 롤백되는지, 동시에 들어올 때 잔액이 안 깨지는지는 테스트로만 확인됩니다. 
    @Autowired TransferService transferService;
    @Autowired AccountService accountService;
    @Autowired MemberRepository memberRepository;
    @Autowired AccountRepository accountRepository;

    /** 계좌 하나 만들고 입금까지 해서 돌려주는 헬퍼 */
    private Account createAccountWith(String username, long balance) {
        Member member = memberRepository.save(Member.create(username, "hash", Role.ROLE_USER));
        accountService.createAccount(username);
        Account account = accountRepository.findByOwnerId(member.getId()).get(0);
        if (balance > 0) {   // 입금액은 0보다 커야 하므로 0원이면 건너뛴다
            accountService.deposit(account.getId(), BigDecimal.valueOf(balance), username);
        }
        return accountRepository.findById(account.getId()).orElseThrow();
    }

    @Test
    void 송금_성공시_총액은_변하지_않는다() {
        // given: A=10000, B=5000  (총액 15000)
        Account a = createAccountWith("userA", 10000);
        Account b = createAccountWith("userB", 5000);
        BigDecimal before = a.getBalance().add(b.getBalance());

        // when: A → B 3000 송금
        transferService.transfer(
                new TransferRequest(a.getId(), b.getId(), BigDecimal.valueOf(3000), UUID.randomUUID().toString()),
                "userA");

        // then: A=7000, B=8000, 총액 그대로 15000
        Account afterA = accountRepository.findById(a.getId()).orElseThrow();
        Account afterB = accountRepository.findById(b.getId()).orElseThrow();
        assertThat(afterA.getBalance()).isEqualByComparingTo("7000");
        assertThat(afterB.getBalance()).isEqualByComparingTo("8000");
        assertThat(afterA.getBalance().add(afterB.getBalance())).isEqualByComparingTo(before);
    }

    @Test
    void 잔액이_부족하면_송금은_실패하고_전체_롤백된다() {
        // given: A=1000, B=5000
        Account a = createAccountWith("userC", 1000);
        Account b = createAccountWith("userD", 5000);

        // when & then: A가 3000 송금 시도 → 잔액부족 예외
        assertThatThrownBy(() -> transferService.transfer(
                new TransferRequest(a.getId(), b.getId(), BigDecimal.valueOf(3000), UUID.randomUUID().toString()),
                "userC"))
                .isInstanceOf(RuntimeException.class);

        // then: 두 계좌 잔액이 '그대로'여야 한다 (롤백 검증)
        Account afterA = accountRepository.findById(a.getId()).orElseThrow();
        Account afterB = accountRepository.findById(b.getId()).orElseThrow();
        assertThat(afterA.getBalance()).isEqualByComparingTo("1000");
        assertThat(afterB.getBalance()).isEqualByComparingTo("5000");
    }
    
    @Test
    void 같은_멱등성키로_두번_송금하면_한번만_처리된다() {
        Account a = createAccountWith("userE", 10000);
        Account b = createAccountWith("userF", 0);
        String key = UUID.randomUUID().toString();   // 같은 키 재사용

        TransferRequest req = new TransferRequest(
                a.getId(), b.getId(), BigDecimal.valueOf(3000), key);

        transferService.transfer(req, "userE");                       // 1번째: 성공
        assertThatThrownBy(() -> transferService.transfer(req, "userE"))  // 2번째: 거부
                .isInstanceOf(RuntimeException.class);

        // B는 3000만 받았어야 한다 (6000이면 중복 처리된 것)
        Account afterB = accountRepository.findById(b.getId()).orElseThrow();
        assertThat(afterB.getBalance()).isEqualByComparingTo("3000");
    }
}
//같은 주문번호로 두 번 시켜도 음료수는 하나만 나와야 해요. B 잔액이 3000이면 합격, 6000이면 불합격(두 번 처리됨).
