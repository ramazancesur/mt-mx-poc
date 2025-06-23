package com.mtmx.service.conversion;

import com.mtmx.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MT102 conversion testleri - MT to MX dönüşümü
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class Mt102ConversionTest {

    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionService();
    }

    @Test
    void testMt102MinToMxConversion() throws IOException {
        // MT102 minimum mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        // MT'den MX'e dönüştür
        String mxResult = conversionService.convertMtToMx(mtContent);
        
        // Assertion
        assertNotNull(mxResult, "MX sonucu null olmamalı");
        assertTrue(mxResult.contains("Document"), "MX formatında Document elementi olmalı");
        assertTrue(mxResult.contains("pacs.008"), "pacs.008 namespace olmalı");
        assertTrue(mxResult.contains("REF102MIN"), "Transaction reference korunmalı");
        assertTrue(mxResult.contains("1000"), "Amount korunmalı");
    }

    @Test
    void testMt102MaxToMxConversion() throws IOException {
        // MT102 maximum mesajını oku
        String mtContent = loadTestResource("test-data/mt102/max/mt102_max.txt");
        
        // MT'den MX'e dönüştür
        String mxResult = conversionService.convertMtToMx(mtContent);
        
        // Assertion
        assertNotNull(mxResult, "MX sonucu null olmamalı");
        assertTrue(mxResult.contains("Document"), "MX formatında Document elementi olmalı");
        assertTrue(mxResult.contains("REF102MAX123456"), "Transaction reference korunmalı");
        assertTrue(mxResult.contains("10000"), "Amount korunmalı");
    }

    @Test
    void testMt102InvalidMessage() {
        // Geçersiz MT102 mesajı
        String invalidMt = "INVALID_MESSAGE";
        
        // MT'den MX'e dönüştür
        String mxResult = conversionService.convertMtToMx(invalidMt);
        
        // Assertion - fallback XML döndürülmeli
        assertNotNull(mxResult, "MX sonucu null olmamalı");
        assertTrue(mxResult.contains("Document"), "Fallback XML formatında olmalı");
    }

    @Test
    void testMt102MessageTypeDetection() throws IOException {
        // MT102 mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        // Message type detection
        String messageType = conversionService.getMessageType(mtContent);
        
        // Assertion
        assertEquals("102", messageType, "Message type MT102 olarak algılanmalı");
    }

    @Test
    void testMt102ValidationCheck() throws IOException {
        // MT102 mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        // Validation check
        boolean isValid = conversionService.isValidMtMessage(mtContent);
        
        // Assertion
        assertTrue(isValid, "MT102 mesajı geçerli olmalı");
    }

    private String loadTestResource(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return Files.readString(resource.getFile().toPath());
    }
}
