package com.bank.notification.controller;

import com.bank.notification.dto.NotificationResponseDTO;
import com.bank.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification query API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notification by ID",
               description = "Returns details of a specific notification.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notification found",
                content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getById(
            @Parameter(description = "Notification ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getById(id));
    }

    @Operation(summary = "Get all notifications for a user",
               description = "Returns all notifications sent to the specified user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully",
                content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> getByUserId(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getByUserId(userId));
    }
}
