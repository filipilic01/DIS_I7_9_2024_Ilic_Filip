package com.bank.notification.controller;

import com.bank.notification.repository.NotificationRepository;
import com.bank.notification.repository.model.NotificationEntity;
import com.bank.notification.repository.model.NotificationEntity.NotificationChannel;
import com.bank.notification.repository.model.NotificationEntity.NotificationStatus;
import com.bank.notification.repository.model.NotificationEntity.NotificationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class NotificationControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("notificationdb_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("eureka.client.register-with-eureka", () -> "false");
        registry.add("eureka.client.fetch-registry", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("spring.rabbitmq.listener.simple.auto-startup", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/notifications/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUserId_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_found_returns200() throws Exception {
        NotificationEntity saved = notificationRepository.save(NotificationEntity.builder()
                .userId(10L)
                .type(NotificationType.TRANSACTION)
                .channel(NotificationChannel.EMAIL)
                .title("Deposit Successful")
                .message("Deposit of 500.00 completed on account 1")
                .status(NotificationStatus.SENT)
                .build());

        mockMvc.perform(get("/notifications/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.title").value("Deposit Successful"))
                .andExpect(jsonPath("$.type").value("TRANSACTION"))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void getByUserId_withData_returnsList() throws Exception {
        notificationRepository.save(NotificationEntity.builder()
                .userId(10L)
                .type(NotificationType.LOAN)
                .channel(NotificationChannel.EMAIL)
                .title("Loan Approved")
                .message("Your loan has been approved")
                .status(NotificationStatus.SENT)
                .build());

        mockMvc.perform(get("/notifications/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(10))
                .andExpect(jsonPath("$[0].type").value("LOAN"));
    }
}
