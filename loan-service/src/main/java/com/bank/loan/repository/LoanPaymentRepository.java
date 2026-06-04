package com.bank.loan.repository;

import com.bank.loan.repository.model.LoanEntity;
import com.bank.loan.repository.model.LoanPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPaymentEntity, Long> {

    List<LoanPaymentEntity> findByLoan(LoanEntity loan);
}
