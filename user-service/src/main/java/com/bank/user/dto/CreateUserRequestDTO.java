package com.bank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a new user")
public class CreateUserRequestDTO {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Unique username", example = "jdoe", minLength = 3, maxLength = 50)
    private String username;

    @NotBlank
    @Email
    @Schema(description = "User email address", example = "jdoe@example.com")
    private String email;

    @NotBlank
    @Size(min = 6)
    @Schema(description = "Password (minimum 6 characters)", example = "secret123", minLength = 6)
    private String password;

    @NotBlank
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Phone number (optional)", example = "+381601234567")
    private String phoneNumber;
}
