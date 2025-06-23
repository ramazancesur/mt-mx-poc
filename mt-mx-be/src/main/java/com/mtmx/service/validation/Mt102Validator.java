package com.mtmx.service.validation;

import com.mtmx.domain.entity.SwiftMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * MT102 - Multiple Customer Credit Transfer validasyonu
 * Maksimum 240 satır ve 6 method sınırı içinde
 */
@Component
@Slf4j
public class Mt102Validator {

    /**
     * MT102 spesifik validasyonları
     */
    public void validateMt102(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT102 specific rules");
        
        validateMt102Amount(message, result);
        validateMt102TransactionCount(message, result);
        validateMt102MandatoryFields(message, result);
    }

    /**
     * MT102 amount kontrolü
     */
    private void validateMt102Amount(SwiftMessage message, ValidationResult result) {
        if (message.getAmount() != null && 
            message.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            result.addError("MT102 için minimum amount 0.01 olmalıdır");
        }
    }

    /**
     * MT102 transaction sayısı kontrolü
     */
    private void validateMt102TransactionCount(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null) {
            long transactionCount = message.getRawMtMessage().split(":21:").length - 1;
            if (transactionCount > 50) {
                result.addError("MT102 maksimum 50 transaction içerebilir");
            }
        }
    }

    /**
     * MT102 zorunlu alanları kontrolü
     */
    private void validateMt102MandatoryFields(SwiftMessage message, ValidationResult result) {
        String rawMessage = message.getRawMtMessage();
        if (rawMessage == null) return;

        checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
        checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
        checkMandatoryField(rawMessage, ":50A:", "Ordering Customer", result);
        checkMandatoryField(rawMessage, ":58A:", "Beneficiary Institution", result);
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
}
