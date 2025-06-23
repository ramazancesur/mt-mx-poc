package com.mtmx.service;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class SwiftMessageValidatorIntegrationTest {

    @Autowired
    private SwiftMessageValidator validator;

    private SwiftMessage validMt103Message;

    @BeforeEach
    void setUp() {
        validMt103Message = createValidMt103Message();
    }

    @Test
    void validateAndSave_WithValidMt103_ShouldSucceed() {
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        
        // Debug için hataları yazdır
        if (!result.isValid()) {
            System.out.println("Validation errors:");
            result.getErrors().forEach(System.out::println);
        }
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getSavedMessage()).isNotNull();
    }

    @Test
    void validateAndSave_WithInvalidSenderBic_ShouldFail() {
        validMt103Message.setSenderBic("INVALID");
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Geçersiz Sender BIC formatı"));
    }

    @Test
    void validateAndSave_WithNullAmount_ShouldFail() {
        validMt103Message.setAmount(null);
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Amount zorunludur"));
    }

    @Test
    void validateAndSave_WithZeroAmount_ShouldFail() {
        validMt103Message.setAmount(BigDecimal.ZERO);
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Amount sıfırdan büyük olmalıdır"));
    }

    @Test
    void validateAndSave_WithInvalidCurrency_ShouldFail() {
        validMt103Message.setCurrency("INVALID");
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Geçersiz currency formatı"));
    }

    @Test
    void validateAndSave_WithFutureValueDate_ShouldFail() {
        validMt103Message.setValueDate(LocalDate.now().plusYears(2));
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Value date çok ileridedir"));
    }

    @Test
    void validateAndSave_WithPastValueDate_ShouldFail() {
        validMt103Message.setValueDate(LocalDate.now().minusYears(2));
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Value date çok eskidir"));
    }

    @Test
    void validateAndSave_WithNullRawMessage_ShouldFail() {
        validMt103Message.setRawMtMessage(null);
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Raw MT message zorunludur"));
    }

    @Test
    void validateAndSave_WithInvalidRawMessageFormat_ShouldFail() {
        validMt103Message.setRawMtMessage("INVALID FORMAT");
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("Geçersiz SWIFT mesaj formatı"));
    }

    @Test
    void validateAndSave_WithMt103ExcessiveAmount_ShouldFail() {
        validMt103Message.setAmount(new BigDecimal("1000000000000.00"));
        SwiftMessageValidator.ValidationResult result = validator.validateAndSave(validMt103Message);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("maksimum amount 999,999,999,999.99"));
    }

    private SwiftMessage createValidMt103Message() {
        SwiftMessage message = new SwiftMessage();
        message.setMessageType(MessageType.MT103);
        message.setSenderBic("BANKBEBB");
        message.setReceiverBic("BANKDEFF");
        message.setAmount(new BigDecimal("1000.00"));
        message.setCurrency("EUR");
        message.setValueDate(LocalDate.now());
        message.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4:" +
                               ":20:REF103\n:32A:241222EUR1000,00\n:50A:BANKBEBB\n:59A:BANKDEFF\n:71A:SHA\n-}");
        return message;
    }
}
