package com.transaction.modules.transaction.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.modules.transaction.enums.TransactionEventStatus;
import com.transaction.modules.transaction.enums.TransactionType;
import com.transaction.modules.transaction.job.TransactionUpdateJob;
import com.transaction.modules.account.entity.Account;
import com.transaction.modules.transaction.entity.Transaction;
import com.transaction.modules.transaction.entity.TransactionEvent;
import com.transaction.modules.account.repository.AccountRepository;
import com.transaction.modules.transaction.repository.TransactionEventRepository;
import com.transaction.modules.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionEventRepository transactionEventRepository;

    @Autowired
    private TransactionUpdateJob transactionUpdateJob;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void load() throws Exception {
        String userId = "2226e2f9-ih09-46a8-958f-d659880asdfD";
        String messageId = "55210c62-e480-asdf-bc1b-e991ac67FSAC";

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals(userId, jsonNode.get("userId").asText());
        assertEquals(messageId, jsonNode.get("messageId").asText());
        assertEquals("100.0", jsonNode.get("balance").get("amount").asText());
        assertEquals("$", jsonNode.get("balance").get("currency").asText());
        assertEquals("CREDIT", jsonNode.get("balance").get("debitOrCredit").asText());

        Optional<TransactionEvent> optionalTransactionEvent = transactionEventRepository.findById(messageId);
        if (optionalTransactionEvent.isEmpty()) {
            fail("TransactionEvent is not inserted");
        }
        TransactionEvent transactionEvent = optionalTransactionEvent.get();
        assertEquals(transactionEvent.getUserId(), userId);
        assertEquals(transactionEvent.getAmount(), 100.0);
        assertEquals(transactionEvent.getStatus(), TransactionEventStatus.PENDING);
        assertEquals(transactionEvent.getCurrency(), "$");
        assertEquals(transactionEvent.getRunningBalance(), 100.0);
        assertEquals(transactionEvent.getType(), TransactionType.CREDIT);
        assertNotNull(transactionEvent.getCreatedAt());

        await().atMost(5, SECONDS).untilAsserted(() -> {
            Account account = accountRepository.findByUserId(userId);
            assertNotNull(account);
            assertEquals(account.getBalance(), 100.0);
            assertEquals(account.getUserId(), userId);

            List<Transaction> transactionList = transactionRepository.findByUserId(userId);
            assertEquals(transactionList.size(), 1);
            Transaction transaction = transactionList.get(0);
            assertNotNull(transaction);
            assertEquals(transaction.getAmount(), 100.0);
            assertEquals(transaction.getType(), TransactionType.CREDIT);
            assertEquals(transaction.getCurrency(), "$");
            assertEquals(transaction.getUserId(), userId);
            assertNotNull(transaction.getId());
            assertNotNull(transaction.getCreatedAt());
        });
    }

    @Test
    void loadErrorNotSameMessageId() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAE\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Message ids are not matched", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void loadMessageId() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Message Id cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void loadErrorUserId() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("User Id cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void loadErrorAmount() throws Exception {

        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\"\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Transaction amount is required.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void loadErrorTransactionAmount() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Amount cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void loadErrorTransactionType() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\" }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Debit or credit type is required.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void loadErrorTransactionCurrency() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/load/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Currency cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorize() throws Exception {
        String userId = "2226e2f9-ih09-46a8-958f-d659880asdfD";
        String messageId = "55210c62-e480-asdf-bc1b-e991ac67FSAD";

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAD\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"50\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"DEBIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals(userId, jsonNode.get("userId").asText());
        assertEquals(messageId, jsonNode.get("messageId").asText());
        assertEquals("50.0", jsonNode.get("balance").get("amount").asText());
        assertEquals("$", jsonNode.get("balance").get("currency").asText());
        assertEquals("DEBIT", jsonNode.get("balance").get("debitOrCredit").asText());

        Optional<TransactionEvent> optionalTransactionEvent = transactionEventRepository.findById(messageId);
        if (optionalTransactionEvent.isEmpty()) {
            fail("TransactionEvent is not inserted");
        }
        TransactionEvent transactionEvent = optionalTransactionEvent.get();
        assertEquals(transactionEvent.getUserId(), userId);
        assertEquals(transactionEvent.getAmount(), 50);
        assertEquals(transactionEvent.getCurrency(), "$");
        assertEquals(transactionEvent.getRunningBalance(), 50.0);
        assertEquals(transactionEvent.getType(), TransactionType.DEBIT);

        await().atMost(5, SECONDS).untilAsserted(() -> {
            Account account = accountRepository.findByUserId(userId);
            assertNotNull(account);
            assertEquals(account.getBalance(), 50.0);

            List<Transaction> transactionList = transactionRepository.findByUserId(userId);
            assertEquals(transactionList.size(), 2);
            Optional<Transaction> debitTransaction = transactionList.stream()
                    .filter(transaction -> transaction.getType() == TransactionType.DEBIT)
                    .findFirst();
            assertTrue(debitTransaction.isPresent());

            Transaction transaction = debitTransaction.get();
            assertEquals(transaction.getAmount(), 50.0);
            assertEquals(transaction.getType(), TransactionType.DEBIT);
            assertEquals(transaction.getCurrency(), "$");
            assertEquals(transaction.getUserId(), userId);
            assertNotNull(transaction.getId());
            assertNotNull(transaction.getCreatedAt());
        });
    }

    @Test
    void authorizeInsufficientBalance() throws Exception {
        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAE\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"500\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"DEBIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Insufficient balance", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeUniqueMessageId() throws Exception {
        String messageId = "55210c62-e480-asdf-bc1b-e991ac67FSAC";

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"50\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"DEBIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Duplicate message id " + messageId, jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeMessageId() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Message Id cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeErrorUserId() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("User Id cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeErrorAmount() throws Exception {

        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\"\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Transaction amount is required.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeErrorTransactionAmount() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"currency\": \"$\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Amount cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeErrorTransactionType() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"currency\": \"$\" }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Debit or credit type is required.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }

    @Test
    void authorizeErrorTransactionCurrency() throws Exception {

        // Prepare request data
        String requestJson = "{\n \"messageId\": \"55210c62-e480-asdf-bc1b-e991ac67FSAC\",\n \"userId\": \"2226e2f9-ih09-46a8-958f-d659880asdfD\",\n\"transactionAmount\": {\n \"amount\": \"100\",\n \"debitOrCredit\": \"CREDIT\"\n }\n}";

        // Send put request to the endpoint
        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transaction/authorize/55210c62-e480-asdf-bc1b-e991ac67FSAC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        assertEquals("Currency cannot be empty.", jsonNode.get("message").asText());
        assertEquals("400", jsonNode.get("code").asText());
    }
}