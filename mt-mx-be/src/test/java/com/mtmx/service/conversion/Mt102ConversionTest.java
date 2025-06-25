package com.mtmx.service.conversion;

import com.mtmx.service.ConversionService;
import com.mtmx.service.converter.ConversionException;
import com.mtmx.service.converter.MtMessageValidator;
import com.mtmx.service.converter.MxMessageValidator;
import com.mtmx.service.converter.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MT102 conversion testleri - MT to MX dönüşümü
 */
@ExtendWith(MockitoExtension.class)
class Mt102ConversionTest {

    @Mock
    private MtMessageValidator mtMessageValidator;

    @Mock
    private MxMessageValidator mxMessageValidator;

    @Mock
    private Mt103ToMxConverter mt103ToMxConverter;

    @Mock
    private Mt202ToMxConverter mt202ToMxConverter;

    @Mock
    private Mt102ToMxConverter mt102ToMxConverter;

    @Mock
    private Mt203ToMxConverter mt203ToMxConverter;

    @Mock
    private Mt202CovToMxConverter mt202CovToMxConverter;

    @Mock
    private MxToMt103Converter mxToMt103Converter;

    @Mock
    private MxToMt202Converter mxToMt202Converter;

    @Mock
    private MxToMt102Converter mxToMt102Converter;

    @Mock
    private MxToMt203Converter mxToMt203Converter;

    @Mock
    private MxToMt202CovConverter mxToMt202CovConverter;

    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionService(
                mtMessageValidator, mxMessageValidator,
                mt103ToMxConverter, mt202ToMxConverter, mt102ToMxConverter, mt203ToMxConverter, mt202CovToMxConverter,
                mxToMt103Converter, mxToMt202Converter, mxToMt102Converter, mxToMt203Converter, mxToMt202CovConverter);
        conversionService.initializeConverters();
    }

    @Test
    void testMt102MinToMxConversion() throws IOException, ConversionException {
        // MT102 minimum mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        // Mock setup
        when(mtMessageValidator.isValid(mtContent)).thenReturn(true);
        when(mtMessageValidator.getMessageType(mtContent)).thenReturn("102");
        when(mt102ToMxConverter.convert(mtContent)).thenReturn(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">...</Document>");

        // MT'den MX'e dönüştür
        String mxResult = conversionService.convertMtToMx(mtContent);
        
        // Assertion
        assertNotNull(mxResult, "MX sonucu null olmamalı");
        assertTrue(mxResult.contains("Document"), "MX formatında Document elementi olmalı");
        assertTrue(mxResult.contains("pacs.008"), "pacs.008 namespace olmalı");
    }

    @Test
    void testMt102MaxToMxConversion() throws IOException, ConversionException {
        // MT102 maximum mesajını oku
        String mtContent = loadTestResource("test-data/mt102/max/mt102_max.txt");
        
        // Mock setup
        when(mtMessageValidator.isValid(mtContent)).thenReturn(true);
        when(mtMessageValidator.getMessageType(mtContent)).thenReturn("102");
        when(mt102ToMxConverter.convert(mtContent)).thenReturn(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">...</Document>");

        // MT'den MX'e dönüştür
        String mxResult = conversionService.convertMtToMx(mtContent);
        
        // Assertion
        assertNotNull(mxResult, "MX sonucu null olmamalı");
        assertTrue(mxResult.contains("Document"), "MX formatında Document elementi olmalı");
    }

    @Test
    void testMt102InvalidMessage() {
        // Geçersiz MT102 mesajı
        String invalidMt = "INVALID_MESSAGE";
        
        // Mock setup
        when(mtMessageValidator.isValid(invalidMt)).thenReturn(false);
        
        // MT'den MX'e dönüştür - exception bekleniyor
        assertThrows(ConversionException.class, () -> conversionService.convertMtToMx(invalidMt));
    }

    @Test
    void testMt102MessageTypeDetection() throws IOException {
        // MT102 mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        // Mock setup
        when(mtMessageValidator.getMessageType(mtContent)).thenReturn("102");

        // Message type detection
        String messageType = conversionService.getMtMessageType(mtContent);
        
        // Assertion
        assertEquals("102", messageType, "Message type MT102 olarak algılanmalı");
    }

    @Test
    void testMt102ValidationCheck() throws IOException {
        // MT102 mesajını oku
        String mtContent = loadTestResource("test-data/mt102/min/mt102_min.txt");
        
        // Mock setup
        when(mtMessageValidator.isValid(mtContent)).thenReturn(true);

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