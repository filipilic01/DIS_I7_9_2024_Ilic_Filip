package com.bank.account.dto;

import com.bank.account.repository.model.AccountEntity.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a new bank account")
public class CreateAccountRequestDTO {

    @NotNull
    @Positive
    @Schema(description = "ID of the user who owns this account", example = "1")
    private Long userId;

    @NotNull
    @Schema(description = "Type of bank account", example = "CHECKING")
    private AccountType type;

    @NotBlank
    @Schema(description = "Currency code (ISO 4217)", example = "RSD", defaultValue = "RSD")
    private String currency;
}
