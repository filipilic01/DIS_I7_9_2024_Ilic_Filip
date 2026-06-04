package com.bank.loan.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendLoanEvent(LoanEvent event) {
        String routingKey = "loan." + event.getEventType().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
        log.info("Published loan event: loanId={}, eventType={}, status={}",
                event.getLoanId(), event.getEventType(), event.getLoanStatus());
    }
}
