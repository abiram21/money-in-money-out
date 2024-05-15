package com.transaction.modules.base.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transaction.modules.base.controller.response.PingResponse;
import com.transaction.modules.base.service.BaseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BaseService baseService;


    @Test
    void ping() throws Exception {
        LocalDateTime expectedDateTime = LocalDateTime.of(2024, 5, 1, 12, 0, 0);
        PingResponse mockResponse = new PingResponse(expectedDateTime);
        when(baseService.ping()).thenReturn(mockResponse);
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = new ObjectMapper().readTree(response);

        Assertions.assertNotNull(jsonNode.get("serverTime").asText());
        Assertions.assertEquals(jsonNode.get("serverTime").asText(), "2024-05-01T12:00:00");

    }

    @Test
    void pingError() throws Exception {
        when(baseService.ping()).thenThrow(new RuntimeException("Test Exception"));
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        // Convert the error response content to a JSON node
        String response = mvcResult.getResponse().getContentAsString();
        JsonNode jsonNode = new ObjectMapper().readTree(response);
        System.out.println(jsonNode);
        Assertions.assertTrue(jsonNode.has("code"));
        Assertions.assertTrue(jsonNode.has("message"));
        Assertions.assertEquals(jsonNode.get("message").asText(), "Internal Server Error.");
        Assertions.assertEquals(jsonNode.get("code").asText(), "500");

    }
}