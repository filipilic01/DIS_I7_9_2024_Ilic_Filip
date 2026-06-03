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
import com.bank.account.repository.model.AccountEntity.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserServiceClient userServiceClient;

    @Transactional
    public AccountResponseDTO createAccount(CreateAccountRequestDTO request) {
        var user = userServiceClient.getUserById(request.getUserId());
        if (user == null) {
            throw new UserNotFoundException(request.getUserId());
        }

        AccountEntity account = AccountEntity.builder()
                .accountNumber(generateAccountNumber())
                .userId(request.getUserId())
                .type(request.getType())
                .currency(request.getCurrency())
                .build();

        AccountEntity saved = accountRepository.save(account);
        log.info("Created account id={} for userId={}", saved.getId(), saved.getUserId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(Long id) {
        return toResponse(accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountByNumber(String accountNumber) {
        return toResponse(accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber)));
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AccountResponseDTO updateBalance(Long id, BalanceUpdateRequestDTO request) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (request.getOperationType() == OperationType.DEBIT) {
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(account.getBalance(), request.getAmount());
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(request.getAmount()));
        }

        log.info("Updated balance for accountId={}, operation={}, amount={}",
                id, request.getOperationType(), request.getAmount());
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponseDTO closeAccount(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        account.setStatus(AccountStatus.CLOSED);
        log.info("Closed account id={}", id);
        return toResponse(accountRepository.save(account));
    }

    private String generateAccountNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%06d", (long) (Math.random() * 1_000_000));
        return "ACC-" + datePart + "-" + randomPart;
    }

    private AccountResponseDTO toResponse(AccountEntity account) {
        return AccountResponseDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .type(account.getType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
