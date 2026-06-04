package com.bank.notification.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanEventDTO {

    private Long loanId;
    private Long userId;
    private Long accountId;
    private BigDecimal amount;
    private String eventType;
    private String loanStatus;
    private LocalDateTime timestamp;
}
