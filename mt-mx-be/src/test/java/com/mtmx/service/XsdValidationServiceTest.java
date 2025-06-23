package com.mtmx.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class XsdValidationServiceTest {

    private XsdValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new XsdValidationService();
    }

    @Test
    void testValidateByMtType_MT103() {
        String validXml = "<Document><test>MT103</test></Document>";
        XsdValidationService.ValidationResult result = validationService.validateByMtType(validXml, "MT103");
        
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }

    @Test
    void testValidateByMtType_UnsupportedType() {
        String xml = "<test>content</test>";
        
        XsdValidationService.ValidationResult result = validationService.validateByMtType(xml, "MT999");
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("Desteklenmeyen MT tipi"));
    }

    @Test
    void testValidateByMtType_NullType() {
        String xml = "<test>content</test>";
        
        XsdValidationService.ValidationResult result = validationService.validateByMtType(xml, null);
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("MT tipi belirtilmedi"));
    }

    @Test
    void testValidateEmptyXml() {
        XsdValidationService.ValidationResult result = validationService.validatePacs008("");
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("XML içeriği boş"));
    }

    @Test
    void testValidateNullXml() {
        XsdValidationService.ValidationResult result = validationService.validatePacs008(null);
        
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("XML içeriği boş"));
    }

    @Test
    void testValidateBatch() {
        String validXml1 = "<Document><test>content1</test></Document>";
        String validXml2 = "<Document><test>content2</test></Document>";

        List<String> xmlMessages = Arrays.asList(validXml1, validXml2);
        List<XsdValidationService.ValidationResult> results = validationService.validateBatch(xmlMessages, "MT103");
        
        assertEquals(2, results.size());
        assertNotNull(results.get(0));
        assertNotNull(results.get(1));
    }

    @Test
    void testValidationResult_ToString() {
        XsdValidationService.ValidationResult result = 
            XsdValidationService.ValidationResult.valid("Test message");
        
        String toString = result.toString();
        System.out.println("Actual toString: " + toString);
        
        assertTrue(toString.contains("ValidationResult"));
        assertTrue(toString.contains("valid=true"));
        assertTrue(toString.contains("Test message"));
    }

    @Test
    void testValidationResult_InvalidResult() {
        XsdValidationService.ValidationResult result = 
            XsdValidationService.ValidationResult.invalid("Error occurred");
        
        assertFalse(result.isValid());
        assertEquals("Error occurred", result.getMessage());
    }
}
