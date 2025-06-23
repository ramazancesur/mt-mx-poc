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

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class Mt103ValidationTest {

    private Mt103Validator validator;
    private BasicFieldValidator basicValidator;

    @BeforeEach
    void setUp() {
        validator = new Mt103Validator();
        basicValidator = new BasicFieldValidator();
    }

    @Test
    void testMt103MinimumValid() throws IOException {
        String mtContent = loadTestResource("test-data/mt103/min/mt103_min.txt");
        
        SwiftMessage message = createSwiftMessage(mtContent, MessageType.MT103, 
                                                new BigDecimal("1500.00"));
        ValidationResult result = new ValidationResult();
        
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);


        assertTrue(result.isValid(), "MT103 minimum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);


        assertTrue(result.isValid(), "MT103 minimum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);


        assertTrue(result.isValid(), "MT103 minimum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);


        assertTrue(result.isValid(), "MT103 minimum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);


        assertTrue(result.isValid(), "MT103 minimum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
        assertTrue(result.isValid(), "MT103 minimum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void testMt103MaximumValid() throws IOException {
        String mtContent = loadTestResource("test-data/mt103/max/mt103_max.txt");
        
        SwiftMessage message = createSwiftMessage(mtContent, MessageType.MT103, 
                                                new BigDecimal("25000.00"));
        ValidationResult result = new ValidationResult();
        
        basicValidator.validateBasicFields(message, result);
        validator.validateMt103(message, result);
        
        assertTrue(result.isValid(), "MT103 maximum mesaj geçerli olmalı");
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void testMt103ExcessiveAmount() {
        SwiftMessage message = createSwiftMessage("", MessageType.MT103, 
                                                new BigDecimal("1000000000000.00"));
        ValidationResult result = new ValidationResult();
        
        validator.validateMt103(message, result);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("maksimum amount")));
    }

    @Test
    void testMt103MissingCharges() {
        String invalidMt = "{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4:" +
                          ":20:REF103\n:32A:241222EUR1500,00\n:50A:BANKBEBB\n:59A:BANKDEFF\n-}";
        
        SwiftMessage message = createSwiftMessage(invalidMt, MessageType.MT103, 
                                                new BigDecimal("1500.00"));
        ValidationResult result = new ValidationResult();
        
        validator.validateMt103(message, result);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(error -> error.contains("Details of Charges")));
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
