package com.bank.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating an existing user. All fields are optional.")
public class UpdateUserRequestDTO {

    @Schema(description = "Updated first name", example = "Jane")
    private String firstName;

    @Schema(description = "Updated last name", example = "Doe")
    private String lastName;

    @Schema(description = "Updated phone number", example = "+381609876543")
    private String phoneNumber;

    @Schema(description = "Updated email address", example = "jane.doe@example.com")
    private String email;
}
