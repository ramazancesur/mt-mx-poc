package com.mtmx.service;

import com.mtmx.service.converter.ConversionException;
import com.mtmx.service.converter.MessageConverter;
import com.mtmx.service.converter.MtMessageValidator;
import com.mtmx.service.converter.MxMessageValidator;
import com.mtmx.service.converter.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main conversion service following SOLID principles
 * Orchestrates MT to MX and MX to MT conversions using specialized converters
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConversionService {
    
    private final MtMessageValidator mtMessageValidator;
    private final MxMessageValidator mxMessageValidator;
    
    // MT to MX converters
    private final Mt103ToMxConverter mt103ToMxConverter;
    private final Mt202ToMxConverter mt202ToMxConverter;
    private final Mt102ToMxConverter mt102ToMxConverter;
    private final Mt203ToMxConverter mt203ToMxConverter;
    private final Mt202CovToMxConverter mt202CovToMxConverter;

    // MX to MT converters
    private final MxToMt103Converter mxToMt103Converter;
    private final MxToMt202Converter mxToMt202Converter;
    private final MxToMt102Converter mxToMt102Converter;
    private final MxToMt203Converter mxToMt203Converter;
    private final MxToMt202CovConverter mxToMt202CovConverter;

    // Converter registry for easy lookup
    private final Map<String, MessageConverter<String, String>> mtToMxConverters = new HashMap<>();
    private final Map<String, MessageConverter<String, String>> mxToMtConverters = new HashMap<>();

    /**
     * Initialize converter registries after dependency injection
     */
    @PostConstruct
    public void initializeConverters() {
        // MT to MX converters - Her mesaj tipi için kendi converter'ını kullan
        mtToMxConverters.put("103", mt103ToMxConverter);
        mtToMxConverters.put("202", mt202ToMxConverter);
        mtToMxConverters.put("102", mt102ToMxConverter);
        mtToMxConverters.put("203", mt203ToMxConverter);
        mtToMxConverters.put("202COV", mt202CovToMxConverter);

        // MX to MT converters - Sadece temel dönüşümler (çakışma yönetimi için)
        mxToMtConverters.put("pacs.008.001.08", mxToMt103Converter); // pacs.008 -> MT103 (varsayılan)
        mxToMtConverters.put("pacs.009.001.08", mxToMt202Converter); // pacs.009 -> MT202

        log.info("ConversionService initialized with {} MT to MX converters and {} MX to MT converters",
                mtToMxConverters.size(), mxToMtConverters.size());
    }

    /**
     * Convert MT message to MX format
     * 
     * @param mtMessage MT message to convert
     * @return Converted MX message
     * @throws ConversionException if conversion fails
     */
    public String convertMtToMx(String mtMessage) throws ConversionException {
        if (mtMessage == null || mtMessage.trim().isEmpty()) {
            throw new ConversionException("MT message cannot be null or empty");
        }

        try {
            // Validate MT message
            if (!mtMessageValidator.isValid(mtMessage)) {
                throw new ConversionException("Invalid MT message format");
            }
            
            // Get message type
            String messageType = mtMessageValidator.getMessageType(mtMessage);
            if (messageType == null) {
                throw new ConversionException("Could not determine MT message type");
            }

            // Find appropriate converter
            MessageConverter<String, String> converter = mtToMxConverters.get(messageType);
            if (converter == null) {
                throw new ConversionException("No converter found for MT message type: " + messageType);
            }
            
            // Perform conversion
            String mxMessage = converter.convert(mtMessage);
            log.info("Successfully converted MT{} to MX format", messageType);
            
            return mxMessage;
            
        } catch (ConversionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during MT to MX conversion: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MT to MX", e);
        }
    }

    /**
     * Convert MX message to MT format
     * 
     * @param mxMessage MX message to convert
     * @return Converted MT message
     * @throws ConversionException if conversion fails
     */
    public String convertMxToMt(String mxMessage) throws ConversionException {
        if (mxMessage == null || mxMessage.trim().isEmpty()) {
            throw new ConversionException("MX message cannot be null or empty");
        }

        try {
            // Validate MX message
            if (!mxMessageValidator.isValid(mxMessage)) {
                throw new ConversionException("Invalid MX message format");
            }
            
            // Get message type
            String messageType = mxMessageValidator.getMessageType(mxMessage);
            if (messageType == null) {
                throw new ConversionException("Could not determine MX message type");
            }

            // Find appropriate converter
            MessageConverter<String, String> converter = mxToMtConverters.get(messageType);
            if (converter == null) {
                throw new ConversionException("No converter found for MX message type: " + messageType);
            }
            
            // Perform conversion
            String mtMessage = converter.convert(mxMessage);
            log.info("Successfully converted MX {} to MT format", messageType);
            
            return mtMessage;
            
        } catch (ConversionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during MX to MT conversion: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MX to MT", e);
        }
    }

    /**
     * Get supported MT message types
     * 
     * @return List of supported MT message types
     */
    public List<String> getSupportedMtMessageTypes() {
        return List.of("102", "103", "202", "202COV", "203");
    }

    /**
     * Get supported MX message types
     * 
     * @return List of supported MX message types
     */
    public List<String> getSupportedMxMessageTypes() {
        return List.of("pacs.008.001.08", "pacs.009.001.08");
    }

    /**
     * Validate MT message
     * 
     * @param mtMessage MT message to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMtMessage(String mtMessage) {
        return mtMessageValidator.isValid(mtMessage);
    }

    /**
     * Validate MX message
     * 
     * @param mxMessage MX message to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMxMessage(String mxMessage) {
        return mxMessageValidator.isValid(mxMessage);
    }

    /**
     * Get MT message type
     * 
     * @param mtMessage MT message
     * @return Message type or null if not found
     */
    public String getMtMessageType(String mtMessage) {
        return mtMessageValidator.getMessageType(mtMessage);
    }

    /**
     * Get MX message type
     * 
     * @param mxMessage MX message
     * @return Message type or null if not found
     */
    public String getMxMessageType(String mxMessage) {
        return mxMessageValidator.getMessageType(mxMessage);
    }

    /**
     * Extract field from MT message
     * 
     * @param mtMessage MT message
     * @param fieldName Field name to extract
     * @return Field value or null if not found
     */
    public String extractMtField(String mtMessage, String fieldName) {
        return mtMessageValidator.extractField(mtMessage, fieldName);
    }

    /**
     * Extract element value from MX message
     * 
     * @param mxMessage   MX message
     * @param elementPath Element path
     * @return Element value or null if not found
     */
    public String extractMxElement(String mxMessage, String elementPath) {
        return mxMessageValidator.extractElementValue(mxMessage, elementPath);
    }
}