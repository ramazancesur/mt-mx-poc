package com.mtmx.service.validation;

import com.mtmx.domain.entity.SwiftMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * SWIFT mesajları için temel alan validasyonları
 * Tüm mesaj tiplerinde ortak olan validasyon kuralları
 */
@Component
@Slf4j
public class BasicFieldValidator {

    // SWIFT format desenleri
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");

    /**
     * Temel alan validasyonları
     */
    public void validateBasicFields(SwiftMessage message, ValidationResult result) {
        log.debug("Validating basic fields for message type: {}", message.getMessageType());
        
        validateMessageType(message, result);
        validateSenderBic(message, result);
        validateReceiverBic(message, result);
        validateAmount(message, result);
        validateCurrency(message, result);
        validateValueDate(message, result);
        validateRawMtMessage(message, result);
    }

    /**
     * Message Type validasyonu
     */
    private void validateMessageType(SwiftMessage message, ValidationResult result) {
        if (message.getMessageType() == null) {
            result.addError("Message type zorunludur");
        }
    }

    /**
     * Sender BIC validasyonu
     */
    private void validateSenderBic(SwiftMessage message, ValidationResult result) {
        if (!StringUtils.hasText(message.getSenderBic())) {
            result.addError("Sender BIC zorunludur");
        } else if (!BIC_PATTERN.matcher(message.getSenderBic()).matches()) {
            result.addError("Geçersiz Sender BIC formatı: " + message.getSenderBic());
        }
    }

    /**
     * Receiver BIC validasyonu
     */
    private void validateReceiverBic(SwiftMessage message, ValidationResult result) {
        if (!StringUtils.hasText(message.getReceiverBic())) {
            result.addError("Receiver BIC zorunludur");
        } else if (!BIC_PATTERN.matcher(message.getReceiverBic()).matches()) {
            result.addError("Geçersiz Receiver BIC formatı: " + message.getReceiverBic());
        }
    }

    /**
     * Amount validasyonu
     */
    private void validateAmount(SwiftMessage message, ValidationResult result) {
        if (message.getAmount() == null) {
            result.addError("Amount zorunludur");
        } else if (message.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError("Amount sıfırdan büyük olmalıdır");
        } else if (message.getAmount().scale() > 2) {
            result.addError("Amount en fazla 2 ondalık basamak içerebilir");
        }
    }

    /**
     * Currency validasyonu
     */
    private void validateCurrency(SwiftMessage message, ValidationResult result) {
        if (!StringUtils.hasText(message.getCurrency())) {
            result.addError("Currency zorunludur");
        } else if (!CURRENCY_PATTERN.matcher(message.getCurrency()).matches()) {
            result.addError("Geçersiz currency formatı: " + message.getCurrency());
        }
    }

    /**
     * Value Date validasyonu
     */
    private void validateValueDate(SwiftMessage message, ValidationResult result) {
        if (message.getValueDate() == null) {
            result.addError("Value date zorunludur");
        } else if (message.getValueDate().isBefore(LocalDate.now().minusYears(1))) {
            result.addError("Value date çok eskidir");
        } else if (message.getValueDate().isAfter(LocalDate.now().plusYears(1))) {
            result.addError("Value date çok ileridedir");
        }
    }

    /**
     * Raw MT Message validasyonu
     */
    private void validateRawMtMessage(SwiftMessage message, ValidationResult result) {
        if (!StringUtils.hasText(message.getRawMtMessage())) {
            result.addError("Raw MT message zorunludur");
        }
    }

    /**
     * BIC format kontrolü
     */
    public boolean isValidBic(String bic) {
        return StringUtils.hasText(bic) && BIC_PATTERN.matcher(bic).matches();
    }

    /**
     * Currency format kontrolü
     */
    public boolean isValidCurrency(String currency) {
        return StringUtils.hasText(currency) && CURRENCY_PATTERN.matcher(currency).matches();
    }
}
