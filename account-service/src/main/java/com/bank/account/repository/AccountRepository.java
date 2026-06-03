package com.bank.account.repository;

import com.bank.account.repository.model.AccountEntity;
import com.bank.account.repository.model.AccountEntity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    List<AccountEntity> findByUserId(Long userId);
    List<AccountEntity> findByUserIdAndStatus(Long userId, AccountStatus status);
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
}
