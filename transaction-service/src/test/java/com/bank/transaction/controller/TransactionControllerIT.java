package com.bank.transaction.controller;

import com.bank.transaction.client.AccountServiceClient;
import com.bank.transaction.client.AccountServiceClient.AccountDTO;
import com.bank.transaction.dto.DepositRequestDTO;
import com.bank.transaction.dto.WithdrawRequestDTO;
import com.bank.transaction.messaging.RabbitMQProducer;
import com.bank.transaction.repository.TransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class TransactionControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("transactiondb_test")
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
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private RabbitMQProducer rabbitMQProducer;

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
    }

    @Test
    void deposit_success() throws Exception {
        AccountDTO account = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("1000.00"), "ACTIVE");
        when(accountServiceClient.getAccountById(1L)).thenReturn(account);
        when(accountServiceClient.updateBalance(eq(1L), any())).thenReturn(account);

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("500.00"));
        request.setDescription("Test deposit");

        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceAccountId").value(1))
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void deposit_accountNotFound_returns404() throws Exception {
        when(accountServiceClient.getAccountById(99L)).thenReturn(null);

        DepositRequestDTO request = new DepositRequestDTO();
        request.setAccountId(99L);
        request.setAmount(new BigDecimal("100.00"));

        mockMvc.perform(post("/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdraw_success() throws Exception {
        AccountDTO account = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("1000.00"), "ACTIVE");
        when(accountServiceClient.getAccountById(1L)).thenReturn(account);
        when(accountServiceClient.updateBalance(eq(1L), any())).thenReturn(account);

        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void withdraw_insufficientFunds_returns422() throws Exception {
        AccountDTO account = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("100.00"), "ACTIVE");
        when(accountServiceClient.getAccountById(1L)).thenReturn(account);

        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("9999.00"));

        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/transactions/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByAccountId_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/transactions/account/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
