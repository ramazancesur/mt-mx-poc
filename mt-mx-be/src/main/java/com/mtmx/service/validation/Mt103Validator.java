package com.mtmx.service.validation;

import com.mtmx.domain.entity.SwiftMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * MT103 - Single Customer Credit Transfer validasyonu
 * Maksimum 240 satır ve 6 method sınırı içinde
 */
@Component
@Slf4j
public class Mt103Validator {

    /**
     * MT103 spesifik validasyonları
     */
    public void validateMt103(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT103 specific rules");
        
        validateMt103Amount(message, result);
        validateMt103MandatoryFields(message, result);
        validateMt103Charges(message, result);
    }

    /**
     * MT103 amount kontrolü
     */
    private void validateMt103Amount(SwiftMessage message, ValidationResult result) {
        if (message.getAmount() != null && 
            message.getAmount().compareTo(new BigDecimal("999999999999.99")) > 0) {
            result.addError("MT103 için maksimum amount 999,999,999,999.99 olmalıdır");
        }
    }

    /**
     * MT103 zorunlu alanları kontrolü
     */
    private void validateMt103MandatoryFields(SwiftMessage message, ValidationResult result) {
        String rawMessage = message.getRawMtMessage();
        if (rawMessage == null) return;

        checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
        checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
        checkMandatoryField(rawMessage, ":50A:", "Ordering Customer", result);
        checkMandatoryField(rawMessage, ":59A:", "Beneficiary Customer", result);
        checkMandatoryField(rawMessage, ":71A:", "Details of Charges", result);
    }

    /**
     * MT103 charges kontrolü
     */
    private void validateMt103Charges(SwiftMessage message, ValidationResult result) {
        String rawMessage = message.getRawMtMessage();
        if (rawMessage != null && rawMessage.contains(":71A:")) {
            String charges = extractField(rawMessage, ":71A:");
            if (charges != null && !charges.matches("^(BEN|OUR|SHA)$")) {
                result.addError("Details of Charges (:71A:) geçersiz - BEN, OUR veya SHA olmalı");
            }
        }
    }

    /**
     * Zorunlu alan kontrolü
     */
    private void checkMandatoryField(String rawMessage, String fieldTag, 
                                   String fieldName, ValidationResult result) {
        if (!rawMessage.contains(fieldTag)) {
            result.addError("Zorunlu alan eksik: " + fieldName + " (" + fieldTag + ")");
        }
    }

    /**
     * Alan çıkarma yardımcı metodu
     */
    private String extractField(String message, String fieldTag) {
        int startIndex = message.indexOf(fieldTag);
        if (startIndex == -1) return null;
        
        startIndex += fieldTag.length();
        int endIndex = message.indexOf("\n:", startIndex);
        if (endIndex == -1) {
            endIndex = message.indexOf("-}", startIndex);
        }
        if (endIndex == -1) {
            endIndex = message.length();
        }
        
        return message.substring(startIndex, endIndex).trim();
    }
}
