package com.bank.account.controller;

import com.bank.account.dto.AccountResponseDTO;
import com.bank.account.dto.BalanceUpdateRequestDTO;
import com.bank.account.dto.CreateAccountRequestDTO;
import com.bank.account.service.AccountService;
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
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management API")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Create a new bank account",
               description = "Creates a new bank account for an existing user. Validates that the user exists via user-service.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created successfully",
                content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody CreateAccountRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request));
    }

    @Operation(summary = "Get account by ID",
               description = "Returns bank account details for the given account ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found",
                content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> getAccountById(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @Operation(summary = "Get account by account number",
               description = "Returns bank account details for the given account number.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found",
                content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDTO> getAccountByNumber(
            @Parameter(description = "Account number", required = true, example = "ACC-20240101-000001")
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @Operation(summary = "Get all accounts for a user",
               description = "Returns all bank accounts belonging to the specified user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully",
                content = @Content(schema = @Schema(implementation = AccountResponseDTO.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountResponseDTO>> getAccountsByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(userId));
    }

    @Operation(summary = "Update account balance",
               description = "Credits or debits the account balance. DEBIT operations are rejected if funds are insufficient.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Balance updated successfully",
                content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content),
        @ApiResponse(responseCode = "422", description = "Insufficient funds", content = @Content)
    })
    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountResponseDTO> updateBalance(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody BalanceUpdateRequestDTO request) {
        return ResponseEntity.ok(accountService.updateBalance(id, request));
    }

    @Operation(summary = "Close an account",
               description = "Sets the account status to CLOSED. Closed accounts cannot perform transactions.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account closed successfully",
                content = @Content(schema = @Schema(implementation = AccountResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<AccountResponseDTO> closeAccount(
            @Parameter(description = "Account ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }
}
