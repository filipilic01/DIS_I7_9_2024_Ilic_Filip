package com.bank.account.dto;

import com.bank.account.repository.model.AccountEntity.AccountStatus;
import com.bank.account.repository.model.AccountEntity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Bank account response payload")
public class AccountResponseDTO {

    @Schema(description = "Unique account identifier", example = "1")
    private Long id;

    @Schema(description = "Unique account number", example = "ACC-20240101-000001")
    private String accountNumber;

    @Schema(description = "ID of the account owner", example = "1")
    private Long userId;

    @Schema(description = "Account type", example = "CHECKING")
    private AccountType type;

    @Schema(description = "Current balance", example = "15000.00")
    private BigDecimal balance;

    @Schema(description = "Currency code", example = "RSD")
    private String currency;

    @Schema(description = "Account status", example = "ACTIVE")
    private AccountStatus status;

    @Schema(description = "Timestamp when the account was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update", example = "2024-06-01T08:00:00")
    private LocalDateTime updatedAt;
}
