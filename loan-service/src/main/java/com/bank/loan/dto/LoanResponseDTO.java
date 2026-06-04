package com.bank.loan.dto;

import com.bank.loan.repository.model.LoanEntity.LoanStatus;
import com.bank.loan.repository.model.LoanEntity.LoanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Loan response payload")
public class LoanResponseDTO {

    @Schema(description = "Unique loan identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the loan owner", example = "1")
    private Long userId;

    @Schema(description = "ID of the linked account", example = "1")
    private Long accountId;

    @Schema(description = "Original loan amount", example = "10000.00")
    private BigDecimal amount;

    @Schema(description = "Annual interest rate in percent", example = "5.5")
    private BigDecimal interestRate;

    @Schema(description = "Loan term in months", example = "24")
    private Integer termMonths;

    @Schema(description = "Calculated monthly installment", example = "440.43")
    private BigDecimal monthlyInstallment;

    @Schema(description = "Remaining amount to be repaid", example = "8800.86")
    private BigDecimal remainingAmount;

    @Schema(description = "Loan status", example = "ACTIVE")
    private LoanStatus status;

    @Schema(description = "Loan type", example = "PERSONAL")
    private LoanType type;

    @Schema(description = "Purpose of the loan", example = "Home renovation")
    private String purpose;

    @Schema(description = "Loan start date", example = "2024-01-15")
    private LocalDate startDate;

    @Schema(description = "Loan end date", example = "2026-01-15")
    private LocalDate endDate;

    @Schema(description = "Timestamp when the loan was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update", example = "2024-06-01T08:00:00")
    private LocalDateTime updatedAt;
}
