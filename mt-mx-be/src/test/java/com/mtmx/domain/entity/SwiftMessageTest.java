package com.mtmx.domain.entity;

import com.mtmx.domain.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SwiftMessageTest {

    private SwiftMessage swiftMessage;

    @BeforeEach
    void setUp() {
        swiftMessage = new SwiftMessage();
    }

    @Test
    void constructor_ShouldCreateEmptyInstance() {
        // Then
        assertThat(swiftMessage).isNotNull();
        assertThat(swiftMessage.getId()).isNull();
        assertThat(swiftMessage.getMessageType()).isNull();
        assertThat(swiftMessage.getSenderBic()).isNull();
        assertThat(swiftMessage.getReceiverBic()).isNull();
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        // Given
        Long id = 1L;
        MessageType messageType = MessageType.MT103;
        String senderBic = "BANKBEBB";
        String receiverBic = "BANKDEFF";
        BigDecimal amount = BigDecimal.valueOf(5000.00);
        String currency = "EUR";
        LocalDate valueDate = LocalDate.of(2025, 6, 22);
        String rawMtMessage = "{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF123-}";
        String generatedMxMessage = "<?xml version=\"1.0\"?><Document>...</Document>";
        LocalDateTime now = LocalDateTime.now();

        // When
        swiftMessage.setId(id);
        swiftMessage.setMessageType(messageType);
        swiftMessage.setSenderBic(senderBic);
        swiftMessage.setReceiverBic(receiverBic);
        swiftMessage.setAmount(amount);
        swiftMessage.setCurrency(currency);
        swiftMessage.setValueDate(valueDate);
        swiftMessage.setRawMtMessage(rawMtMessage);
        swiftMessage.setGeneratedMxMessage(generatedMxMessage);
        swiftMessage.setCreatedAt(now);
        swiftMessage.setUpdatedAt(now);

        // Then
        assertThat(swiftMessage.getId()).isEqualTo(id);
        assertThat(swiftMessage.getMessageType()).isEqualTo(messageType);
        assertThat(swiftMessage.getSenderBic()).isEqualTo(senderBic);
        assertThat(swiftMessage.getReceiverBic()).isEqualTo(receiverBic);
        assertThat(swiftMessage.getAmount()).isEqualTo(amount);
        assertThat(swiftMessage.getCurrency()).isEqualTo(currency);
        assertThat(swiftMessage.getValueDate()).isEqualTo(valueDate);
        assertThat(swiftMessage.getRawMtMessage()).isEqualTo(rawMtMessage);
        assertThat(swiftMessage.getGeneratedMxMessage()).isEqualTo(generatedMxMessage);
        assertThat(swiftMessage.getCreatedAt()).isEqualTo(now);
        assertThat(swiftMessage.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Given
        SwiftMessage message1 = new SwiftMessage();
        message1.setId(1L);
        message1.setMessageType(MessageType.MT103);
        message1.setSenderBic("BANKBEBB");

        SwiftMessage message2 = new SwiftMessage();
        message2.setId(1L);
        message2.setMessageType(MessageType.MT103);
        message2.setSenderBic("BANKBEBB");

        SwiftMessage message3 = new SwiftMessage();
        message3.setId(2L);
        message3.setMessageType(MessageType.MT102);
        message3.setSenderBic("OTHERBANKXXX");

        // Then
        assertThat(message1).isEqualTo(message2);
        assertThat(message1).isNotEqualTo(message3);
        assertThat(message1.hashCode()).isEqualTo(message2.hashCode());
        assertThat(message1.hashCode()).isNotEqualTo(message3.hashCode());
    }

    @Test
    void toString_ShouldContainImportantFields() {
        // Given
        swiftMessage.setId(1L);
        swiftMessage.setMessageType(MessageType.MT103);
        swiftMessage.setSenderBic("BANKBEBB");
        swiftMessage.setReceiverBic("BANKDEFF");

        // When
        String toString = swiftMessage.toString();

        // Then
        assertThat(toString).contains("1");
        assertThat(toString).contains("MT103");
        assertThat(toString).contains("BANKBEBB");
        assertThat(toString).contains("BANKDEFF");
    }

    @Test
    void prePersist_ShouldSetCreatedAt() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        swiftMessage.prePersist();

        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(swiftMessage.getCreatedAt()).isAfter(before);
        assertThat(swiftMessage.getCreatedAt()).isBefore(after);
        assertThat(swiftMessage.getUpdatedAt()).isNotNull();
        // Allow small time difference between createdAt and updatedAt
        assertThat(swiftMessage.getUpdatedAt()).isAfterOrEqualTo(swiftMessage.getCreatedAt().minusNanos(1000000));
        assertThat(swiftMessage.getUpdatedAt()).isBeforeOrEqualTo(swiftMessage.getCreatedAt().plusNanos(1000000));
    }

    @Test
    void preUpdate_ShouldSetUpdatedAt() {
        // Given
        LocalDateTime originalCreatedAt = LocalDateTime.now().minusHours(1);
        swiftMessage.setCreatedAt(originalCreatedAt);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        swiftMessage.preUpdate();

        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(swiftMessage.getCreatedAt()).isEqualTo(originalCreatedAt); // Should not change
        assertThat(swiftMessage.getUpdatedAt()).isAfter(before);
        assertThat(swiftMessage.getUpdatedAt()).isBefore(after);
    }

    @Test
    void builder_ShouldCreateInstanceCorrectly() {
        // Given & When
        SwiftMessage message = SwiftMessage.builder()
                .id(1L)
                .messageType(MessageType.MT103)
                .senderBic("BANKBEBB")
                .receiverBic("BANKDEFF")
                .amount(BigDecimal.valueOf(1000.00))
                .currency("USD")
                .valueDate(LocalDate.of(2025, 6, 22))
                .rawMtMessage("test message")
                .generatedMxMessage("test xml")
                .build();

        // Then
        assertThat(message.getId()).isEqualTo(1L);
        assertThat(message.getMessageType()).isEqualTo(MessageType.MT103);
        assertThat(message.getSenderBic()).isEqualTo("BANKBEBB");
        assertThat(message.getReceiverBic()).isEqualTo("BANKDEFF");
        assertThat(message.getAmount()).isEqualTo(BigDecimal.valueOf(1000.00));
        assertThat(message.getCurrency()).isEqualTo("USD");
        assertThat(message.getValueDate()).isEqualTo(LocalDate.of(2025, 6, 22));
        assertThat(message.getRawMtMessage()).isEqualTo("test message");
        assertThat(message.getGeneratedMxMessage()).isEqualTo("test xml");
    }

    @Test
    void nullFields_ShouldBeHandledCorrectly() {
        // Given & When
        swiftMessage.setAmount(null);
        swiftMessage.setCurrency(null);
        swiftMessage.setValueDate(null);
        swiftMessage.setRawMtMessage(null);
        swiftMessage.setGeneratedMxMessage(null);

        // Then
        assertThat(swiftMessage.getAmount()).isNull();
        assertThat(swiftMessage.getCurrency()).isNull();
        assertThat(swiftMessage.getValueDate()).isNull();
        assertThat(swiftMessage.getRawMtMessage()).isNull();
        assertThat(swiftMessage.getGeneratedMxMessage()).isNull();
    }
}
