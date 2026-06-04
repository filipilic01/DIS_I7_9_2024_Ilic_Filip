package com.bank.transaction.service;

import com.bank.transaction.client.AccountServiceClient;
import com.bank.transaction.client.AccountServiceClient.BalanceUpdateDTO;
import com.bank.transaction.dto.DepositRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.dto.TransferRequestDTO;
import com.bank.transaction.dto.WithdrawRequestDTO;
import com.bank.transaction.exception.AccountNotFoundException;
import com.bank.transaction.exception.InsufficientFundsException;
import com.bank.transaction.exception.TransactionNotFoundException;
import com.bank.transaction.messaging.RabbitMQProducer;
import com.bank.transaction.messaging.TransactionEvent;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.repository.model.TransactionEntity;
import com.bank.transaction.repository.model.TransactionEntity.TransactionStatus;
import com.bank.transaction.repository.model.TransactionEntity.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final RabbitMQProducer rabbitMQProducer;

    @Transactional
    public TransactionResponseDTO deposit(DepositRequestDTO request) {
        var account = accountServiceClient.getAccountById(request.getAccountId());
        if (account == null) {
            throw new AccountNotFoundException(request.getAccountId());
        }

        TransactionEntity transaction = TransactionEntity.builder()
                .sourceAccountId(request.getAccountId())
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        accountServiceClient.updateBalance(request.getAccountId(),
                new BalanceUpdateDTO(request.getAmount(), "CREDIT"));

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction = transactionRepository.save(transaction);

        rabbitMQProducer.sendTransactionEvent(buildEvent(transaction));
        log.info("Deposit completed: transactionId={}, accountId={}, amount={}",
                transaction.getId(), request.getAccountId(), request.getAmount());
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponseDTO withdraw(WithdrawRequestDTO request) {
        var account = accountServiceClient.getAccountById(request.getAccountId());
        if (account == null) {
            throw new AccountNotFoundException(request.getAccountId());
        }
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(account.getBalance(), request.getAmount());
        }

        TransactionEntity transaction = TransactionEntity.builder()
                .sourceAccountId(request.getAccountId())
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAWAL)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        accountServiceClient.updateBalance(request.getAccountId(),
                new BalanceUpdateDTO(request.getAmount(), "DEBIT"));

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction = transactionRepository.save(transaction);

        rabbitMQProducer.sendTransactionEvent(buildEvent(transaction));
        log.info("Withdrawal completed: transactionId={}, accountId={}, amount={}",
                transaction.getId(), request.getAccountId(), request.getAmount());
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponseDTO transfer(TransferRequestDTO request) {
        var source = accountServiceClient.getAccountById(request.getSourceAccountId());
        if (source == null) {
            throw new AccountNotFoundException(request.getSourceAccountId());
        }
        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(source.getBalance(), request.getAmount());
        }

        var target = accountServiceClient.getAccountById(request.getTargetAccountId());
        if (target == null) {
            throw new AccountNotFoundException(request.getTargetAccountId());
        }

        TransactionEntity transaction = TransactionEntity.builder()
                .sourceAccountId(request.getSourceAccountId())
                .targetAccountId(request.getTargetAccountId())
                .amount(request.getAmount())
                .type(TransactionType.TRANSFER)
                .description(request.getDescription())
                .build();
        transaction = transactionRepository.save(transaction);

        accountServiceClient.updateBalance(request.getSourceAccountId(),
                new BalanceUpdateDTO(request.getAmount(), "DEBIT"));
        accountServiceClient.updateBalance(request.getTargetAccountId(),
                new BalanceUpdateDTO(request.getAmount(), "CREDIT"));

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction = transactionRepository.save(transaction);

        rabbitMQProducer.sendTransactionEvent(buildEvent(transaction));
        log.info("Transfer completed: transactionId={}, from={}, to={}, amount={}",
                transaction.getId(), request.getSourceAccountId(), request.getTargetAccountId(), request.getAmount());
        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getById(Long id) {
        return toResponse(transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> getByAccountId(Long accountId) {
        return transactionRepository
                .findBySourceAccountIdOrTargetAccountId(accountId, accountId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TransactionEvent buildEvent(TransactionEntity t) {
        return TransactionEvent.builder()
                .transactionId(t.getId())
                .sourceAccountId(t.getSourceAccountId())
                .targetAccountId(t.getTargetAccountId())
                .amount(t.getAmount())
                .type(t.getType().name())
                .status(t.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private TransactionResponseDTO toResponse(TransactionEntity t) {
        return TransactionResponseDTO.builder()
                .id(t.getId())
                .sourceAccountId(t.getSourceAccountId())
                .targetAccountId(t.getTargetAccountId())
                .amount(t.getAmount())
                .type(t.getType())
                .status(t.getStatus())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
