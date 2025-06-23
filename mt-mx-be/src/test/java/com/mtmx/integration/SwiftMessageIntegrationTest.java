package com.mtmx.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.repository.SwiftMessageRepository;
import com.mtmx.web.dto.SwiftMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class SwiftMessageIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SwiftMessageRepository swiftMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        swiftMessageRepository.deleteAll();
    }

    @Test
    void createAndRetrieveMessage_ShouldWorkEndToEnd() throws Exception {
        // Given
        SwiftMessageDto dto = new SwiftMessageDto();
        dto.setMessageType(MessageType.MT103);
        dto.setSenderBic("BANKBEBB");
        dto.setReceiverBic("BANKDEFF");
        dto.setAmount(BigDecimal.valueOf(5000.00));
        dto.setCurrency("EUR");
        dto.setValueDate(LocalDate.of(2025, 6, 22));
        dto.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF123SINGLE-}");

        // When - Create message
        String response = mockMvc.perform(post("/api/swift-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.messageType").value("MT103"))
                .andExpect(jsonPath("$.data.senderBic").value("BANKBEBB"))
                .andExpect(jsonPath("$.data.generatedMxMessage").exists())
                .andReturn().getResponse().getContentAsString();

        // Parse StandardResponse to get the data
        var responseMap = objectMapper.readValue(response, java.util.Map.class);
        var dataMap = (java.util.Map) responseMap.get("data");
        Long createdId = Long.valueOf(dataMap.get("id").toString());

        // Then - Retrieve message
        mockMvc.perform(get("/api/swift-messages/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdId))
                .andExpect(jsonPath("$.data.messageType").value("MT103"))
                .andExpect(jsonPath("$.data.senderBic").value("BANKBEBB"));
    }

    @Test
    void getAllMessages_WithMultipleMessages_ShouldReturnPagedResults() throws Exception {
        // Given - Create test data
        SwiftMessage message1 = createTestMessage(MessageType.MT103, "BANKBEBB", "BANKDEFF");
        SwiftMessage message2 = createTestMessage(MessageType.MT102, "BANKUS33XXX", "BANKGB2LXXX");
        swiftMessageRepository.save(message1);
        swiftMessageRepository.save(message2);

        // When & Then
        mockMvc.perform(get("/api/swift-messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void getMessagesByType_ShouldFilterCorrectly() throws Exception {
        // Given
        SwiftMessage mt103Message = createTestMessage(MessageType.MT103, "BANKBEBB", "BANKDEFF");
        SwiftMessage mt102Message = createTestMessage(MessageType.MT102, "BANKUS33XXX", "BANKGB2LXXX");
        swiftMessageRepository.save(mt103Message);
        swiftMessageRepository.save(mt102Message);

        // When & Then
        mockMvc.perform(get("/api/swift-messages/type/MT103"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].messageType").value("MT103"));
    }

    @Test
    void convertMtToMx_ShouldUpdateMessage() throws Exception {
        // Given
        SwiftMessage message = createTestMessage(MessageType.MT103, "BANKBEBB", "BANKDEFF");
        SwiftMessage saved = swiftMessageRepository.save(message);
        String newMtMessage = "{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF456NEW-}";

        // When & Then
        mockMvc.perform(post("/api/swift-messages/" + saved.getId() + "/convert")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(newMtMessage))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(saved.getId()))
                .andExpect(jsonPath("$.data.rawMtMessage").value(newMtMessage))
                .andExpect(jsonPath("$.data.generatedMxMessage").exists());
    }

    @Test
    void updateMessage_ShouldModifyExistingMessage() throws Exception {
        // Given
        SwiftMessage message = createTestMessage(MessageType.MT103, "BANKBEBB", "BANKDEFF");
        SwiftMessage saved = swiftMessageRepository.save(message);

        SwiftMessageDto updateDto = new SwiftMessageDto();
        updateDto.setId(saved.getId());
        updateDto.setMessageType(MessageType.MT103);
        updateDto.setSenderBic("NEWBANKXXXX");
        updateDto.setReceiverBic("BANKDEFF");
        updateDto.setAmount(BigDecimal.valueOf(7500.00));
        updateDto.setCurrency("USD");
        updateDto.setValueDate(LocalDate.of(2025, 6, 25));
        updateDto.setRawMtMessage("{1:F01NEWBANKXXXX0000000000}{2:I103BANKDEFFN}{4::20:REFUPDATE-}");

        // When & Then
        mockMvc.perform(put("/api/swift-messages/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.senderBic").value("NEWBANKXXXX"))
                .andExpect(jsonPath("$.data.amount").value(7500.00))
                .andExpect(jsonPath("$.data.currency").value("USD"));
    }

    @Test
    void deleteMessage_ShouldRemoveMessage() throws Exception {
        // Given
        SwiftMessage message = createTestMessage(MessageType.MT103, "BANKBEBB", "BANKDEFF");
        SwiftMessage saved = swiftMessageRepository.save(message);

        // When
        mockMvc.perform(delete("/api/swift-messages/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Then
        mockMvc.perform(get("/api/swift-messages/" + saved.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void messageTypeDetection_ShouldWorkForAllTypes() throws Exception {
        // Test MT102
        testMessageTypeDetection("MT102", "{1:F01BANKBEBB0000000000}{2:I102BANKDEFFN}{4::20:REF102-}");
        
        // Test MT103
        testMessageTypeDetection("MT103", "{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF103-}");
        
        // Test MT202
        testMessageTypeDetection("MT202", "{1:F01BANKBEBB0000000000}{2:I202BANKDEFFN}{4::20:REF202-}");
        
        // Test MT203
        testMessageTypeDetection("MT203", "{1:F01BANKBEBB0000000000}{2:I203BANKDEFFN}{4::20:REF203-}");
        
        // Test MT202COV
        testMessageTypeDetection("MT202COV", "{1:F01BANKBEBB0000000000}{2:I202COVBANKDEFFN}{4::20:REF202COV-}");
    }

    private void testMessageTypeDetection(String expectedType, String mtMessage) throws Exception {
        SwiftMessageDto dto = new SwiftMessageDto();
        dto.setSenderBic("BANKBEBB");
        dto.setReceiverBic("BANKDEFF");
        dto.setAmount(BigDecimal.valueOf(1000.00));
        dto.setCurrency("EUR");
        dto.setValueDate(LocalDate.of(2025, 6, 22));
        dto.setRawMtMessage(mtMessage);

        mockMvc.perform(post("/api/swift-messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.messageType").value(expectedType));
    }

    private SwiftMessage createTestMessage(MessageType messageType, String senderBic, String receiverBic) {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(messageType);
        message.setSenderBic(senderBic);
        message.setReceiverBic(receiverBic);
        message.setAmount(BigDecimal.valueOf(5000.00));
        message.setCurrency("EUR");
        message.setValueDate(LocalDate.of(2025, 6, 22));
        message.setRawMtMessage("{1:F01" + senderBic + "0000000000}{2:I103" + receiverBic + "N}{4::20:REF123-}");
        message.setGeneratedMxMessage("<?xml version=\"1.0\"?><Document>test</Document>");
        return message;
    }
}
