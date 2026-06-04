package com.bank.loan.repository;

import com.bank.loan.repository.model.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findByUserId(Long userId);
}
