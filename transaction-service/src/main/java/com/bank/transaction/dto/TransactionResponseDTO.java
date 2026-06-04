package com.bank.transaction.dto;

import com.bank.transaction.repository.model.TransactionEntity.TransactionStatus;
import com.bank.transaction.repository.model.TransactionEntity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Transaction response payload")
public class TransactionResponseDTO {

    @Schema(description = "Unique transaction identifier", example = "1")
    private Long id;

    @Schema(description = "Source account ID", example = "1")
    private Long sourceAccountId;

    @Schema(description = "Target account ID (only for transfers)", example = "2")
    private Long targetAccountId;

    @Schema(description = "Transaction amount", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Transaction type", example = "DEPOSIT")
    private TransactionType type;

    @Schema(description = "Transaction status", example = "COMPLETED")
    private TransactionStatus status;

    @Schema(description = "Optional description", example = "Salary deposit")
    private String description;

    @Schema(description = "Timestamp when the transaction was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the transaction was completed", example = "2024-01-15T10:30:01")
    private LocalDateTime completedAt;
}
