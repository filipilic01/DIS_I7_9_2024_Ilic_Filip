package com.bank.notification.messaging;

import com.bank.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_QUEUE)
    public void handleTransactionEvent(TransactionEventDTO event) {
        log.info("Received transaction event: transactionId={}, type={}", event.getTransactionId(), event.getType());
        notificationService.processTransactionEvent(event);
    }

    @RabbitListener(queues = RabbitMQConfig.LOAN_QUEUE)
    public void handleLoanEvent(LoanEventDTO event) {
        log.info("Received loan event: loanId={}, eventType={}", event.getLoanId(), event.getEventType());
        notificationService.processLoanEvent(event);
    }
}
