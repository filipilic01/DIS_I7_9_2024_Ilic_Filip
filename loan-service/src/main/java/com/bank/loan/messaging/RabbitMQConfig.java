package com.bank.loan.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "banking.events";
    public static final String QUEUE = "loan.notifications";
    public static final String ROUTING_KEY = "loan.*";

    @Bean
    public TopicExchange bankingEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue loanNotificationsQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding loanBinding(Queue loanNotificationsQueue, TopicExchange bankingEventsExchange) {
        return BindingBuilder.bind(loanNotificationsQueue)
                .to(bankingEventsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter(objectMapper));
        return template;
    }
}
