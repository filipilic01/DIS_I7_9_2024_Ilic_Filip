package com.bank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Authentication response containing JWT token")
public class AuthResponseDTO {

    @Schema(description = "JWT Bearer token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Authenticated user ID", example = "1")
    private Long userId;

    @Schema(description = "Authenticated username", example = "jdoe")
    private String username;

    @Schema(description = "Token expiration timestamp", example = "2024-01-16T10:30:00")
    private LocalDateTime expiresAt;
}
