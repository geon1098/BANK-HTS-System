package com.bank.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.entity.TransactionHistory;
import com.bank.entity.TransactionType;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long>{

	// 특정 계좌의 거래내역을 최신순 페이징 조회
	Page<TransactionHistory> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
	
	// 송금 그룹으로 묶인 거래들(보냄/받음) 찾기
	List<TransactionHistory> findByTransferGroupId(String transferGroupId);
	
	// 이미 취소됐는지 확인용 (CANCEL 기록이 존재하는가)
	boolean existsByTransferGroupIdAndType(String transferGroupId, TransactionType type);
}
