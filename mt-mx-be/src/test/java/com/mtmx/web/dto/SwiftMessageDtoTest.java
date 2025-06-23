package com.mtmx.web.dto;

import com.mtmx.domain.enums.MessageType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftMessageDtoTest {

    @Test
    void swiftMessageDto_ShouldCreateWithAllFields() {
        // Given
        Long id = 1L;
        MessageType messageType = MessageType.MT103;
        String senderBic = "BANKBEBB0000";
        String receiverBic = "BANKDEFF0000";
        BigDecimal amount = new BigDecimal("1000.00");
        String currency = "EUR";
        LocalDate valueDate = LocalDate.of(2025, 6, 22);
        String rawMtMessage = "{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:TEST123-}";
        String generatedMxMessage = "<?xml version=\"1.0\"?><Document/>";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        // When
        SwiftMessageDto dto = new SwiftMessageDto();
        dto.setId(id);
        dto.setMessageType(messageType);
        dto.setSenderBic(senderBic);
        dto.setReceiverBic(receiverBic);
        dto.setAmount(amount);
        dto.setCurrency(currency);
        dto.setValueDate(valueDate);
        dto.setRawMtMessage(rawMtMessage);
        dto.setGeneratedMxMessage(generatedMxMessage);
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);

        // Then
        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getMessageType()).isEqualTo(messageType);
        assertThat(dto.getSenderBic()).isEqualTo(senderBic);
        assertThat(dto.getReceiverBic()).isEqualTo(receiverBic);
        assertThat(dto.getAmount()).isEqualTo(amount);
        assertThat(dto.getCurrency()).isEqualTo(currency);
        assertThat(dto.getValueDate()).isEqualTo(valueDate);
        assertThat(dto.getRawMtMessage()).isEqualTo(rawMtMessage);
        assertThat(dto.getGeneratedMxMessage()).isEqualTo(generatedMxMessage);
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
        assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void swiftMessageDto_ShouldHandleNullValues() {
        // When
        SwiftMessageDto dto = new SwiftMessageDto();

        // Then
        assertThat(dto.getId()).isNull();
        assertThat(dto.getMessageType()).isNull();
        assertThat(dto.getSenderBic()).isNull();
        assertThat(dto.getReceiverBic()).isNull();
        assertThat(dto.getAmount()).isNull();
        assertThat(dto.getCurrency()).isNull();
        assertThat(dto.getValueDate()).isNull();
        assertThat(dto.getRawMtMessage()).isNull();
        assertThat(dto.getGeneratedMxMessage()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    @Test
    void swiftMessageDto_ToString_ShouldContainAllFields() {
        // Given
        SwiftMessageDto dto = new SwiftMessageDto();
        dto.setId(1L);
        dto.setMessageType(MessageType.MT103);
        dto.setSenderBic("BANKBEBB0000");
        dto.setReceiverBic("BANKDEFF0000");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("messageType=MT103");
        assertThat(toString).contains("senderBic=BANKBEBB0000");
        assertThat(toString).contains("receiverBic=BANKDEFF0000");
    }

    @Test
    void swiftMessageDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        // Given
        SwiftMessageDto dto1 = new SwiftMessageDto();
        dto1.setId(1L);
        dto1.setMessageType(MessageType.MT103);
        dto1.setSenderBic("BANKBEBB0000");

        SwiftMessageDto dto2 = new SwiftMessageDto();
        dto2.setId(1L);
        dto2.setMessageType(MessageType.MT103);
        dto2.setSenderBic("BANKBEBB0000");

        SwiftMessageDto dto3 = new SwiftMessageDto();
        dto3.setId(2L);
        dto3.setMessageType(MessageType.MT102);
        dto3.setSenderBic("BANKDEFF0000");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        assertThat(dto1.hashCode()).isNotEqualTo(dto3.hashCode());
    }

    @Test
    void swiftMessageDto_WithBuilder_ShouldCreateCorrectly() {
        // Given
        BigDecimal amount = new BigDecimal("5000.00");
        LocalDate valueDate = LocalDate.of(2025, 12, 31);

        // When
        SwiftMessageDto dto = new SwiftMessageDto();
        dto.setMessageType(MessageType.MT202);
        dto.setSenderBic("TESTBIC1");
        dto.setReceiverBic("TESTBIC2");
        dto.setAmount(amount);
        dto.setCurrency("USD");
        dto.setValueDate(valueDate);

        // Then
        assertThat(dto.getMessageType()).isEqualTo(MessageType.MT202);
        assertThat(dto.getSenderBic()).isEqualTo("TESTBIC1");
        assertThat(dto.getReceiverBic()).isEqualTo("TESTBIC2");
        assertThat(dto.getAmount()).isEqualTo(amount);
        assertThat(dto.getCurrency()).isEqualTo("USD");
        assertThat(dto.getValueDate()).isEqualTo(valueDate);
    }
}