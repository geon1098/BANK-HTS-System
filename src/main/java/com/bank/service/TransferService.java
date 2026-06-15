package com.bank.service;

import com.bank.dto.TransferRequest;
import com.bank.dto.TransferResponse;
import com.bank.entity.Account;
import com.bank.entity.IdempotencyKey;
import com.bank.entity.TransactionHistory;
import com.bank.entity.TransactionType;
import com.bank.exception.BusinessException;
import com.bank.exception.ErrorCode;
import com.bank.repository.AccountRepository;
import com.bank.repository.IdempotencyKeyRepository;
import com.bank.repository.TransactionHistoryRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransferService {
	
	@Value("${transfer.per-limit}") private BigDecimal perLimit;
	@Value("${transfer.daily-limit}") private BigDecimal dailyLimit;
    private final AccountRepository accountRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public TransferService(AccountRepository accountRepository,
                           TransactionHistoryRepository transactionHistoryRepository,
                           IdempotencyKeyRepository idempotencyKeyRepository) {
        this.accountRepository = accountRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    /** 송금: 전부 성공하거나 전부 실패한다 */
    @Transactional
    public TransferResponse transfer(TransferRequest request, String username) {

        // 0) 중복 송금 방지: 같은 멱등성 키는 한 번만
        if (idempotencyKeyRepository.findByKeyValue(request.idempotencyKey()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_TRANSFER_REQUEST);
        }

        // 1) 자기 자신에게 송금 금지
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER);
        }

        // 2) 두 계좌를 id 오름차순으로 락 (교착 방지)
        Long fromId = request.fromAccountId();
        Long toId = request.toAccountId();
        lockAccount(Math.min(fromId, toId));   // 항상 작은 id부터 잠근다
        lockAccount(Math.max(fromId, toId));
        Account from = getAccount(fromId);     // 이미 락 잡힌 엔티티를 가져옴
        Account to = getAccount(toId);

        // 3) 소유권: 출금 계좌는 반드시 본인 것
        if (!from.isOwnedBy(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 4) 실제 이동 (잔액부족/비활성 시 예외 → 전체 롤백)
        BigDecimal amount = request.amount();
        from.withdraw(amount);
        to.deposit(amount);

        // 5) 거래기록 append (보냄/받음, 같은 그룹 ID)
        String groupId = UUID.randomUUID().toString();
        transactionHistoryRepository.save(TransactionHistory.ofTransfer(
                from, TransactionType.TRANSFER_OUT, amount, to.getAccountNumber(), groupId, username));
        transactionHistoryRepository.save(TransactionHistory.ofTransfer(
                to, TransactionType.TRANSFER_IN, amount, from.getAccountNumber(), groupId, username));

        // 6) 멱등성 키 저장 (다음 동일 요청 차단)
        idempotencyKeyRepository.save(IdempotencyKey.of(request.idempotencyKey(), groupId));

        return new TransferResponse(groupId,
                from.getId(), from.getBalance(),
                to.getId(), to.getBalance());
    }

    /** 송금 취소: 원거래를 지우지 않고 '역거래'를 추가한다 */
    @Transactional
    public TransferResponse cancelTransfer(String transferGroupId, String username) {

        List<TransactionHistory> group = transactionHistoryRepository.findByTransferGroupId(transferGroupId);
        if (group.isEmpty()) {
            throw new BusinessException(ErrorCode.TRANSFER_NOT_FOUND);
        }

        // 이미 취소됐으면 중복 취소 금지
        if (transactionHistoryRepository
                .existsByTransferGroupIdAndType(transferGroupId, TransactionType.TRANSFER_CANCEL_OUT)) {
            throw new BusinessException(ErrorCode.TRANSFER_ALREADY_CANCELED);
        }

        TransactionHistory out = findByType(group, TransactionType.TRANSFER_OUT); // 원래 보낸 기록
        TransactionHistory in = findByType(group, TransactionType.TRANSFER_IN);   // 원래 받은 기록

        Long fromId = out.getAccount().getId();   // 원래 보낸 계좌(돈 돌려받음)
        Long toId = in.getAccount().getId();      // 원래 받은 계좌(돈 빠져나감)
        lockAccount(Math.min(fromId, toId));      // id 오름차순 락
        lockAccount(Math.max(fromId, toId));
        Account from = getAccount(fromId);
        Account to = getAccount(toId);

        if (!from.isOwnedBy(username)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        BigDecimal amount = out.getAmount();

        // 반대로 이동: 받았던 계좌에서 출금 → 보냈던 계좌로 입금
        to.withdraw(amount);     // 받은 사람이 이미 다 써서 잔액부족이면 예외 → 롤백
        from.deposit(amount);

        // 취소도 '기록 추가'로만 (append-only)
        transactionHistoryRepository.save(TransactionHistory.ofTransfer(
                to, TransactionType.TRANSFER_CANCEL_OUT, amount, from.getAccountNumber(), transferGroupId, username));
        transactionHistoryRepository.save(TransactionHistory.ofTransfer(
                from, TransactionType.TRANSFER_CANCEL_IN, amount, to.getAccountNumber(), transferGroupId, username));

        return new TransferResponse(transferGroupId,
                from.getId(), from.getBalance(),
                to.getId(), to.getBalance());
    }

    // ===== 내부 헬퍼 =====

    /** 비관적 락(FOR UPDATE)으로 잠그며 조회 */
    private Account lockAccount(Long accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    /** 이미 같은 트랜잭션에서 락이 잡힌 계좌를 가져옴 */
    private Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

    private TransactionHistory findByType(List<TransactionHistory> group, TransactionType type) {
        return group.stream()
                .filter(h -> h.getType() == type)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSFER_NOT_FOUND));
    }
}