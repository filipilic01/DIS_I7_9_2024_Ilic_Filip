package com.bank.loan.dto;

import com.bank.loan.repository.model.LoanEntity.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request payload for creating a loan application")
public class CreateLoanRequestDTO {

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the user applying for the loan", example = "1")
    private Long userId;

    @NotNull(message = "Account ID is required")
    @Schema(description = "ID of the account to receive/repay the loan", example = "1")
    private Long accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.00", message = "Loan amount must be at least 100")
    @Schema(description = "Requested loan amount", example = "10000.00")
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than zero")
    @Schema(description = "Annual interest rate in percent", example = "5.5")
    private BigDecimal interestRate;

    @NotNull(message = "Term is required")
    @Min(value = 1, message = "Term must be at least 1 month")
    @Schema(description = "Loan term in months", example = "24")
    private Integer termMonths;

    @NotNull(message = "Loan type is required")
    @Schema(description = "Type of loan", example = "PERSONAL")
    private LoanType type;

    @Schema(description = "Purpose of the loan", example = "Home renovation")
    private String purpose;
}
