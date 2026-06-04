package com.bank.transaction.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "account-service", fallback = AccountServiceClient.AccountServiceFallback.class)
public interface AccountServiceClient {

    @GetMapping("/accounts/{id}")
    AccountDTO getAccountById(@PathVariable Long id);

    @PutMapping("/accounts/{id}/balance")
    AccountDTO updateBalance(@PathVariable Long id, @RequestBody BalanceUpdateDTO request);

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class AccountDTO {
        private Long id;
        private String accountNumber;
        private Long userId;
        private BigDecimal balance;
        private String status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class BalanceUpdateDTO {
        private BigDecimal amount;
        private String operationType;
    }

    class AccountServiceFallback implements AccountServiceClient {
        @Override
        public AccountDTO getAccountById(Long id) {
            return null;
        }

        @Override
        public AccountDTO updateBalance(Long id, BalanceUpdateDTO request) {
            throw new RuntimeException("account-service is unavailable");
        }
    }
}
