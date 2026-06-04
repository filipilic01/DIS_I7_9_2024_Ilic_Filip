package com.bank.loan.service;

import com.bank.loan.client.AccountServiceClient;
import com.bank.loan.client.AccountServiceClient.BalanceUpdateDTO;
import com.bank.loan.dto.CreateLoanRequestDTO;
import com.bank.loan.dto.LoanPaymentRequestDTO;
import com.bank.loan.dto.LoanPaymentResponseDTO;
import com.bank.loan.dto.LoanResponseDTO;
import com.bank.loan.exception.AccountNotFoundException;
import com.bank.loan.exception.InvalidLoanStateException;
import com.bank.loan.exception.LoanNotFoundException;
import com.bank.loan.messaging.LoanEvent;
import com.bank.loan.messaging.RabbitMQProducer;
import com.bank.loan.repository.LoanPaymentRepository;
import com.bank.loan.repository.LoanRepository;
import com.bank.loan.repository.model.LoanEntity;
import com.bank.loan.repository.model.LoanEntity.LoanStatus;
import com.bank.loan.repository.model.LoanPaymentEntity;
import com.bank.loan.repository.model.LoanPaymentEntity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final AccountServiceClient accountServiceClient;
    private final RabbitMQProducer rabbitMQProducer;

    @Transactional
    public LoanResponseDTO createLoan(CreateLoanRequestDTO request) {
        var account = accountServiceClient.getAccountById(request.getAccountId());
        if (account == null) {
            throw new AccountNotFoundException(request.getAccountId());
        }

        BigDecimal monthlyInstallment = calculateMonthlyInstallment(
                request.getAmount(), request.getInterestRate(), request.getTermMonths());

        LoanEntity loan = LoanEntity.builder()
                .userId(request.getUserId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .monthlyInstallment(monthlyInstallment)
                .remainingAmount(request.getAmount())
                .type(request.getType())
                .purpose(request.getPurpose())
                .build();

        loan = loanRepository.save(loan);
        log.info("Created loan application: loanId={}, userId={}, amount={}",
                loan.getId(), loan.getUserId(), loan.getAmount());

        publishEvent(buildEvent(loan, "CREATED"));
        return toResponse(loan);
    }

    @Transactional
    public LoanResponseDTO approveLoan(Long id) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidLoanStateException(
                    "Loan cannot be approved: current status is " + loan.getStatus());
        }

        LocalDate startDate = LocalDate.now();
        loan.setStartDate(startDate);
        loan.setEndDate(startDate.plusMonths(loan.getTermMonths()));
        loan.setStatus(LoanStatus.ACTIVE);
        loan = loanRepository.save(loan);

        accountServiceClient.updateBalance(loan.getAccountId(),
                new BalanceUpdateDTO(loan.getAmount(), "CREDIT"));

        log.info("Approved loan: loanId={}, accountId={}, amount={}", id, loan.getAccountId(), loan.getAmount());
        publishEvent(buildEvent(loan, "APPROVED"));
        return toResponse(loan);
    }

    @Transactional
    public LoanResponseDTO rejectLoan(Long id) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new InvalidLoanStateException(
                    "Loan cannot be rejected: current status is " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan = loanRepository.save(loan);

        log.info("Rejected loan: loanId={}", id);
        publishEvent(buildEvent(loan, "REJECTED"));
        return toResponse(loan);
    }

    @Transactional
    public LoanPaymentResponseDTO makePayment(Long id, LoanPaymentRequestDTO request) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new InvalidLoanStateException(
                    "Payment cannot be made: loan status is " + loan.getStatus());
        }

        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        BigDecimal interestPart = loan.getRemainingAmount()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal principalPart = request.getAmount().subtract(interestPart);
        if (principalPart.compareTo(BigDecimal.ZERO) < 0) {
            principalPart = BigDecimal.ZERO;
            interestPart = request.getAmount();
        }

        accountServiceClient.updateBalance(loan.getAccountId(),
                new BalanceUpdateDTO(request.getAmount(), "DEBIT"));

        BigDecimal newRemaining = loan.getRemainingAmount().subtract(principalPart);
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            newRemaining = BigDecimal.ZERO;
            loan.setStatus(LoanStatus.PAID_OFF);
        }
        loan.setRemainingAmount(newRemaining);
        loanRepository.save(loan);

        LoanPaymentEntity payment = LoanPaymentEntity.builder()
                .loan(loan)
                .amount(request.getAmount())
                .principalPart(principalPart)
                .interestPart(interestPart)
                .paidDate(LocalDate.now())
                .status(PaymentStatus.PAID)
                .build();
        payment = loanPaymentRepository.save(payment);

        String eventType = loan.getStatus() == LoanStatus.PAID_OFF ? "PAID_OFF" : "PAYMENT_MADE";
        log.info("Payment recorded: loanId={}, amount={}, remaining={}, eventType={}",
                id, request.getAmount(), newRemaining, eventType);
        publishEvent(buildEvent(loan, eventType));
        return toPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public LoanResponseDTO getById(Long id) {
        return toResponse(loanRepository.findById(id)
                .orElseThrow(() -> new LoanNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void publishEvent(LoanEvent event) {
        try {
            rabbitMQProducer.sendLoanEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish loan event for loanId={}: {}",
                    event.getLoanId(), e.getMessage());
        }
    }

    private BigDecimal calculateMonthlyInstallment(BigDecimal amount, BigDecimal interestRate, int termMonths) {
        if (interestRate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }
        double r = interestRate.doubleValue() / 1200.0;
        double n = termMonths;
        double installment = amount.doubleValue() * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(installment).setScale(2, RoundingMode.HALF_UP);
    }

    private LoanEvent buildEvent(LoanEntity loan, String eventType) {
        return LoanEvent.builder()
                .loanId(loan.getId())
                .userId(loan.getUserId())
                .accountId(loan.getAccountId())
                .amount(loan.getAmount())
                .eventType(eventType)
                .loanStatus(loan.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private LoanResponseDTO toResponse(LoanEntity loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .accountId(loan.getAccountId())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .monthlyInstallment(loan.getMonthlyInstallment())
                .remainingAmount(loan.getRemainingAmount())
                .status(loan.getStatus())
                .type(loan.getType())
                .purpose(loan.getPurpose())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    private LoanPaymentResponseDTO toPaymentResponse(LoanPaymentEntity payment) {
        return LoanPaymentResponseDTO.builder()
                .id(payment.getId())
                .loanId(payment.getLoan().getId())
                .amount(payment.getAmount())
                .principalPart(payment.getPrincipalPart())
                .interestPart(payment.getInterestPart())
                .dueDate(payment.getDueDate())
                .paidDate(payment.getPaidDate())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
