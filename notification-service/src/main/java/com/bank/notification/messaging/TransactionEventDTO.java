package com.bank.notification.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEventDTO {

    private Long transactionId;
    private Long sourceAccountId;
    private Long targetAccountId;
    private BigDecimal amount;
    private String type;
    private String status;
    private LocalDateTime timestamp;
}
