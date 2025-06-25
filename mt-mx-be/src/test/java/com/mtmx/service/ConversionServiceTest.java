package com.mtmx.service;

import com.mtmx.service.converter.ConversionException;
import com.mtmx.service.converter.MtMessageValidator;
import com.mtmx.service.converter.MxMessageValidator;
import com.mtmx.service.converter.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {

    @Mock
    private MtMessageValidator mtMessageValidator;

    @Mock
    private MxMessageValidator mxMessageValidator;

    @Mock
    private Mt103ToMxConverter mt103ToMxConverter;

    @Mock
    private Mt202ToMxConverter mt202ToMxConverter;

    @Mock
    private MxToMt103Converter mxToMt103Converter;

    @Mock
    private MxToMt202Converter mxToMt202Converter;

    private ConversionService conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionService(
                mtMessageValidator, mxMessageValidator,
                mt103ToMxConverter, mt202ToMxConverter,
                mxToMt103Converter, mxToMt202Converter);
        conversionService.initializeConverters();
    }

    @Test
    void testConvertMtToMx_Success() throws ConversionException {
        // Given
        String mtMessage = "{1:F01BANKTRISAXXX1234567890}{2:I103BANKTRISAXXXN}{3:{108:MT103CONVERSION}}{4::20:REF123:32A:231215USD1000,00:50K:/1234567890123456DEBTOR NAME:59:/9876543210987654CREDITOR NAME:71A:SHA:72:/ACC/PAYMENT DETAILS-}";
        String expectedMxMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document>...</Document>";

        when(mtMessageValidator.isValid(mtMessage)).thenReturn(true);
        when(mtMessageValidator.getMessageType(mtMessage)).thenReturn("103");
        when(mt103ToMxConverter.convert(mtMessage)).thenReturn(expectedMxMessage);

        // When
        String result = conversionService.convertMtToMx(mtMessage);

        // Then
        assertEquals(expectedMxMessage, result);
        verify(mtMessageValidator).isValid(mtMessage);
        verify(mtMessageValidator).getMessageType(mtMessage);
        verify(mt103ToMxConverter).convert(mtMessage);
    }

    @Test
    void testConvertMtToMx_InvalidMessage() {
        // Given
        String mtMessage = "invalid message";
        when(mtMessageValidator.isValid(mtMessage)).thenReturn(false);

        // When & Then
        assertThrows(ConversionException.class, () -> conversionService.convertMtToMx(mtMessage));
        verify(mtMessageValidator).isValid(mtMessage);
        verifyNoMoreInteractions(mt103ToMxConverter, mt202ToMxConverter);
    }

    @Test
    void testConvertMtToMx_UnsupportedMessageType() {
        // Given
        String mtMessage = "{1:F01BANKTRISAXXX1234567890}{2:I999BANKTRISAXXXN}{3:{108:MT999CONVERSION}}{4::20:REF123-}";
        when(mtMessageValidator.isValid(mtMessage)).thenReturn(true);
        when(mtMessageValidator.getMessageType(mtMessage)).thenReturn("999");

        // When & Then
        assertThrows(ConversionException.class, () -> conversionService.convertMtToMx(mtMessage));
        verify(mtMessageValidator).isValid(mtMessage);
        verify(mtMessageValidator).getMessageType(mtMessage);
        verifyNoMoreInteractions(mt103ToMxConverter, mt202ToMxConverter);
    }

    @Test
    void testConvertMxToMt_Success() throws ConversionException {
        // Given
        String mxMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">...</Document>";
        String expectedMtMessage = "{1:F01BANKTRISAXXX1234567890}{2:I103BANKTRISAXXXN}...";

        when(mxMessageValidator.isValid(mxMessage)).thenReturn(true);
        when(mxMessageValidator.getMessageType(mxMessage)).thenReturn("pacs.008.001.08");
        when(mxToMt103Converter.convert(mxMessage)).thenReturn(expectedMtMessage);

        // When
        String result = conversionService.convertMxToMt(mxMessage);

        // Then
        assertEquals(expectedMtMessage, result);
        verify(mxMessageValidator).isValid(mxMessage);
        verify(mxMessageValidator).getMessageType(mxMessage);
        verify(mxToMt103Converter).convert(mxMessage);
    }

    @Test
    void testConvertMxToMt_InvalidMessage() {
        // Given
        String mxMessage = "invalid xml";
        when(mxMessageValidator.isValid(mxMessage)).thenReturn(false);

        // When & Then
        assertThrows(ConversionException.class, () -> conversionService.convertMxToMt(mxMessage));
        verify(mxMessageValidator).isValid(mxMessage);
        verifyNoMoreInteractions(mxToMt103Converter, mxToMt202Converter);
    }

    @Test
    void testIsValidMtMessage() {
        // Given
        String mtMessage = "valid mt message";
        when(mtMessageValidator.isValid(mtMessage)).thenReturn(true);

        // When
        boolean result = conversionService.isValidMtMessage(mtMessage);

        // Then
        assertTrue(result);
        verify(mtMessageValidator).isValid(mtMessage);
    }

    @Test
    void testIsValidMxMessage() {
        // Given
        String mxMessage = "valid mx message";
        when(mxMessageValidator.isValid(mxMessage)).thenReturn(true);

        // When
        boolean result = conversionService.isValidMxMessage(mxMessage);

        // Then
        assertTrue(result);
        verify(mxMessageValidator).isValid(mxMessage);
    }

    @Test
    void testGetMtMessageType() {
        // Given
        String mtMessage = "mt message";
        when(mtMessageValidator.getMessageType(mtMessage)).thenReturn("103");

        // When
        String result = conversionService.getMtMessageType(mtMessage);

        // Then
        assertEquals("103", result);
        verify(mtMessageValidator).getMessageType(mtMessage);
    }

    @Test
    void testGetMxMessageType() {
        // Given
        String mxMessage = "mx message";
        when(mxMessageValidator.getMessageType(mxMessage)).thenReturn("pacs.008.001.08");

        // When
        String result = conversionService.getMxMessageType(mxMessage);

        // Then
        assertEquals("pacs.008.001.08", result);
        verify(mxMessageValidator).getMessageType(mxMessage);
    }

    @Test
    void testGetSupportedMtMessageTypes() {
        // When
        var result = conversionService.getSupportedMtMessageTypes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("103"));
        assertTrue(result.contains("202"));
    }

    @Test
    void testGetSupportedMxMessageTypes() {
        // When
        var result = conversionService.getSupportedMxMessageTypes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("pacs.008.001.08"));
        assertTrue(result.contains("pacs.009.001.08"));
    }

    @Test
    void testExtractMtField() {
        // Given
        String mtMessage = "mt message";
        String fieldName = "20";
        when(mtMessageValidator.extractField(mtMessage, fieldName)).thenReturn("REF123");

        // When
        String result = conversionService.extractMtField(mtMessage, fieldName);

        // Then
        assertEquals("REF123", result);
        verify(mtMessageValidator).extractField(mtMessage, fieldName);
    }

    @Test
    void testExtractMxElement() {
        // Given
        String mxMessage = "mx message";
        String elementPath = "GrpHdr/MsgId";
        when(mxMessageValidator.extractElementValue(mxMessage, elementPath)).thenReturn("MSG123");

        // When
        String result = conversionService.extractMxElement(mxMessage, elementPath);

        // Then
        assertEquals("MSG123", result);
        verify(mxMessageValidator).extractElementValue(mxMessage, elementPath);
    }
}