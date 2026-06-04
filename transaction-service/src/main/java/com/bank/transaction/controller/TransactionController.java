package com.bank.transaction.controller;

import com.bank.transaction.dto.DepositRequestDTO;
import com.bank.transaction.dto.TransactionResponseDTO;
import com.bank.transaction.dto.TransferRequestDTO;
import com.bank.transaction.dto.WithdrawRequestDTO;
import com.bank.transaction.service.TransactionService;
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
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Banking transaction management API")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Deposit funds",
               description = "Credits the specified amount to the given account and records the transaction.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Deposit completed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDTO> deposit(@Valid @RequestBody DepositRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.deposit(request));
    }

    @Operation(summary = "Withdraw funds",
               description = "Debits the specified amount from the given account. Fails if funds are insufficient.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Withdrawal completed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
        @ApiResponse(responseCode = "422", description = "Insufficient funds", content = @Content)
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDTO> withdraw(@Valid @RequestBody WithdrawRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.withdraw(request));
    }

    @Operation(summary = "Transfer funds between accounts",
               description = "Debits source account and credits target account. Fails if source has insufficient funds.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transfer completed successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
        @ApiResponse(responseCode = "422", description = "Insufficient funds", content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponseDTO> transfer(@Valid @RequestBody TransferRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(request));
    }

    @Operation(summary = "Get transaction by ID",
               description = "Returns details of a specific transaction.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found",
                content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Transaction not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getById(
            @Parameter(description = "Transaction ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getById(id));
    }

    @Operation(summary = "Get all transactions for an account",
               description = "Returns all transactions where the given account is either source or target.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully",
                content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class)))
    })
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponseDTO>> getByAccountId(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long accountId) {
        return ResponseEntity.ok(transactionService.getByAccountId(accountId));
    }
}
