package com.mtmx.service.validation;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MT102 validation testleri - minimum ve maximum senaryolar
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class Mt102ValidationTest {

    private Mt102Validator validator;
    private BasicFieldValidator basicValidator;

    @BeforeEach
    void setUp() {
        validator = new Mt102Validator();
        basicValidator = new BasicFieldValidator();
    }

    @Test
    void testMt102MinimumValid() throws IOException {
        // Resource'dan minimum MT102 mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        SwiftMessage message = createSwiftMessage(mtContent, MessageType.MT102, 
                                                new BigDecimal("1000.00"));
        ValidationResult result = new ValidationResult();
        
        // Validasyon
        basicValidator.validateBasicFields(message, result);
        validator.validateMt102(message, result);
        
        // Debug: Error mesajlarını yazdır
        if (!result.isValid()) {
            System.out.println("Validation errors:");
            result.getErrors().forEach(System.out::println);
        }
        
        // Assertion
        assertTrue(result.isValid(), "MT102 minimum mesaj geçerli olmalı");
    }

    @Test
    void testMt102MaximumValid() throws IOException {
        // Resource'dan maximum MT102 mesajını oku
        String mtContent = loadTestResource("test-data/mt102/max/mt102_max.txt");
        
        SwiftMessage message = createSwiftMessage(mtContent, MessageType.MT102, 
                                                new BigDecimal("10000.00"));
        ValidationResult result = new ValidationResult();
        
        // Validasyon
        basicValidator.validateBasicFields(message, result);
        validator.validateMt102(message, result);
        
        // Debug: Error mesajlarını yazdır
        if (!result.isValid()) {
            System.out.println("Validation errors:");
            result.getErrors().forEach(System.out::println);
        }
        
        // Assertion
        assertTrue(result.isValid(), "MT102 maximum mesaj geçerli olmalı");
    }

    @Test
    void testMt102InvalidAmount() {
        SwiftMessage message = createSwiftMessage("", MessageType.MT102, 
                                                new BigDecimal("0.005"));
        ValidationResult result = new ValidationResult();
        
        validator.validateMt102(message, result);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("minimum amount 0.01")));
    }

    @Test
    void testMt102MissingMandatoryFields() {
        String invalidMt = "{1:F01BANKBEBB0000000000}{2:I102BANKDEFFN}{4:-}";
        
        SwiftMessage message = createSwiftMessage(invalidMt, MessageType.MT102, 
                                                new BigDecimal("1000.00"));
        ValidationResult result = new ValidationResult();
        
        validator.validateMt102(message, result);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() > 0);
    }

    private String loadTestResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return Files.readString(resource.getFile().toPath());
    }

    private SwiftMessage createSwiftMessage(String rawMt, MessageType type, BigDecimal amount) {
        SwiftMessage message = new SwiftMessage();
        message.setRawMtMessage(rawMt);
        message.setMessageType(type);
        message.setAmount(amount);
        message.setCurrency("EUR");
        message.setValueDate(LocalDate.now());
        message.setSenderBic("BANKBEBB");
        message.setReceiverBic("BANKDEFF");
        return message;
    }
}
