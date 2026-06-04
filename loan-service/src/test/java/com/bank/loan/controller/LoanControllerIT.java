package com.bank.loan.controller;

import com.bank.loan.client.AccountServiceClient;
import com.bank.loan.client.AccountServiceClient.AccountDTO;
import com.bank.loan.dto.CreateLoanRequestDTO;
import com.bank.loan.messaging.RabbitMQProducer;
import com.bank.loan.repository.LoanPaymentRepository;
import com.bank.loan.repository.LoanRepository;
import com.bank.loan.repository.model.LoanEntity.LoanType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class LoanControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("loandb_test")
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
    private LoanRepository loanRepository;

    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    @MockBean
    private AccountServiceClient accountServiceClient;

    @MockBean
    private RabbitMQProducer rabbitMQProducer;

    @AfterEach
    void tearDown() {
        loanPaymentRepository.deleteAll();
        loanRepository.deleteAll();
    }

    @Test
    void createLoan_success() throws Exception {
        AccountDTO account = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("50000.00"), "ACTIVE");
        when(accountServiceClient.getAccountById(1L)).thenReturn(account);

        CreateLoanRequestDTO request = new CreateLoanRequestDTO();
        request.setUserId(10L);
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("10000.00"));
        request.setInterestRate(new BigDecimal("5.5"));
        request.setTermMonths(24);
        request.setType(LoanType.PERSONAL);
        request.setPurpose("Home renovation");

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.amount").value(10000.00))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("PERSONAL"))
                .andExpect(jsonPath("$.monthlyInstallment").isNumber());
    }

    @Test
    void createLoan_accountNotFound_returns404() throws Exception {
        when(accountServiceClient.getAccountById(99L)).thenReturn(null);

        CreateLoanRequestDTO request = new CreateLoanRequestDTO();
        request.setUserId(10L);
        request.setAccountId(99L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setInterestRate(new BigDecimal("5.0"));
        request.setTermMonths(12);
        request.setType(LoanType.PERSONAL);

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLoan_invalidRequest_returns400() throws Exception {
        CreateLoanRequestDTO request = new CreateLoanRequestDTO();
        request.setUserId(10L);

        mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveLoan_success() throws Exception {
        AccountDTO account = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("50000.00"), "ACTIVE");
        when(accountServiceClient.getAccountById(1L)).thenReturn(account);
        when(accountServiceClient.updateBalance(eq(1L), any())).thenReturn(account);

        CreateLoanRequestDTO createRequest = new CreateLoanRequestDTO();
        createRequest.setUserId(10L);
        createRequest.setAccountId(1L);
        createRequest.setAmount(new BigDecimal("5000.00"));
        createRequest.setInterestRate(new BigDecimal("6.0"));
        createRequest.setTermMonths(12);
        createRequest.setType(LoanType.PERSONAL);

        String createResponse = mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long loanId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/loans/" + loanId + "/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.startDate").isNotEmpty())
                .andExpect(jsonPath("$.endDate").isNotEmpty());
    }

    @Test
    void approveLoan_notFound_returns404() throws Exception {
        mockMvc.perform(put("/loans/999/approve"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectLoan_success() throws Exception {
        AccountDTO account = new AccountDTO(1L, "ACC-20240101-000001", 10L,
                new BigDecimal("50000.00"), "ACTIVE");
        when(accountServiceClient.getAccountById(1L)).thenReturn(account);

        CreateLoanRequestDTO createRequest = new CreateLoanRequestDTO();
        createRequest.setUserId(10L);
        createRequest.setAccountId(1L);
        createRequest.setAmount(new BigDecimal("3000.00"));
        createRequest.setInterestRate(new BigDecimal("7.0"));
        createRequest.setTermMonths(6);
        createRequest.setType(LoanType.PERSONAL);

        String createResponse = mockMvc.perform(post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long loanId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/loans/" + loanId + "/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/loans/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByUserId_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/loans/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
