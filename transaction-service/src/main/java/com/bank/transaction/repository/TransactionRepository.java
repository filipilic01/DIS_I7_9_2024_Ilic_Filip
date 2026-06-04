package com.bank.transaction.repository;

import com.bank.transaction.repository.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findBySourceAccountIdOrTargetAccountId(Long sourceAccountId, Long targetAccountId);
}
