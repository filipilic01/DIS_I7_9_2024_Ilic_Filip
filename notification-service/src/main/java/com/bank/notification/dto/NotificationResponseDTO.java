package com.bank.notification.dto;

import com.bank.notification.repository.model.NotificationEntity.NotificationChannel;
import com.bank.notification.repository.model.NotificationEntity.NotificationStatus;
import com.bank.notification.repository.model.NotificationEntity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Notification response payload")
public class NotificationResponseDTO {

    @Schema(description = "Unique notification identifier", example = "1")
    private Long id;

    @Schema(description = "ID of the user this notification belongs to", example = "1")
    private Long userId;

    @Schema(description = "Notification type", example = "TRANSACTION")
    private NotificationType type;

    @Schema(description = "Delivery channel", example = "EMAIL")
    private NotificationChannel channel;

    @Schema(description = "Notification title", example = "Deposit Successful")
    private String title;

    @Schema(description = "Notification message", example = "Deposit of 500.00 completed on account 1")
    private String message;

    @Schema(description = "Notification delivery status", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Timestamp when the notification was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the notification was sent", example = "2024-01-15T10:30:01")
    private LocalDateTime sentAt;
}
