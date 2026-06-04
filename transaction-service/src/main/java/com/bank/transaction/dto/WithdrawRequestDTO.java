package com.bank.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload for withdrawing funds from an account")
public class WithdrawRequestDTO {

    @NotNull(message = "Account ID is required")
    @Schema(description = "Source account ID", example = "1")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount to withdraw", example = "200.00")
    private BigDecimal amount;

    @Schema(description = "Optional description", example = "ATM withdrawal")
    private String description;
}
