package com.mtmx.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.service.SwiftMessageService;
import com.mtmx.service.ConversionService;
import com.mtmx.web.dto.SwiftMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verify;

@WebMvcTest(SwiftMessageController.class)
class SwiftMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SwiftMessageService swiftMessageService;

    @MockBean
    private ConversionService conversionService;

    @Autowired
    private ObjectMapper objectMapper;

    private SwiftMessageDto swiftMessageDto;

    @BeforeEach
    void setUp() {
        swiftMessageDto = new SwiftMessageDto();
        swiftMessageDto.setId(1L);
        swiftMessageDto.setMessageType(MessageType.MT103);
        swiftMessageDto.setSenderBic("BANKBEBB");
        swiftMessageDto.setReceiverBic("BANKDEFF");
        swiftMessageDto.setAmount(BigDecimal.valueOf(5000.00));
        swiftMessageDto.setCurrency("EUR");
        swiftMessageDto.setValueDate(LocalDate.of(2025, 6, 22));
        swiftMessageDto.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF123-}");
        swiftMessageDto.setGeneratedMxMessage("<?xml version=\"1.0\"?><Document>...</Document>");
    }

    @Test
    void getAllMessages_ShouldReturnPagedMessages() throws Exception {
        // Given
        Page<SwiftMessageDto> messagePage = new PageImpl<>(Arrays.asList(swiftMessageDto));
        when(swiftMessageService.findAll(any())).thenReturn(messagePage);

        // When & Then
        mockMvc.perform(get("/api/swift-messages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].messageType").value("MT103"))
                .andExpect(jsonPath("$.data.content[0].senderBic").value("BANKBEBB"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getMessageById_WithValidId_ShouldReturnMessage() throws Exception {
        // Given
        when(swiftMessageService.findOne(1L)).thenReturn(Optional.of(swiftMessageDto));

        // When & Then
        mockMvc.perform(get("/api/swift-messages/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.messageType").value("MT103"))
                .andExpect(jsonPath("$.data.senderBic").value("BANKBEBB"));
    }

    @Test
    void getMessageById_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(swiftMessageService.findOne(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/swift-messages/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createMessage_WithValidData_ShouldReturnCreatedMessage() throws Exception {
        // Given
        when(swiftMessageService.save(any(SwiftMessageDto.class))).thenReturn(swiftMessageDto);

        // When & Then
        mockMvc.perform(post("/api/swift-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(swiftMessageDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.messageType").value("MT103"));
    }

    @Test
    void updateMessage_WithValidData_ShouldReturnUpdatedMessage() throws Exception {
        // Given
        when(swiftMessageService.save(any(SwiftMessageDto.class))).thenReturn(swiftMessageDto);

        // When & Then
        mockMvc.perform(put("/api/swift-messages/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(swiftMessageDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void deleteMessage_WithValidId_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/swift-messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getMessagesByType_ShouldReturnFilteredMessages() throws Exception {
        // Given
        Page<SwiftMessageDto> messagePage = new PageImpl<>(Arrays.asList(swiftMessageDto));
        when(swiftMessageService.findByMessageType("MT103", PageRequest.of(0, 20))).thenReturn(messagePage);

        // When & Then
        mockMvc.perform(get("/api/swift-messages/type/MT103"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].messageType").value("MT103"));
    }

    @Test
    void convertMtToMx_WithValidData_ShouldReturnConvertedMessage() throws Exception {
        // Given
        String newMtMessage = "{1:F01BANKBEBB0000000000}{2:I202BANKDEFFN}{4::20:REF456-}";
        SwiftMessageDto convertedDto = new SwiftMessageDto();
        convertedDto.setId(1L);
        convertedDto.setMessageType(MessageType.MT202);
        convertedDto.setSenderBic("BANKBEBB");
        convertedDto.setReceiverBic("BANKDEFF");
        convertedDto.setRawMtMessage(newMtMessage);

        when(swiftMessageService.convertMtToMx(anyLong(), any(String.class))).thenReturn(convertedDto);

        // When & Then
        mockMvc.perform(post("/api/swift-messages/1/convert")
                .contentType(MediaType.TEXT_PLAIN)
                .content(newMtMessage))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void createMessage_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid DTO without required fields
        SwiftMessageDto invalidDto = new SwiftMessageDto();

        // When & Then
        mockMvc.perform(post("/api/swift-messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllMessages_WithPagination_ShouldReturnCorrectPage() throws Exception {
        // Given
        Page<SwiftMessageDto> messagePage = new PageImpl<>(Arrays.asList(swiftMessageDto), PageRequest.of(1, 5), 10);
        when(swiftMessageService.findAll(any())).thenReturn(messagePage);

        // When & Then
        mockMvc.perform(get("/api/swift-messages?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.totalElements").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.size").value(5));
    }

    @Test
    void getAllMessages_WithSorting_ShouldReturnSortedResults() throws Exception {
        // Given
        Page<SwiftMessageDto> messagePage = new PageImpl<>(Arrays.asList(swiftMessageDto));
        when(swiftMessageService.findAll(any())).thenReturn(messagePage);

        // When & Then
        mockMvc.perform(get("/api/swift-messages?sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)));
    }

    @Test
    void testConvertMxToMt() throws Exception {
        // Given
        Long messageId = 1L;
        SwiftMessageDto convertedDto = new SwiftMessageDto();
        convertedDto.setId(messageId);
        convertedDto.setRawMtMessage(
                "{1:F01BANKXXXXAXXX0000000000}{2:I103BANKXXXXBXXXN}{4:\n:20:CONVERTED001\n:23B:CRED\n-}");

        when(swiftMessageService.convertMxToMt(messageId)).thenReturn(convertedDto);

        // When & Then
        mockMvc.perform(post("/api/swift-messages/{id}/convert-mx-to-mt", messageId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("MX message successfully converted to MT format"))
                .andExpect(jsonPath("$.data.id").value(messageId))
                .andExpect(jsonPath("$.data.rawMtMessage").exists());

        verify(swiftMessageService).convertMxToMt(messageId);
    }

    @Test
    void testConvertMxToMt_NotFound() throws Exception {
        // Given
        Long messageId = 999L;
        when(swiftMessageService.convertMxToMt(messageId))
                .thenThrow(new RuntimeException("Message not found with id: " + messageId));

        // When & Then
        mockMvc.perform(post("/api/swift-messages/{id}/convert-mx-to-mt", messageId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Message not found: Message not found with id: " + messageId));

        verify(swiftMessageService).convertMxToMt(messageId);
    }
}
