package com.bank.loan.controller;

import com.bank.loan.dto.CreateLoanRequestDTO;
import com.bank.loan.dto.LoanPaymentRequestDTO;
import com.bank.loan.dto.LoanPaymentResponseDTO;
import com.bank.loan.dto.LoanResponseDTO;
import com.bank.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan management API")
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Apply for a loan",
               description = "Creates a new loan application in PENDING status. Monthly installment is auto-calculated.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Loan application created",
                content = @Content(schema = @Schema(implementation = LoanResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<LoanResponseDTO> createLoan(@Valid @RequestBody CreateLoanRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.createLoan(request));
    }

    @Operation(summary = "Approve a loan",
               description = "Approves a PENDING loan, disburses funds to the linked account, and sets status to ACTIVE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Loan approved and funds disbursed",
                content = @Content(schema = @Schema(implementation = LoanResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Loan is not in PENDING state", content = @Content)
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<LoanResponseDTO> approveLoan(
            @Parameter(description = "Loan ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    @Operation(summary = "Reject a loan",
               description = "Rejects a PENDING loan application.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Loan rejected",
                content = @Content(schema = @Schema(implementation = LoanResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Loan is not in PENDING state", content = @Content)
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<LoanResponseDTO> rejectLoan(
            @Parameter(description = "Loan ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.rejectLoan(id));
    }

    @Operation(summary = "Make a loan payment",
               description = "Records a payment against an ACTIVE loan. Automatically splits into principal and interest. Sets status to PAID_OFF when fully repaid.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Payment recorded successfully",
                content = @Content(schema = @Schema(implementation = LoanPaymentResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
        @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Loan is not in ACTIVE state", content = @Content)
    })
    @PostMapping("/{id}/payment")
    public ResponseEntity<LoanPaymentResponseDTO> makePayment(
            @Parameter(description = "Loan ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody LoanPaymentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.makePayment(id, request));
    }

    @Operation(summary = "Get loan by ID",
               description = "Returns details of a specific loan.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Loan found",
                content = @Content(schema = @Schema(implementation = LoanResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponseDTO> getById(
            @Parameter(description = "Loan ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(loanService.getById(id));
    }

    @Operation(summary = "Get all loans for a user",
               description = "Returns all loan applications belonging to the specified user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully",
                content = @Content(schema = @Schema(implementation = LoanResponseDTO.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponseDTO>> getByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getByUserId(userId));
    }
}
