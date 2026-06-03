package com.bank.user.dto;

import com.bank.user.repository.model.UserEntity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "User response payload")
public class UserResponseDTO {

    @Schema(description = "Unique user identifier", example = "1")
    private Long id;

    @Schema(description = "Username", example = "jdoe")
    private String username;

    @Schema(description = "Email address", example = "jdoe@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Phone number", example = "+381601234567")
    private String phoneNumber;

    @Schema(description = "Account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Timestamp when the user was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update", example = "2024-06-01T08:00:00")
    private LocalDateTime updatedAt;
}
