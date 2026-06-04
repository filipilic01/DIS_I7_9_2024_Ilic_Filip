package com.bank.loan.service;

import com.bank.loan.client.AccountServiceClient;
import com.bank.loan.client.AccountServiceClient.AccountDTO;
import com.bank.loan.dto.CreateLoanRequestDTO;
import com.bank.loan.dto.LoanPaymentRequestDTO;
import com.bank.loan.dto.LoanPaymentResponseDTO;
import com.bank.loan.dto.LoanResponseDTO;
import com.bank.loan.exception.AccountNotFoundException;
import com.bank.loan.exception.InvalidLoanStateException;
import com.bank.loan.exception.LoanNotFoundException;
import com.bank.loan.messaging.RabbitMQProducer;
import com.bank.loan.repository.LoanPaymentRepository;
import com.bank.loan.repository.LoanRepository;
import com.bank.loan.repository.model.LoanEntity;
import com.bank.loan.repository.model.LoanEntity.LoanStatus;
import com.bank.loan.repository.model.LoanEntity.LoanType;
import com.bank.loan.repository.model.LoanPaymentEntity;
import com.bank.loan.repository.model.LoanPaymentEntity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanPaymentRepository loanPaymentRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private LoanService loanService;

    private AccountDTO activeAccount;
    private LoanEntity pendingLoan;
    private LoanEntity activeLoan;

    @BeforeEach
    void setUp() {
        activeAccount = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("50000.00"), "ACTIVE");

        pendingLoan = LoanEntity.builder()
                .id(1L)
                .userId(10L)
                .accountId(1L)
                .amount(new BigDecimal("10000.00"))
                .interestRate(new BigDecimal("5.5"))
                .termMonths(24)
                .monthlyInstallment(new BigDecimal("440.43"))
                .remainingAmount(new BigDecimal("10000.00"))
                .status(LoanStatus.PENDING)
                .type(LoanType.PERSONAL)
                .build();

        activeLoan = LoanEntity.builder()
                .id(2L)
                .userId(10L)
                .accountId(1L)
                .amount(new BigDecimal("5000.00"))
                .interestRate(new BigDecimal("6.0"))
                .termMonths(12)
                .monthlyInstallment(new BigDecimal("430.33"))
                .remainingAmount(new BigDecimal("5000.00"))
                .status(LoanStatus.ACTIVE)
                .type(LoanType.PERSONAL)
                .build();
    }

    @Test
    void createLoan_success() {
        CreateLoanRequestDTO request = new CreateLoanRequestDTO();
        request.setUserId(10L);
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("10000.00"));
        request.setInterestRate(new BigDecimal("5.5"));
        request.setTermMonths(24);
        request.setType(LoanType.PERSONAL);

        when(accountServiceClient.getAccountById(1L)).thenReturn(activeAccount);
        when(loanRepository.save(any())).thenReturn(pendingLoan);

        LoanResponseDTO result = loanService.createLoan(request);

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getAmount()).isEqualByComparingTo("10000.00");
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
        verify(rabbitMQProducer).sendLoanEvent(any());
    }

    @Test
    void createLoan_accountNotFound_throwsException() {
        CreateLoanRequestDTO request = new CreateLoanRequestDTO();
        request.setUserId(10L);
        request.setAccountId(99L);
        request.setAmount(new BigDecimal("10000.00"));
        request.setInterestRate(new BigDecimal("5.5"));
        request.setTermMonths(24);
        request.setType(LoanType.PERSONAL);

        when(accountServiceClient.getAccountById(99L)).thenReturn(null);

        assertThatThrownBy(() -> loanService.createLoan(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(loanRepository, never()).save(any());
    }

    @Test
    void approveLoan_success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any())).thenReturn(pendingLoan);
        when(accountServiceClient.updateBalance(anyLong(), any())).thenReturn(activeAccount);

        LoanResponseDTO result = loanService.approveLoan(1L);

        assertThat(pendingLoan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        verify(accountServiceClient).updateBalance(eq(1L), any());
        verify(rabbitMQProducer).sendLoanEvent(any());
    }

    @Test
    void approveLoan_notPending_throwsException() {
        activeLoan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findById(2L)).thenReturn(Optional.of(activeLoan));

        assertThatThrownBy(() -> loanService.approveLoan(2L))
                .isInstanceOf(InvalidLoanStateException.class);

        verify(accountServiceClient, never()).updateBalance(anyLong(), any());
    }

    @Test
    void approveLoan_notFound_throwsException() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.approveLoan(99L))
                .isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    void rejectLoan_success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any())).thenReturn(pendingLoan);

        LoanResponseDTO result = loanService.rejectLoan(1L);

        assertThat(pendingLoan.getStatus()).isEqualTo(LoanStatus.REJECTED);
        verify(rabbitMQProducer).sendLoanEvent(any());
    }

    @Test
    void rejectLoan_notPending_throwsException() {
        when(loanRepository.findById(2L)).thenReturn(Optional.of(activeLoan));

        assertThatThrownBy(() -> loanService.rejectLoan(2L))
                .isInstanceOf(InvalidLoanStateException.class);
    }

    @Test
    void makePayment_success() {
        LoanPaymentRequestDTO request = new LoanPaymentRequestDTO();
        request.setAmount(new BigDecimal("430.33"));

        LoanPaymentEntity savedPayment = LoanPaymentEntity.builder()
                .id(1L)
                .loan(activeLoan)
                .amount(new BigDecimal("430.33"))
                .principalPart(new BigDecimal("405.33"))
                .interestPart(new BigDecimal("25.00"))
                .status(PaymentStatus.PAID)
                .build();

        when(loanRepository.findById(2L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any())).thenReturn(activeLoan);
        when(accountServiceClient.updateBalance(anyLong(), any())).thenReturn(activeAccount);
        when(loanPaymentRepository.save(any())).thenReturn(savedPayment);

        LoanPaymentResponseDTO result = loanService.makePayment(2L, request);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getLoanId()).isEqualTo(2L);
        verify(accountServiceClient).updateBalance(eq(1L), any());
        verify(rabbitMQProducer).sendLoanEvent(any());
    }

    @Test
    void makePayment_loanNotActive_throwsException() {
        LoanPaymentRequestDTO request = new LoanPaymentRequestDTO();
        request.setAmount(new BigDecimal("440.43"));

        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

        assertThatThrownBy(() -> loanService.makePayment(1L, request))
                .isInstanceOf(InvalidLoanStateException.class);

        verify(accountServiceClient, never()).updateBalance(anyLong(), any());
    }

    @Test
    void makePayment_fullRepayment_setsPaidOff() {
        activeLoan.setRemainingAmount(new BigDecimal("430.33"));

        LoanPaymentRequestDTO request = new LoanPaymentRequestDTO();
        request.setAmount(new BigDecimal("432.48"));

        LoanPaymentEntity savedPayment = LoanPaymentEntity.builder()
                .id(2L)
                .loan(activeLoan)
                .amount(new BigDecimal("432.48"))
                .principalPart(new BigDecimal("430.33"))
                .interestPart(new BigDecimal("2.15"))
                .status(PaymentStatus.PAID)
                .build();

        when(loanRepository.findById(2L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any())).thenReturn(activeLoan);
        when(accountServiceClient.updateBalance(anyLong(), any())).thenReturn(activeAccount);
        when(loanPaymentRepository.save(any())).thenReturn(savedPayment);

        loanService.makePayment(2L, request);

        assertThat(activeLoan.getStatus()).isEqualTo(LoanStatus.PAID_OFF);
        assertThat(activeLoan.getRemainingAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void getById_found() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

        LoanResponseDTO result = loanService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void getById_notFound_throwsException() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.getById(99L))
                .isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    void getByUserId_returnsList() {
        when(loanRepository.findByUserId(10L)).thenReturn(List.of(pendingLoan, activeLoan));

        List<LoanResponseDTO> result = loanService.getByUserId(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
