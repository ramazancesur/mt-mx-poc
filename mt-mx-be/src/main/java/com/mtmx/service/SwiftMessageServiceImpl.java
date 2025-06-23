package com.mtmx.service;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.repository.SwiftMessageRepository;
import com.mtmx.web.dto.SwiftMessageDto;
import com.mtmx.web.mapper.SwiftMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class SwiftMessageServiceImpl implements SwiftMessageService {

    private final SwiftMessageRepository swiftMessageRepository;
    private final SwiftMessageMapper swiftMessageMapper;
    private final XsdValidationService xsdValidationService;
    private final ConversionService conversionService;

    @Override
    public SwiftMessageDto save(SwiftMessageDto swiftMessageDto) {
        SwiftMessage swiftMessage = swiftMessageMapper.toEntity(swiftMessageDto);

        if (swiftMessage.getRawMtMessage() != null && !swiftMessage.getRawMtMessage().isBlank()) {
            
            try {
                MessageType messageType = swiftMessage.getMessageType();
                
                if (messageType == null) {
                    messageType = determineMessageType(swiftMessage.getRawMtMessage());
                    swiftMessage.setMessageType(messageType);
                }
                
                // Convert MT to MX using ConversionService
                String mxMessage = conversionService.convertMtToMx(swiftMessage.getRawMtMessage());
                
                if (mxMessage != null && !mxMessage.contains("<error>")) {
                    // XSD doğrulaması yap
                    XsdValidationService.ValidationResult validationResult = 
                        xsdValidationService.validateByMtType(mxMessage, messageType.name());
                    
                    if (validationResult.isValid()) {
                        swiftMessage.setGeneratedMxMessage(mxMessage);
                        log.info("Successfully converted and validated {} message to MX format", messageType);
                    } else {
                        swiftMessage.setGeneratedMxMessage(mxMessage + 
                            "\n<!-- XSD Validation Warning: " + validationResult.getMessage() + " -->");
                        log.warn("MX message generated but XSD validation failed: {}", validationResult.getMessage());
                    }
                } else {
                    swiftMessage.setGeneratedMxMessage(mxMessage != null ? mxMessage : "<error>Conversion Failed</error>");
                    log.warn("Conversion failed for message type: {}", messageType);
                }
                
            } catch (Exception e) {
                log.error("Failed to convert MT to MX: {}", e.getMessage(), e);
                swiftMessage.setGeneratedMxMessage("<error>Conversion Failed: " + e.getMessage() + "</error>");
                throw new RuntimeException("Conversion failed", e);
            }
        }
        
        swiftMessage = swiftMessageRepository.save(swiftMessage);
        return swiftMessageMapper.toDto(swiftMessage);
    }

    @Override
    public SwiftMessageDto convertMtToMx(Long id) {
        SwiftMessage swiftMessage = swiftMessageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        
        String rawMtMessage = swiftMessage.getRawMtMessage();
        if (rawMtMessage == null || rawMtMessage.isBlank()) {
            throw new RuntimeException("No MT message content found for id: " + id);
        }
        
        return convertMtToMx(id, rawMtMessage);
    }

    @Override
    public SwiftMessageDto convertMtToMx(Long id, String rawMtMessage) {
        SwiftMessage swiftMessage = swiftMessageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        
        log.debug("Received rawMtMessage: '{}' for id: {}", rawMtMessage, id);
        log.debug("Existing rawMtMessage: '{}'", swiftMessage.getRawMtMessage());
        
        // If no new MT message provided, use existing one
        if (rawMtMessage == null || rawMtMessage.trim().isEmpty() || rawMtMessage.equals("\"\"") || rawMtMessage.equals("")) {
            log.debug("Using existing MT message");
            rawMtMessage = swiftMessage.getRawMtMessage();
            if (rawMtMessage == null || rawMtMessage.isBlank()) {
                throw new RuntimeException("No MT message content found for id: " + id);
            }
        } else {
            log.debug("Using new MT message");
            swiftMessage.setRawMtMessage(rawMtMessage);
        }
        
        MessageType messageType = determineMessageType(rawMtMessage);
        swiftMessage.setMessageType(messageType);
        
        try {
            // Convert MT to MX using ConversionService
            String mxMessage = conversionService.convertMtToMx(rawMtMessage);
            
            if (mxMessage != null && !mxMessage.contains("<error>")) {
                // XSD doğrulaması yap
                XsdValidationService.ValidationResult validationResult = 
                    xsdValidationService.validateByMtType(mxMessage, messageType.name());
                
                if (validationResult.isValid()) {
                    swiftMessage.setGeneratedMxMessage(mxMessage);
                    log.info("Successfully converted and validated {} message to MX format", messageType);
                } else {
                    swiftMessage.setGeneratedMxMessage(mxMessage + 
                        "\n<!-- XSD Validation Warning: " + validationResult.getMessage() + " -->");
                    log.warn("MX message generated but XSD validation failed: {}", validationResult.getMessage());
                }
            } else {
                swiftMessage.setGeneratedMxMessage(mxMessage != null ? mxMessage : "<error>Conversion Failed</error>");
                log.warn("Conversion failed for message type: {}", messageType);
            }
            
        } catch (Exception e) {
            log.error("Failed to convert MT to MX: {}", e.getMessage(), e);
            swiftMessage.setGeneratedMxMessage("<error>Conversion Failed: " + e.getMessage() + "</error>");
        }
        
        swiftMessage = swiftMessageRepository.save(swiftMessage);
        return swiftMessageMapper.toDto(swiftMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SwiftMessageDto> findAll(Pageable pageable) {
        return swiftMessageRepository.findAll(pageable)
                .map(swiftMessageMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SwiftMessageDto> findByMessageType(String messageType, Pageable pageable) {
        try {
            MessageType enumType = MessageType.valueOf(messageType);
            return swiftMessageRepository.findByMessageType(enumType, pageable).map(swiftMessageMapper::toDto);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid message type requested: {}", messageType);
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SwiftMessageDto> findOne(Long id) {
        return swiftMessageRepository.findById(id).map(swiftMessageMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting SWIFT message with id: {}", id);
        swiftMessageRepository.deleteById(id);
    }

    @Override
    public SwiftMessageDto convertMxToMt(Long id) {
        SwiftMessage swiftMessage = swiftMessageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        
        String mxMessage = swiftMessage.getGeneratedMxMessage();
        if (mxMessage == null || mxMessage.isBlank()) {
            throw new RuntimeException("No MX message content found for id: " + id + ". Please convert MT to MX first.");
        }
        
        // Convert MX back to MT
        String convertedMtMessage = conversionService.convertMxToMt(mxMessage);
        
        // Update the message with converted content
        swiftMessage.setRawMtMessage(convertedMtMessage);
        SwiftMessage savedMessage = swiftMessageRepository.save(swiftMessage);
        
        return swiftMessageMapper.toDto(savedMessage);
    }

    // Private helper method
    private MessageType determineMessageType(String rawMtMessage) {
        try {
            // Use ConversionService to determine message type
            String messageType = conversionService.getMessageType(rawMtMessage);
            if (messageType == null) {
                log.warn("Message type is null, defaulting to MT103");
                return MessageType.MT103;
            }
            
            switch (messageType) {
                case "102":
                    return MessageType.MT102;
                case "103":
                    return MessageType.MT103;
                case "202":
                    return MessageType.MT202;
                case "202COV":
                    return MessageType.MT202COV;
                case "203":
                    return MessageType.MT203;
                default:
                    log.warn("Unknown message type: {}, defaulting to MT103", messageType);
                    return MessageType.MT103;
            }
        } catch (Exception e) {
            log.error("Error determining message type: {}", e.getMessage());
            return MessageType.MT103;
        }
    }

    @Override
    public SwiftMessageDto updateXmlContent(Long id, String xmlContent) {
        SwiftMessage swiftMessage = swiftMessageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        
        // Validate XML format
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new RuntimeException("XML content cannot be empty");
        }
        
        try {
            // Basic XML validation
            javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new java.io.ByteArrayInputStream(xmlContent.getBytes()));
            
            // Update the generated MX message
            swiftMessage.setGeneratedMxMessage(xmlContent);
            
            // Try to validate against XSD if possible
            try {
                XsdValidationService.ValidationResult validationResult = 
                    xsdValidationService.validateByMtType(xmlContent, swiftMessage.getMessageType().name());
                
                if (!validationResult.isValid()) {
                    log.warn("Updated XML failed XSD validation: {}", validationResult.getMessage());
                    swiftMessage.setGeneratedMxMessage(xmlContent + 
                        "\n<!-- XSD Validation Warning: " + validationResult.getMessage() + " -->");
                }
            } catch (Exception e) {
                log.warn("Could not validate updated XML against XSD: {}", e.getMessage());
            }
            
            // *** YENİ ÖZELLİK: MX güncellendiğinde MT'ye çevir ***
            try {
                log.info("Converting updated MX back to MT format for message id: {}", id);
                String convertedMtMessage = conversionService.convertMxToMt(xmlContent);
                
                if (convertedMtMessage != null && !convertedMtMessage.contains("FALLBACK")) {
                    swiftMessage.setRawMtMessage(convertedMtMessage);
                    log.info("Successfully converted updated MX to MT format");
                } else {
                    log.warn("MX to MT conversion returned fallback message, keeping original MT");
                }
            } catch (Exception e) {
                log.error("Failed to convert updated MX to MT: {}", e.getMessage());
                // MX güncellemesi başarılı olsa bile, MT çevirme başarısız olursa devam et
                // Sadece log'la, exception fırlatma
            }
            
            SwiftMessage savedMessage = swiftMessageRepository.save(swiftMessage);
            return swiftMessageMapper.toDto(savedMessage);
            
        } catch (Exception e) {
            log.error("Invalid XML content: {}", e.getMessage());
            throw new RuntimeException("Invalid XML format: " + e.getMessage());
        }
    }
}
