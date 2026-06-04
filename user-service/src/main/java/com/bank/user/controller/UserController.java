package com.bank.user.controller;

import com.bank.user.dto.UpdateUserRequestDTO;
import com.bank.user.dto.UserResponseDTO;
import com.bank.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management API")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get user by ID",
               description = "Returns user details for the given ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Get user by username",
               description = "Returns user details for the given username.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @Parameter(description = "Username", required = true, example = "jdoe")
            @PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    @Operation(summary = "Get all users",
               description = "Returns a list of all registered users.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Update user",
               description = "Updates an existing user's details. All fields are optional.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User updated successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email already in use", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id,
            @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Delete user",
               description = "Permanently deletes a user from the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
