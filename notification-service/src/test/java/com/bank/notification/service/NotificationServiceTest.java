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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationEntity sampleNotification;

    @BeforeEach
    void setUp() {
        sampleNotification = NotificationEntity.builder()
                .id(1L)
                .userId(10L)
                .type(NotificationType.TRANSACTION)
                .channel(NotificationChannel.EMAIL)
                .title("Deposit Successful")
                .message("Deposit of 500.00 completed on account 1")
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void processTransactionEvent_deposit_createsNotification() {
        TransactionEventDTO event = new TransactionEventDTO(
                1L, 1L, null, new BigDecimal("500.00"), "DEPOSIT", "COMPLETED", LocalDateTime.now());

        when(notificationRepository.save(any())).thenReturn(sampleNotification);

        NotificationResponseDTO result = notificationService.processTransactionEvent(event);

        assertThat(result.getType()).isEqualTo(NotificationType.TRANSACTION);
        assertThat(result.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(notificationRepository).save(any(NotificationEntity.class));
    }

    @Test
    void processTransactionEvent_withdrawal_createsNotification() {
        TransactionEventDTO event = new TransactionEventDTO(
                2L, 1L, null, new BigDecimal("200.00"), "WITHDRAWAL", "COMPLETED", LocalDateTime.now());

        NotificationEntity withdrawalNotification = NotificationEntity.builder()
                .id(2L).userId(1L).type(NotificationType.TRANSACTION)
                .channel(NotificationChannel.EMAIL).title("Withdrawal Successful")
                .message("Withdrawal of 200.00 from account 1 completed")
                .status(NotificationStatus.SENT).createdAt(LocalDateTime.now()).build();

        when(notificationRepository.save(any())).thenReturn(withdrawalNotification);

        NotificationResponseDTO result = notificationService.processTransactionEvent(event);

        assertThat(result.getTitle()).isEqualTo("Withdrawal Successful");
        assertThat(result.getType()).isEqualTo(NotificationType.TRANSACTION);
    }

    @Test
    void processTransactionEvent_transfer_createsNotification() {
        TransactionEventDTO event = new TransactionEventDTO(
                3L, 1L, 2L, new BigDecimal("1000.00"), "TRANSFER", "COMPLETED", LocalDateTime.now());

        NotificationEntity transferNotification = NotificationEntity.builder()
                .id(3L).userId(1L).type(NotificationType.TRANSACTION)
                .channel(NotificationChannel.EMAIL).title("Transfer Successful")
                .message("Transfer of 1000.00 from account 1 to account 2 completed")
                .status(NotificationStatus.SENT).createdAt(LocalDateTime.now()).build();

        when(notificationRepository.save(any())).thenReturn(transferNotification);

        NotificationResponseDTO result = notificationService.processTransactionEvent(event);

        assertThat(result.getTitle()).isEqualTo("Transfer Successful");
    }

    @Test
    void processLoanEvent_approved_createsNotification() {
        LoanEventDTO event = new LoanEventDTO(
                1L, 10L, 1L, new BigDecimal("10000.00"), "APPROVED", "ACTIVE", LocalDateTime.now());

        NotificationEntity loanNotification = NotificationEntity.builder()
                .id(4L).userId(10L).type(NotificationType.LOAN)
                .channel(NotificationChannel.EMAIL).title("Loan Approved")
                .message("Your loan of 10000.00 has been approved and funds have been disbursed to your account")
                .status(NotificationStatus.SENT).createdAt(LocalDateTime.now()).build();

        when(notificationRepository.save(any())).thenReturn(loanNotification);

        NotificationResponseDTO result = notificationService.processLoanEvent(event);

        assertThat(result.getType()).isEqualTo(NotificationType.LOAN);
        assertThat(result.getTitle()).isEqualTo("Loan Approved");
        assertThat(result.getUserId()).isEqualTo(10L);
    }

    @Test
    void processLoanEvent_rejected_createsNotification() {
        LoanEventDTO event = new LoanEventDTO(
                2L, 10L, 1L, new BigDecimal("5000.00"), "REJECTED", "REJECTED", LocalDateTime.now());

        NotificationEntity rejectedNotification = NotificationEntity.builder()
                .id(5L).userId(10L).type(NotificationType.LOAN)
                .channel(NotificationChannel.EMAIL).title("Loan Application Rejected")
                .message("Your loan application for 5000.00 has been rejected")
                .status(NotificationStatus.SENT).createdAt(LocalDateTime.now()).build();

        when(notificationRepository.save(any())).thenReturn(rejectedNotification);

        NotificationResponseDTO result = notificationService.processLoanEvent(event);

        assertThat(result.getTitle()).isEqualTo("Loan Application Rejected");
    }

    @Test
    void processLoanEvent_paidOff_createsNotification() {
        LoanEventDTO event = new LoanEventDTO(
                3L, 10L, 1L, new BigDecimal("10000.00"), "PAID_OFF", "PAID_OFF", LocalDateTime.now());

        NotificationEntity paidOffNotification = NotificationEntity.builder()
                .id(6L).userId(10L).type(NotificationType.LOAN)
                .channel(NotificationChannel.EMAIL).title("Loan Fully Repaid")
                .message("Congratulations! Your loan of 10000.00 has been fully repaid")
                .status(NotificationStatus.SENT).createdAt(LocalDateTime.now()).build();

        when(notificationRepository.save(any())).thenReturn(paidOffNotification);

        NotificationResponseDTO result = notificationService.processLoanEvent(event);

        assertThat(result.getTitle()).isEqualTo("Loan Fully Repaid");
    }

    @Test
    void getById_found() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(sampleNotification));

        NotificationResponseDTO result = notificationService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Deposit Successful");
    }

    @Test
    void getById_notFound_throwsException() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getById(99L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    @Test
    void getByUserId_returnsList() {
        when(notificationRepository.findByUserId(10L)).thenReturn(List.of(sampleNotification));

        List<NotificationResponseDTO> result = notificationService.getByUserId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(10L);
    }
}
