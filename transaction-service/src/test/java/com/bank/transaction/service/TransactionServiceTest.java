package com.bank.transaction.service;

import com.bank.transaction.client.AccountServiceClient;
import com.bank.transaction.client.AccountServiceClient.AccountDTO;
import com.bank.transaction.dto.DepositRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.dto.TransferRequestDTO;
import com.bank.transaction.dto.WithdrawRequestDTO;
import com.bank.transaction.exception.AccountNotFoundException;
import com.bank.transaction.exception.InsufficientFundsException;
import com.bank.transaction.exception.TransactionNotFoundException;
import com.bank.transaction.messaging.RabbitMQProducer;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.repository.model.TransactionEntity;
import com.bank.transaction.repository.model.TransactionEntity.TransactionStatus;
import com.bank.transaction.repository.model.TransactionEntity.TransactionType;
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
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private RabbitMQProducer rabbitMQProducer;

    @InjectMocks
    private TransactionService transactionService;

    private AccountDTO activeAccount;
    private TransactionEntity sampleTransaction;

    @BeforeEach
    void setUp() {
        activeAccount = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("1000.00"), "ACTIVE");

        sampleTransaction = TransactionEntity.builder()
                .id(1L)
                .sourceAccountId(1L)
                .amount(new BigDecimal("500.00"))
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .build();
    }

    @Test
    void deposit_success() {
        DepositRequestDTO request = new DepositRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("500.00"));

        when(accountServiceClient.getAccountById(1L)).thenReturn(activeAccount);
        when(transactionRepository.save(any())).thenReturn(sampleTransaction);

        TransactionResponseDTO result = transactionService.deposit(request);

        assertThat(result.getSourceAccountId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        verify(accountServiceClient).updateBalance(eq(1L), any());
        verify(rabbitMQProducer).sendTransactionEvent(any());
    }

    @Test
    void deposit_accountNotFound_throwsException() {
        DepositRequestDTO request = new DepositRequestDTO();
        request.setAccountId(99L);
        request.setAmount(new BigDecimal("100.00"));

        when(accountServiceClient.getAccountById(99L)).thenReturn(null);

        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(AccountNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void withdraw_success() {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("300.00"));

        TransactionEntity withdrawTransaction = TransactionEntity.builder()
                .id(2L)
                .sourceAccountId(1L)
                .amount(new BigDecimal("300.00"))
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(accountServiceClient.getAccountById(1L)).thenReturn(activeAccount);
        when(transactionRepository.save(any())).thenReturn(withdrawTransaction);

        TransactionResponseDTO result = transactionService.withdraw(request);

        assertThat(result.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        verify(accountServiceClient).updateBalance(eq(1L), any());
        verify(rabbitMQProducer).sendTransactionEvent(any());
    }

    @Test
    void withdraw_accountNotFound_throwsException() {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setAccountId(99L);
        request.setAmount(new BigDecimal("100.00"));

        when(accountServiceClient.getAccountById(99L)).thenReturn(null);

        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void withdraw_insufficientFunds_throwsException() {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("9999.00"));

        when(accountServiceClient.getAccountById(1L)).thenReturn(activeAccount);

        assertThatThrownBy(() -> transactionService.withdraw(request))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountServiceClient, never()).updateBalance(anyLong(), any());
    }

    @Test
    void transfer_success() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setSourceAccountId(1L);
        request.setTargetAccountId(2L);
        request.setAmount(new BigDecimal("200.00"));

        AccountDTO targetAccount = new AccountDTO(2L, "ACC-20240101-000002", 20L,
                new BigDecimal("500.00"), "ACTIVE");

        TransactionEntity transferTransaction = TransactionEntity.builder()
                .id(3L)
                .sourceAccountId(1L)
                .targetAccountId(2L)
                .amount(new BigDecimal("200.00"))
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(accountServiceClient.getAccountById(1L)).thenReturn(activeAccount);
        when(accountServiceClient.getAccountById(2L)).thenReturn(targetAccount);
        when(transactionRepository.save(any())).thenReturn(transferTransaction);

        TransactionResponseDTO result = transactionService.transfer(request);

        assertThat(result.getType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(result.getSourceAccountId()).isEqualTo(1L);
        assertThat(result.getTargetAccountId()).isEqualTo(2L);
        verify(accountServiceClient, times(2)).updateBalance(anyLong(), any());
        verify(rabbitMQProducer).sendTransactionEvent(any());
    }

    @Test
    void transfer_sourceAccountNotFound_throwsException() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setSourceAccountId(99L);
        request.setTargetAccountId(2L);
        request.setAmount(new BigDecimal("100.00"));

        when(accountServiceClient.getAccountById(99L)).thenReturn(null);

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transfer_insufficientFunds_throwsException() {
        TransferRequestDTO request = new TransferRequestDTO();
        request.setSourceAccountId(1L);
        request.setTargetAccountId(2L);
        request.setAmount(new BigDecimal("9999.00"));

        when(accountServiceClient.getAccountById(1L)).thenReturn(activeAccount);

        assertThatThrownBy(() -> transactionService.transfer(request))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void getById_found() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(sampleTransaction));

        TransactionResponseDTO result = transactionService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getType()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void getById_notFound_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getById(99L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void getByAccountId_returnsList() {
        when(transactionRepository.findBySourceAccountIdOrTargetAccountId(1L, 1L))
                .thenReturn(List.of(sampleTransaction));

        List<TransactionResponseDTO> result = transactionService.getByAccountId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSourceAccountId()).isEqualTo(1L);
    }
}
