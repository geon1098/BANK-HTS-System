package com.bank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.entity.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long>{

	boolean existsByAccountNumber(String accountNumber);
	List<Account> findByOwnerId(Long ownerId);
	
    /**
     * ★ 비관적 쓰기 락(Pessimistic Write Lock)
     * 이 메서드로 계좌를 조회하면 DB가 그 행에 자물쇠를 건다(SELECT ... FOR UPDATE).
     * 다른 트랜잭션은 이 계좌를 읽으려면 자물쇠가 풀릴 때까지 기다린다.
     * → 동시 출금/송금이 들어와도 한 번에 하나씩 처리되어 잔액이 꼬이지 않는다.
     */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Account a where a.id = :id")
	Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
