package com.bank.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bank.entity.TransactionHistory;
import com.bank.entity.TransactionType;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long>, JpaSpecificationExecutor<TransactionHistory>{

	// 특정 계좌의 거래내역을 최신순 페이징 조회
	Page<TransactionHistory> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
	
	// 송금 그룹으로 묶인 거래들(보냄/받음) 찾기
	List<TransactionHistory> findByTransferGroupId(String transferGroupId);
	
	// 이미 취소됐는지 확인용 (CANCEL 기록이 존재하는가)
	boolean existsByTransferGroupIdAndType(String transferGroupId, TransactionType type);
	
	@Query("""
		    select coalesce(sum(h.amount), 0) from TransactionHistory h
		    where h.account.id = :accountId
		      and h.type = :type
		      and h.createdAt >= :start
		""")
	BigDecimal sumAmountSince(
			@Param("accountId") Long accountId,
			@Param("type") TransactionType type,
			@Param("start") LocalDateTime start);
//오늘 보낸 금액의 합계를 구하는 쿼리
//coalesce(sum, 0) : 오늘 보낸 게 하나도 없으면 null 대신 0을 돌려줌.
}
