package com.mtmx.web.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtmx.service.SwiftMessageService;
import com.mtmx.service.ConversionService;
import com.mtmx.web.controller.SwiftMessageController;
import com.mtmx.web.dto.SwiftMessageDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SwiftMessageController.class)
class GlobalExceptionHandlerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private SwiftMessageService swiftMessageService;

        @MockBean
        private ConversionService conversionService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void handleRuntimeException_ShouldReturnInternalServerError() throws Exception {
                // Given
                SwiftMessageDto dto = new SwiftMessageDto();
                dto.setSenderBic("BANKBEBB");
                dto.setReceiverBic("BANKDEFF");

                when(swiftMessageService.save(any(SwiftMessageDto.class)))
                                .thenThrow(new RuntimeException("Database connection failed"));

                // When & Then
                mockMvc.perform(post("/api/swift-messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.message").exists())
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void handleIllegalArgumentException_ShouldReturnBadRequest() throws Exception {
                // Given
                SwiftMessageDto dto = new SwiftMessageDto();
                dto.setSenderBic("INVALID");
                dto.setReceiverBic("BANKDEFF");

                when(swiftMessageService.save(any(SwiftMessageDto.class)))
                                .thenThrow(new IllegalArgumentException("Invalid BIC format"));

                // When & Then
                mockMvc.perform(post("/api/swift-messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.message").value("Invalid BIC format"))
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void handleValidationErrors_ShouldReturnBadRequestWithDetails() throws Exception {
                // Given - DTO with validation errors
                SwiftMessageDto dto = new SwiftMessageDto();
                // Missing required fields will trigger validation errors

                // When & Then
                mockMvc.perform(post("/api/swift-messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isBadRequest());
        }
}
