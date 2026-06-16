package com.bank.repository;

import com.bank.entity.TransactionHistory;
import com.bank.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;

public class TransactionSpecs {

    public static Specification<TransactionHistory> ofAccount(Long accountId) {
        return (root, q, cb) -> cb.equal(root.get("account").get("id"), accountId);
    }
    public static Specification<TransactionHistory> typeEq(TransactionType type) {
        return (root, q, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }
    public static Specification<TransactionHistory> createdAfter(LocalDateTime from) {
        return (root, q, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }
}//null이면 그 조건은 무시 → "값 있으면 거르고, 없으면 통과"가 자동으로 됨.