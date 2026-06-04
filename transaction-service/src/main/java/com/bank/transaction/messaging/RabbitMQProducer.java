package com.bank.transaction.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendTransactionEvent(TransactionEvent event) {
        String routingKey = "transaction." + event.getType().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
        log.info("Published transaction event: transactionId={}, type={}, status={}",
                event.getTransactionId(), event.getType(), event.getStatus());
    }
}
