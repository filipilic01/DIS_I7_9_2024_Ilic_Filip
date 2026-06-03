package com.bank.account.service;

import com.bank.account.client.UserServiceClient;
import com.bank.account.dto.AccountResponseDTO;
import com.bank.account.dto.BalanceUpdateRequestDTO;
import com.bank.account.dto.BalanceUpdateRequestDTO.OperationType;
import com.bank.account.dto.CreateAccountRequestDTO;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.InsufficientFundsException;
import com.bank.account.exception.UserNotFoundException;
import com.bank.account.repository.AccountRepository;
import com.bank.account.repository.model.AccountEntity;
import com.bank.account.repository.model.AccountEntity.AccountType;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AccountService accountService;

    private AccountEntity sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = AccountEntity.builder()
                .id(1L)
                .accountNumber("ACC-20240101-000001")
                .userId(10L)
                .type(AccountType.CHECKING)
                .balance(new BigDecimal("1000.00"))
                .currency("RSD")
                .build();
    }

    @Test
    void createAccount_success() {
        CreateAccountRequestDTO request = new CreateAccountRequestDTO();
        request.setUserId(10L);
        request.setType(AccountType.CHECKING);
        request.setCurrency("RSD");

        var userResponse = UserServiceClient.UserResponseDTO.builder()
                .id(10L).username("jdoe").status("ACTIVE").build();

        when(userServiceClient.getUserById(10L)).thenReturn(userResponse);
        when(accountRepository.save(any())).thenReturn(sampleAccount);

        AccountResponseDTO result = accountService.createAccount(request);

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getType()).isEqualTo(AccountType.CHECKING);
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void createAccount_userNotFound_throwsException() {
        CreateAccountRequestDTO request = new CreateAccountRequestDTO();
        request.setUserId(99L);
        request.setType(AccountType.SAVINGS);
        request.setCurrency("RSD");

        when(userServiceClient.getUserById(99L)).thenReturn(null);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getAccountById_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));

        AccountResponseDTO result = accountService.getAccountById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAccountNumber()).isEqualTo("ACC-20240101-000001");
    }

    @Test
    void getAccountById_notFound_throwsException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccountsByUserId_returnsList() {
        when(accountRepository.findByUserId(10L)).thenReturn(List.of(sampleAccount));

        List<AccountResponseDTO> result = accountService.getAccountsByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void updateBalance_credit_success() {
        BalanceUpdateRequestDTO request = new BalanceUpdateRequestDTO();
        request.setAmount(new BigDecimal("500.00"));
        request.setOperationType(OperationType.CREDIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any())).thenReturn(sampleAccount);

        accountService.updateBalance(1L, request);

        assertThat(sampleAccount.getBalance()).isEqualByComparingTo("1500.00");
        verify(accountRepository).save(sampleAccount);
    }

    @Test
    void updateBalance_debit_success() {
        BalanceUpdateRequestDTO request = new BalanceUpdateRequestDTO();
        request.setAmount(new BigDecimal("300.00"));
        request.setOperationType(OperationType.DEBIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any())).thenReturn(sampleAccount);

        accountService.updateBalance(1L, request);

        assertThat(sampleAccount.getBalance()).isEqualByComparingTo("700.00");
    }

    @Test
    void updateBalance_insufficientFunds_throwsException() {
        BalanceUpdateRequestDTO request = new BalanceUpdateRequestDTO();
        request.setAmount(new BigDecimal("9999.00"));
        request.setOperationType(OperationType.DEBIT);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));

        assertThatThrownBy(() -> accountService.updateBalance(1L, request))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void closeAccount_success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
        when(accountRepository.save(any())).thenReturn(sampleAccount);

        accountService.closeAccount(1L);

        assertThat(sampleAccount.getStatus()).isEqualTo(AccountEntity.AccountStatus.CLOSED);
        verify(accountRepository).save(sampleAccount);
    }
}
