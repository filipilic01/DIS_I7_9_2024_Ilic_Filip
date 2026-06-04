package com.bank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login request payload")
public class AuthRequestDTO {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "jdoe")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "secret123")
    private String password;
}
