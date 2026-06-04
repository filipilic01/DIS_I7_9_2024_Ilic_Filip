package com.bank.notification.service;

import com.bank.notification.dto.NotificationResponseDTO;
import com.bank.notification.exception.NotificationNotFoundException;
import com.bank.notification.messaging.LoanEventDTO;
import com.bank.notification.messaging.TransactionEventDTO;
import com.bank.notification.repository.NotificationRepository;
import com.bank.notification.repository.model.NotificationEntity;
import com.bank.notification.repository.model.NotificationEntity.NotificationChannel;
import com.bank.notification.repository.model.NotificationEntity.NotificationStatus;
import com.bank.notification.repository.model.NotificationEntity.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponseDTO processTransactionEvent(TransactionEventDTO event) {
        String title;
        String message;

        switch (event.getType()) {
            case "DEPOSIT" -> {
                title = "Deposit Successful";
                message = String.format("Deposit of %.2f completed on account %d",
                        event.getAmount(), event.getSourceAccountId());
            }
            case "WITHDRAWAL" -> {
                title = "Withdrawal Successful";
                message = String.format("Withdrawal of %.2f from account %d completed",
                        event.getAmount(), event.getSourceAccountId());
            }
            case "TRANSFER" -> {
                title = "Transfer Successful";
                message = String.format("Transfer of %.2f from account %d to account %d completed",
                        event.getAmount(), event.getSourceAccountId(), event.getTargetAccountId());
            }
            default -> {
                title = "Transaction Update";
                message = String.format("Transaction %d status: %s", event.getTransactionId(), event.getStatus());
            }
        }

        NotificationEntity notification = NotificationEntity.builder()
                .userId(event.getSourceAccountId())
                .type(NotificationType.TRANSACTION)
                .channel(NotificationChannel.EMAIL)
                .title(title)
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);
        log.info("Processed transaction notification: id={}, transactionId={}, type={}",
                saved.getId(), event.getTransactionId(), event.getType());
        return toResponse(saved);
    }

    @Transactional
    public NotificationResponseDTO processLoanEvent(LoanEventDTO event) {
        String title;
        String message;

        switch (event.getEventType()) {
            case "CREATED" -> {
                title = "Loan Application Received";
                message = String.format("Your loan application for %.2f has been received and is under review",
                        event.getAmount());
            }
            case "APPROVED" -> {
                title = "Loan Approved";
                message = String.format("Your loan of %.2f has been approved and funds have been disbursed to your account",
                        event.getAmount());
            }
            case "REJECTED" -> {
                title = "Loan Application Rejected";
                message = String.format("Your loan application for %.2f has been rejected",
                        event.getAmount());
            }
            case "PAYMENT_MADE" -> {
                title = "Loan Payment Recorded";
                message = String.format("Your loan payment has been recorded. Loan status: %s",
                        event.getLoanStatus());
            }
            case "PAID_OFF" -> {
                title = "Loan Fully Repaid";
                message = String.format("Congratulations! Your loan of %.2f has been fully repaid",
                        event.getAmount());
            }
            default -> {
                title = "Loan Update";
                message = String.format("Your loan %d status: %s", event.getLoanId(), event.getLoanStatus());
            }
        }

        NotificationEntity notification = NotificationEntity.builder()
                .userId(event.getUserId())
                .type(NotificationType.LOAN)
                .channel(NotificationChannel.EMAIL)
                .title(title)
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        NotificationEntity saved = notificationRepository.save(notification);
        log.info("Processed loan notification: id={}, loanId={}, eventType={}",
                saved.getId(), event.getLoanId(), event.getEventType());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public NotificationResponseDTO getById(Long id) {
        return toResponse(notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private NotificationResponseDTO toResponse(NotificationEntity n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .channel(n.getChannel())
                .title(n.getTitle())
                .message(n.getMessage())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}
