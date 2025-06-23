package com.mtmx.service;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.repository.SwiftMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwiftMessageValidatorTest {

    @Mock
    private SwiftMessageRepository swiftMessageRepository;

    private SwiftMessageValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SwiftMessageValidator(swiftMessageRepository);
    }

    @Test
    void validateAndSave_WithValidMT103_ShouldReturnSuccess() {
        // Given
        SwiftMessage message = createValidMT103Message();
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(message);

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validateAndSave_WithInvalidMT103_ShouldReturnErrors() {
        // Given
        SwiftMessage message = createValidMT103Message();
        message.setSenderBic("INVALID"); // Invalid BIC

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void validateAndSave_WithNullMessage_ShouldReturnErrors() {
        // When
        SwiftMessageValidator.ValidationResult result = null;
        try {
            result = validator.validateAndSave(null);
        } catch (Exception e) {
            // Expected exception for null message
            assertThat(e).isInstanceOf(NullPointerException.class);
            return;
        }

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void validateAndSave_WithMissingMandatoryFields_ShouldReturnErrors() {
        // Given
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT103);
        // Missing other mandatory fields

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    void validateAndSave_WithValidMT102_ShouldReturnSuccess() {
        // Given
        SwiftMessage message = createValidMT102Message();
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(message);

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validateAndSave_WithValidMT202_ShouldReturnSuccess() {
        // Given
        SwiftMessage message = createValidMT202Message();
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(message);

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validateAndSave_WithValidMT202COV_ShouldReturnSuccess() {
        // Given
        SwiftMessage message = createValidMT202COVMessage();
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(message);

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validateAndSave_WithValidMT203_ShouldReturnSuccess() {
        // Given
        SwiftMessage message = createValidMT203Message();
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(message);

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void validateAndSave_WithInvalidBIC_ShouldReturnErrors() {
        // Given
        SwiftMessage message = createValidMT103Message();
        message.setSenderBic("INVALID_BIC");

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Geçersiz Sender BIC formatı: INVALID_BIC");
    }

    @Test
    void validateAndSave_WithNegativeAmount_ShouldReturnErrors() {
        // Given
        SwiftMessage message = createValidMT103Message();
        message.setAmount(new BigDecimal("-100.00"));

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Amount sıfırdan büyük olmalıdır");
    }

    @Test
    void validateAndSave_WithInvalidCurrency_ShouldReturnErrors() {
        // Given
        SwiftMessage message = createValidMT103Message();
        message.setCurrency("INVALID");

        // When
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(message);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).contains("Geçersiz currency formatı: INVALID");
    }

    // Helper methods
    private SwiftMessage createValidMT103Message() {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT103);
        message.setSenderBic("BANKBEBB000");
        message.setReceiverBic("BANKDEFF000");
        message.setAmount(new BigDecimal("1000.00"));
        message.setCurrency("EUR");
        message.setValueDate(LocalDate.now());
        message.setRawMtMessage(
                "{1:F01BANKBEBB000000000}{2:I103BANKDEFF000N}{4:\n" +
                        ":20:ABC123\n" +
                        ":23B:CRED\n" +
                        ":32A:250622EUR1000,\n" +
                        ":50A:BANKBEBB000\n" +
                        ":59A:BANKDEFF000\n" +
                        ":70:PAYMENT\n" +
                        ":71A:SHA\n" +
                        "-}");
        return message;
    }

    private SwiftMessage createValidMT102Message() {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT102);
        message.setSenderBic("BANKBEBB000");
        message.setReceiverBic("BANKDEFF000");
        message.setAmount(new BigDecimal("5000.00"));
        message.setCurrency("EUR");
        message.setValueDate(LocalDate.now());
        message.setRawMtMessage(
                "{1:F01BANKBEBB000000000}{2:I102BANKDEFF000N}{4:\n" +
                        ":20:TXN123\n" +
                        ":23:CRED\n" +
                        ":32A:250622EUR5000,\n" +
                        ":50A:BANKBEBB000\n" +
                        ":21:TXN001\n" +
                        ":32B:EUR1000,\n" +
                        ":59A:BANKDEFF000\n" +
                        ":21:TXN002\n" +
                        ":32B:EUR2000,\n" +
                        ":59A:BANKCHZZ000\n" +
                        "-}");
        return message;
    }

    private SwiftMessage createValidMT202Message() {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT202);
        message.setSenderBic("BANKBEBB000");
        message.setReceiverBic("BANKDEFF000");
        message.setAmount(new BigDecimal("10000.00"));
        message.setCurrency("EUR");
        message.setValueDate(LocalDate.now());
        message.setRawMtMessage(
                "{1:F01BANKBEBB000000000}{2:I202BANKDEFF000N}{4:\n" +
                        ":20:REF123\n" +
                        ":32A:250622EUR10000,00\n" +
                        ":52A:BANKBEBB000\n" +
                        ":58A:BANKCHZZ000\n" +
                        "-}");
        return message;
    }

    private SwiftMessage createValidMT202COVMessage() {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT202COV);
        message.setSenderBic("BANKBEBB000");
        message.setReceiverBic("BANKDEFF000");
        message.setAmount(new BigDecimal("15000.00"));
        message.setCurrency("USD");
        message.setValueDate(LocalDate.now());
        message.setRawMtMessage(
                "{1:F01BANKBEBB000000000}{2:I202COVBANKDEFF000N}{4:\n" +
                        ":20:COV123\n" +
                        ":21:REL123\n" +
                        ":32A:250622USD15000,00\n" +
                        ":50A:BANKBEBB000\n" +
                        ":58A:BANKCHZZ000\n" +
                        ":59A:BANKDEFF000\n" +
                        ":72:/BNF/UNDERLYING PAYMENT\n" +
                        "-}");
        return message;
    }

    private SwiftMessage createValidMT203Message() {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT203);
        message.setSenderBic("BANKBEBB000");
        message.setReceiverBic("BANKDEFF000");
        message.setAmount(new BigDecimal("8000.00"));
        message.setCurrency("GBP");
        message.setValueDate(LocalDate.now());
        message.setRawMtMessage(
                "{1:F01BANKBEBB000000000}{2:I203BANKDEFF000N}{4:\n" +
                        ":20:REF203\n" +
                        ":19:8000,00\n" +
                        ":30:250622\n" +
                        ":32A:250622GBP8000,00\n" +
                        ":52A:BANKBEBB000\n" +
                        ":58A:BANKCHZZ000\n" +
                        ":21:TXN001\n" +
                        ":32A:250622GBP4000,00\n" +
                        ":58A:BANKGBZZ000\n" +
                        ":21:TXN002\n" +
                        ":32A:250622GBP4000,00\n" +
                        ":58A:BANKFRZZ000\n" +
                        "-}");
        return message;
    }
}