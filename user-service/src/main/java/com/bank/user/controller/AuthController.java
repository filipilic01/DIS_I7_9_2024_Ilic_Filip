package com.bank.user.controller;

import com.bank.user.dto.AuthRequestDTO;
import com.bank.user.dto.AuthResponseDTO;
import com.bank.user.dto.CreateUserRequestDTO;
import com.bank.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration and login API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user",
               description = "Creates a new user account and returns a JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or username/email already taken", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody CreateUserRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login",
               description = "Authenticates user credentials and returns a JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
