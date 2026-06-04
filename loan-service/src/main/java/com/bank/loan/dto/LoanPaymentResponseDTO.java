package com.bank.loan.dto;

import com.bank.loan.repository.model.LoanPaymentEntity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Loan payment response payload")
public class LoanPaymentResponseDTO {

    @Schema(description = "Unique payment identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the associated loan", example = "1")
    private Long loanId;

    @Schema(description = "Total payment amount", example = "440.43")
    private BigDecimal amount;

    @Schema(description = "Principal portion of the payment", example = "394.01")
    private BigDecimal principalPart;

    @Schema(description = "Interest portion of the payment", example = "46.42")
    private BigDecimal interestPart;

    @Schema(description = "Payment due date", example = "2024-02-15")
    private LocalDate dueDate;

    @Schema(description = "Date the payment was made", example = "2024-02-14")
    private LocalDate paidDate;

    @Schema(description = "Payment status", example = "PAID")
    private PaymentStatus status;

    @Schema(description = "Timestamp when the payment record was created", example = "2024-02-14T09:00:00")
    private LocalDateTime createdAt;
}
