package com.bank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload for transferring funds between accounts")
public class TransferRequestDTO {

    @NotNull(message = "Source account ID is required")
    @Schema(description = "Source account ID to debit", example = "1")
    private Long sourceAccountId;

    @NotNull(message = "Target account ID is required")
    @Schema(description = "Target account ID to credit", example = "2")
    private Long targetAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount to transfer", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Optional description", example = "Rent payment")
    private String description;
}
