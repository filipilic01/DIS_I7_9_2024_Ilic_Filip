package com.bank.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload for updating account balance")
public class BalanceUpdateRequestDTO {

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Amount to add (positive) or subtract (negative) from the balance", example = "500.00")
    private BigDecimal amount;

    @NotNull
    @Schema(description = "Operation type: CREDIT to add funds, DEBIT to subtract funds", example = "CREDIT")
    private OperationType operationType;

    public enum OperationType {
        CREDIT, DEBIT
    }
}
