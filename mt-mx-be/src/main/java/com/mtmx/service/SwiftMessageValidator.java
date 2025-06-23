package com.mtmx.service;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.repository.SwiftMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * SWIFT MT mesajları için kapsamlı validasyon servisi
 * MT102, MT103, MT202, MT202COV ve MT203 mesajları için detaylı kurallar içerir
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SwiftMessageValidator {

    private final SwiftMessageRepository swiftMessageRepository;

    // SWIFT format desenleri
    private static final Pattern BIC_PATTERN = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("^[A-Z0-9]{1,34}$");
    private static final Pattern CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d{1,12}(,\\d{1,2})?$");
    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ ]{1,16}$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{6}$"); // YYMMDD
    private static final Pattern NARRATIVE_PATTERN = Pattern.compile("^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ \\r\\n]{1,210}$");

    /**
     * Ana validasyon metodu - mesajı validate eder ve hatasız ise kaydeder
     */
    public ValidationResult validateAndSave(SwiftMessage message) {
        log.info("Validating and saving SWIFT message of type: {}", message.getMessageType());
        
        ValidationResult result = new ValidationResult();
        
        try {
            // 1. Temel validasyonlar
            validateBasicFields(message, result);
            
            // 2. Mesaj tipine özel validasyonlar
            switch (message.getMessageType()) {
                case MT102:
                    validateMT102(message, result);
                    break;
                case MT103:
                    validateMT103(message, result);
                    break;
                case MT202:
                    validateMT202(message, result);
                    break;
                case MT202COV:
                    validateMT202COV(message, result);
                    break;
                case MT203:
                    validateMT203(message, result);
                    break;
                default:
                    result.addError("Desteklenmeyen mesaj tipi: " + message.getMessageType());
            }
            
            // 3. Raw MT mesaj validasyonu
            if (StringUtils.hasText(message.getRawMtMessage())) {
                validateRawMtMessage(message.getRawMtMessage(), message.getMessageType(), result);
            }
            
            // 4. Eğer hata yoksa kaydet
            if (result.isValid()) {
                SwiftMessage savedMessage = swiftMessageRepository.save(message);
                result.setSavedMessage(savedMessage);
                log.info("SWIFT message successfully validated and saved with ID: {}", savedMessage.getId());
            } else {
                log.warn("SWIFT message validation failed with {} errors", result.getErrors().size());
            }
            
        } catch (Exception e) {
            log.error("Error during validation and save: {}", e.getMessage(), e);
            result.addError("Validation sırasında hata oluştu: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Temel alan validasyonları
     */
    private void validateBasicFields(SwiftMessage message, ValidationResult result) {
        // Message Type zorunlu
        if (message.getMessageType() == null) {
            result.addError("Message type zorunludur");
        }
        
        // Sender BIC validasyonu
        if (!StringUtils.hasText(message.getSenderBic())) {
            result.addError("Sender BIC zorunludur");
        } else if (!BIC_PATTERN.matcher(message.getSenderBic()).matches()) {
            result.addError("Geçersiz Sender BIC formatı: " + message.getSenderBic());
        }
        
        // Receiver BIC validasyonu
        if (!StringUtils.hasText(message.getReceiverBic())) {
            result.addError("Receiver BIC zorunludur");
        } else if (!BIC_PATTERN.matcher(message.getReceiverBic()).matches()) {
            result.addError("Geçersiz Receiver BIC formatı: " + message.getReceiverBic());
        }
        
        // Amount validasyonu
        if (message.getAmount() == null) {
            result.addError("Amount zorunludur");
        } else if (message.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            result.addError("Amount sıfırdan büyük olmalıdır");
        } else if (message.getAmount().scale() > 2) {
            result.addError("Amount en fazla 2 ondalık basamak içerebilir");
        }
        
        // Currency validasyonu
        if (!StringUtils.hasText(message.getCurrency())) {
            result.addError("Currency zorunludur");
        } else if (!CURRENCY_PATTERN.matcher(message.getCurrency()).matches()) {
            result.addError("Geçersiz currency formatı: " + message.getCurrency());
        }
        
        // Value Date validasyonu
        if (message.getValueDate() == null) {
            result.addError("Value date zorunludur");
        } else if (message.getValueDate().isBefore(LocalDate.now().minusYears(1))) {
            result.addError("Value date çok eskidir");
        } else if (message.getValueDate().isAfter(LocalDate.now().plusYears(1))) {
            result.addError("Value date çok ileridedir");
        }
        
        // Raw MT Message zorunlu
        if (!StringUtils.hasText(message.getRawMtMessage())) {
            result.addError("Raw MT message zorunludur");
        }
    }

    /**
     * MT102 - Multiple Customer Credit Transfer validasyonu
     */
    private void validateMT102(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT102 specific rules");
        
        // MT102 özel kuralları
        validateTransactionReference(message, result);
        validateOrderingCustomer(message, result);
        validateBeneficiaryCustomer(message, result);
        validateRemittanceInfo(message, result);
        
        // MT102 için minimum amount kontrolü
        if (message.getAmount() != null && message.getAmount().compareTo(new BigDecimal("0.01")) < 0) {
            result.addError("MT102 için minimum amount 0.01 olmalıdır");
        }
        
        // MT102 için maksimum transaction sayısı kontrolü (genellikle 50)
        if (message.getRawMtMessage() != null) {
            long transactionCount = message.getRawMtMessage().split(":21:").length - 1;
            if (transactionCount > 50) {
                result.addError("MT102 maksimum 50 transaction içerebilir");
            }
        }
    }

    /**
     * MT103 - Single Customer Credit Transfer validasyonu
     */
    private void validateMT103(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT103 specific rules");
        
        // MT103 zorunlu alanları
        validateTransactionReference(message, result);
        validateOrderingCustomer(message, result);
        validateBeneficiaryCustomer(message, result);
        validateRemittanceInfo(message, result);
        validateChargesInformation(message, result);
        
        // MT103 için regulatory reporting kontrolü
        validateRegulatoryReporting(message, result);
        
        // MT103 için maksimum amount kontrolü
        if (message.getAmount() != null && message.getAmount().compareTo(new BigDecimal("999999999999.99")) > 0) {
            result.addError("MT103 için maksimum amount 999,999,999,999.99 olmalıdır");
        }
    }

    /**
     * MT202 - General Financial Institution Transfer validasyonu
     */
    private void validateMT202(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT202 specific rules");
        
        // MT202 zorunlu alanları
        validateTransactionReference(message, result);
        validateCorrespondentBanks(message, result);
        validateSettlementInstructions(message, result);
        
        // MT202 için interbank settlement kontrolü
        validateInterbankSettlement(message, result);
        
        // MT202 için minimum amount kontrolü (genellikle daha yüksek)
        if (message.getAmount() != null && message.getAmount().compareTo(new BigDecimal("1000.00")) < 0) {
            result.addError("MT202 için minimum amount 1,000.00 olmalıdır");
        }
    }

    /**
     * MT202COV - General Financial Institution Transfer with Cover validasyonu
     */
    private void validateMT202COV(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT202COV specific rules");
        
        // MT202COV, MT202'nin tüm kurallarını içerir
        validateMT202(message, result);
        
        // Ek olarak cover payment özel kuralları
        validateCoverPaymentInfo(message, result);
        validateUnderlyingTransaction(message, result);
        
        // Cover payment için özel narrative kontrolü
        if (message.getRawMtMessage() != null && !message.getRawMtMessage().contains(":72:")) {
            result.addError("MT202COV için sender to receiver information (:72:) zorunludur");
        }
    }

    /**
     * MT203 - Multiple General Financial Institution Transfer validasyonu
     */
    private void validateMT203(SwiftMessage message, ValidationResult result) {
        log.debug("Validating MT203 specific rules");
        
        // MT203, MT202'nin çoklu versiyonu
        validateMT202(message, result);
        
        // Multiple transfer kontrolü
        if (message.getRawMtMessage() != null) {
            long transactionCount = message.getRawMtMessage().split(":21:").length - 1;
            if (transactionCount < 2) {
                result.addError("MT203 en az 2 transaction içermelidir");
            }
            if (transactionCount > 100) {
                result.addError("MT203 maksimum 100 transaction içerebilir");
            }
        }
        
        // MT203 için total amount kontrolü
        validateTotalAmount(message, result);
    }

    /**
     * Raw MT mesaj formatı validasyonu
     */
    private void validateRawMtMessage(String rawMessage, MessageType messageType, ValidationResult result) {
        // SWIFT mesaj yapısı kontrolü
        if (!rawMessage.contains("{1:") || !rawMessage.contains("{2:") || !rawMessage.contains("{4:")) {
            result.addError("Geçersiz SWIFT mesaj formatı - gerekli bloklar eksik");
        }
        
        // Message type consistency kontrolü
        String expectedType = messageType.name().substring(2); // MT103 -> 103
        if (messageType == MessageType.MT202COV) {
            expectedType = "202COV";
        }
        
        if (!rawMessage.contains("I" + expectedType) && !rawMessage.contains("O" + expectedType)) {
            result.addError("Mesaj tipi tutarsızlığı - beklenen: " + expectedType);
        }
        
        // Mandatory fields kontrolü
        validateMandatoryFields(rawMessage, messageType, result);
    }

    /**
     * Zorunlu alanların varlığını kontrol eder
     */
    private void validateMandatoryFields(String rawMessage, MessageType messageType, ValidationResult result) {
        switch (messageType) {
            case MT102:
                checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
                checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
                checkMandatoryField(rawMessage, ":50A:", "Ordering Customer", result);
                checkMandatoryField(rawMessage, ":59A:", "Beneficiary Customer", result);
                break;
                
            case MT103:
                checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
                checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
                checkMandatoryField(rawMessage, ":50A:", "Ordering Customer", result);
                checkMandatoryField(rawMessage, ":59A:", "Beneficiary Customer", result);
                checkMandatoryField(rawMessage, ":71A:", "Details of Charges", result);
                break;
                
            case MT202:
                checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
                checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
                checkMandatoryField(rawMessage, ":52A:", "Ordering Institution", result);
                checkMandatoryField(rawMessage, ":58A:", "Beneficiary Institution", result);
                break;
                
            case MT202COV:
                checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
                checkMandatoryField(rawMessage, ":21:", "Related Reference", result);
                checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
                checkMandatoryField(rawMessage, ":50A:", "Ordering Customer", result);
                checkMandatoryField(rawMessage, ":59A:", "Beneficiary Customer", result);
                checkMandatoryField(rawMessage, ":72:", "Sender to Receiver Info", result);
                break;
                
            case MT203:
                checkMandatoryField(rawMessage, ":20:", "Transaction Reference", result);
                checkMandatoryField(rawMessage, ":32A:", "Value Date/Currency/Amount", result);
                checkMandatoryField(rawMessage, ":52A:", "Ordering Institution", result);
                checkMandatoryField(rawMessage, ":58A:", "Beneficiary Institution", result);
                break;
        }
    }

    private void checkMandatoryField(String rawMessage, String fieldTag, String fieldName, ValidationResult result) {
        if (!rawMessage.contains(fieldTag)) {
            result.addError("Zorunlu alan eksik: " + fieldName + " (" + fieldTag + ")");
        }
    }

    // Yardımcı validasyon metodları
    private void validateTransactionReference(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null) {
            // :20: Transaction Reference zorunlu ve 16 karakter max
            if (message.getRawMtMessage().contains(":20:")) {
                String field20 = extractField(message.getRawMtMessage(), ":20:");
                if (field20 != null) {
                    if (field20.length() > 16) {
                        result.addError("Transaction Reference (:20:) 16 karakterden uzun olamaz");
                    }
                    if (!REFERENCE_PATTERN.matcher(field20).matches()) {
                        result.addError("Transaction Reference (:20:) geçersiz karakterler içeriyor");
                    }
                }
            }
        }
    }

    private void validateOrderingCustomer(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null) {
            // :50A:, :50F:, :50K: alanlarından biri olmalı
            boolean hasOrderingCustomer = message.getRawMtMessage().contains(":50A:") ||
                                        message.getRawMtMessage().contains(":50F:") ||
                                        message.getRawMtMessage().contains(":50K:");
            
            if (!hasOrderingCustomer) {
                result.addError("Ordering Customer (:50A:/:50F:/:50K:) alanlarından biri zorunludur");
            }
            
            // :50A: BIC kontrolü
            if (message.getRawMtMessage().contains(":50A:")) {
                String field50A = extractField(message.getRawMtMessage(), ":50A:");
                if (field50A != null && field50A.length() >= 8) {
                    String bic = field50A.substring(0, Math.min(11, field50A.length()));
                    if (!BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Ordering Customer BIC (:50A:) geçersiz format");
                    }
                }
            }
        }
    }

    private void validateBeneficiaryCustomer(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null) {
            // :59A:, :59F:, :59: alanlarından biri olmalı
            boolean hasBeneficiary = message.getRawMtMessage().contains(":59A:") ||
                                   message.getRawMtMessage().contains(":59F:") ||
                                   message.getRawMtMessage().contains(":59:");
            
            if (!hasBeneficiary) {
                result.addError("Beneficiary Customer (:59A:/:59F:/:59:) alanlarından biri zorunludur");
            }
            
            // :59A: BIC kontrolü
            if (message.getRawMtMessage().contains(":59A:")) {
                String field59A = extractField(message.getRawMtMessage(), ":59A:");
                if (field59A != null && field59A.length() >= 8) {
                    String bic = field59A.substring(0, Math.min(11, field59A.length()));
                    if (!BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Beneficiary Customer BIC (:59A:) geçersiz format");
                    }
                }
            }
            
            // :59: hesap numarası kontrolü
            if (message.getRawMtMessage().contains(":59:")) {
                String field59 = extractField(message.getRawMtMessage(), ":59:");
                if (field59 != null && field59.startsWith("/")) {
                    String account = field59.substring(1, field59.indexOf('\n') > 0 ? field59.indexOf('\n') : field59.length());
                    if (account.length() > 34) {
                        result.addError("Beneficiary Account Number (:59:) 34 karakterden uzun olamaz");
                    }
                }
            }
        }
    }

    private void validateRemittanceInfo(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null && message.getRawMtMessage().contains(":70:")) {
            String field70 = extractField(message.getRawMtMessage(), ":70:");
            if (field70 != null) {
                // :70: maksimum 4 satır, her satır 35 karakter
                String[] lines = field70.split("\\r?\\n");
                if (lines.length > 4) {
                    result.addError("Remittance Information (:70:) maksimum 4 satır içerebilir");
                }
                for (String line : lines) {
                    if (line.length() > 35) {
                        result.addError("Remittance Information (:70:) her satır 35 karakterden uzun olamaz");
                    }
                }
            }
        }
    }

    private void validateChargesInformation(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null) {
            // :71A: Details of Charges - MT103 için zorunlu
            if (message.getMessageType() == MessageType.MT103) {
                if (!message.getRawMtMessage().contains(":71A:")) {
                    result.addError("Details of Charges (:71A:) MT103 için zorunludur");
                } else {
                    String field71A = extractField(message.getRawMtMessage(), ":71A:");
                    if (field71A != null) {
                        // Geçerli charge codes: BEN, OUR, SHA
                        if (!field71A.matches("^(BEN|OUR|SHA)$")) {
                            result.addError("Details of Charges (:71A:) geçersiz kod - BEN, OUR veya SHA olmalı");
                        }
                    }
                }
            }
            
            // :71F: Sender's Charges kontrolü
            if (message.getRawMtMessage().contains(":71F:")) {
                String field71F = extractField(message.getRawMtMessage(), ":71F:");
                if (field71F != null) {
                    // Currency ve amount formatı kontrolü
                    if (!field71F.matches("^[A-Z]{3}\\d+,?\\d*$")) {
                        result.addError("Sender's Charges (:71F:) geçersiz format - CUR1234,56 formatında olmalı");
                    }
                }
            }
        }
    }

    private void validateRegulatoryReporting(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null && message.getRawMtMessage().contains(":77B:")) {
            String field77B = extractField(message.getRawMtMessage(), ":77B:");
            if (field77B != null) {
                // :77B: maksimum 3 satır, her satır 35 karakter
                String[] lines = field77B.split("\\r?\\n");
                if (lines.length > 3) {
                    result.addError("Regulatory Reporting (:77B:) maksimum 3 satır içerebilir");
                }
                for (String line : lines) {
                    if (line.length() > 35) {
                        result.addError("Regulatory Reporting (:77B:) her satır 35 karakterden uzun olamaz");
                    }
                }
                
                // Regulatory reporting kodları kontrolü (/ORDERRES/, /BENEFRES/ vb.)
                if (!field77B.contains("/ORDERRES/") && !field77B.contains("/BENEFRES/")) {
                    result.addWarning("Regulatory Reporting (:77B:) standart kodlar içermiyor");
                }
            }
        }
    }

    private void validateCorrespondentBanks(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null) {
            // :52A: Ordering Institution (MT202/MT203 için)
            if (message.getRawMtMessage().contains(":52A:")) {
                String field52A = extractField(message.getRawMtMessage(), ":52A:");
                if (field52A != null) {
                    String bic = extractBicFromField(field52A);
                    if (bic != null && !BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Ordering Institution BIC (:52A:) geçersiz format");
                    }
                }
            }
            
            // :53A: Sender's Correspondent
            if (message.getRawMtMessage().contains(":53A:")) {
                String field53A = extractField(message.getRawMtMessage(), ":53A:");
                if (field53A != null) {
                    String bic = extractBicFromField(field53A);
                    if (bic != null && !BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Sender's Correspondent BIC (:53A:) geçersiz format");
                    }
                }
            }
            
            // :54A: Receiver's Correspondent
            if (message.getRawMtMessage().contains(":54A:")) {
                String field54A = extractField(message.getRawMtMessage(), ":54A:");
                if (field54A != null) {
                    String bic = extractBicFromField(field54A);
                    if (bic != null && !BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Receiver's Correspondent BIC (:54A:) geçersiz format");
                    }
                }
            }
            
            // :56A: Intermediary
            if (message.getRawMtMessage().contains(":56A:")) {
                String field56A = extractField(message.getRawMtMessage(), ":56A:");
                if (field56A != null) {
                    String bic = extractBicFromField(field56A);
                    if (bic != null && !BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Intermediary BIC (:56A:) geçersiz format");
                    }
                }
            }
            
            // :57A: Account With Institution
            if (message.getRawMtMessage().contains(":57A:")) {
                String field57A = extractField(message.getRawMtMessage(), ":57A:");
                if (field57A != null) {
                    String bic = extractBicFromField(field57A);
                    if (bic != null && !BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Account With Institution BIC (:57A:) geçersiz format");
                    }
                }
            }
            
            // :58A: Beneficiary Institution (MT202/MT203 için)
            if (message.getRawMtMessage().contains(":58A:")) {
                String field58A = extractField(message.getRawMtMessage(), ":58A:");
                if (field58A != null) {
                    String bic = extractBicFromField(field58A);
                    if (bic != null && !BIC_PATTERN.matcher(bic).matches()) {
                        result.addError("Beneficiary Institution BIC (:58A:) geçersiz format");
                    }
                }
            }
        }
    }

    private void validateSettlementInstructions(SwiftMessage message, ValidationResult result) {
        if (message.getRawMtMessage() != null && message.getRawMtMessage().contains(":72:")) {
            String field72 = extractField(message.getRawMtMessage(), ":72:");
            if (field72 != null) {
                // :72: maksimum 6 satır, her satır 35 karakter
                String[] lines = field72.split("\\r?\\n");
                if (lines.length > 6) {
                    result.addError("Sender to Receiver Information (:72:) maksimum 6 satır içerebilir");
                }
                for (String line : lines) {
                    if (line.length() > 35) {
                        result.addError("Sender to Receiver Information (:72:) her satır 35 karakterden uzun olamaz");
                    }
                }
                
                // Settlement instruction kodları kontrolü
                if (field72.contains("/INS/") || field72.contains("/ACC/") || field72.contains("/INT/")) {
                    // Geçerli instruction kodları var
                } else {
                    result.addWarning("Settlement Instructions (:72:) standart kodlar içermiyor");
                }
            }
        }
    }

    private void validateInterbankSettlement(SwiftMessage message, ValidationResult result) {
        // Interbank settlement amount ve date kontrolü
        if (message.getRawMtMessage() != null && message.getRawMtMessage().contains(":32A:")) {
            String field32A = extractField(message.getRawMtMessage(), ":32A:");
            if (field32A != null && field32A.length() >= 9) {
                // Date format kontrolü (YYMMDD)
                String dateStr = field32A.substring(0, 6);
                if (!DATE_PATTERN.matcher(dateStr).matches()) {
                    result.addError("Value Date (:32A:) geçersiz format - YYMMDD olmalı");
                } else {
                    try {
                        LocalDate.parse("20" + dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                    } catch (DateTimeParseException e) {
                        result.addError("Value Date (:32A:) geçersiz tarih");
                    }
                }
                
                // Currency kontrolü
                if (field32A.length() >= 9) {
                    String currency = field32A.substring(6, 9);
                    if (!CURRENCY_PATTERN.matcher(currency).matches()) {
                        result.addError("Currency (:32A:) geçersiz format");
                    }
                }
                
                // Amount kontrolü
                if (field32A.length() > 9) {
                    String amount = field32A.substring(9);
                    if (!AMOUNT_PATTERN.matcher(amount).matches()) {
                        result.addError("Amount (:32A:) geçersiz format");
                    }
                }
            }
        }
    }

    private void validateCoverPaymentInfo(SwiftMessage message, ValidationResult result) {
        // MT202COV özel kontrolleri
        if (message.getMessageType() == MessageType.MT202COV) {
            // :21: Related Reference zorunlu
            if (message.getRawMtMessage() != null && !message.getRawMtMessage().contains(":21:")) {
                result.addError("Related Reference (:21:) MT202COV için zorunludur");
            }
            
            // Cover payment için underlying customer credit transfer bilgileri
            boolean hasUnderlyingInfo = message.getRawMtMessage().contains(":50A:") || 
                                      message.getRawMtMessage().contains(":50F:") ||
                                      message.getRawMtMessage().contains(":50K:");
            
            if (!hasUnderlyingInfo) {
                result.addError("MT202COV underlying customer bilgileri (:50A:/:50F:/:50K:) zorunludur");
            }
        }
    }

    private void validateUnderlyingTransaction(SwiftMessage message, ValidationResult result) {
        // Underlying transaction kontrolü (MT202COV için)
        if (message.getMessageType() == MessageType.MT202COV && message.getRawMtMessage() != null) {
            // :33B: Original Ordered Amount kontrolü
            if (message.getRawMtMessage().contains(":33B:")) {
                String field33B = extractField(message.getRawMtMessage(), ":33B:");
                if (field33B != null && field33B.length() >= 3) {
                    String currency = field33B.substring(0, 3);
                    if (!CURRENCY_PATTERN.matcher(currency).matches()) {
                        result.addError("Original Ordered Currency (:33B:) geçersiz format");
                    }
                    
                    if (field33B.length() > 3) {
                        String amount = field33B.substring(3);
                        if (!AMOUNT_PATTERN.matcher(amount).matches()) {
                            result.addError("Original Ordered Amount (:33B:) geçersiz format");
                        }
                    }
                }
            }
        }
    }

    private void validateTotalAmount(SwiftMessage message, ValidationResult result) {
        // MT203 için total amount kontrolü
        if (message.getMessageType() == MessageType.MT203 && message.getRawMtMessage() != null) {
            // Tüm :32A: alanlarını topla ve kontrol et
            String[] field32As = message.getRawMtMessage().split(":32A:");
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (int i = 1; i < field32As.length; i++) {
                String field32A = field32As[i];
                if (field32A.length() > 9) {
                    try {
                        String amountStr = field32A.substring(9);
                        // Satır sonu karakterine kadar al
                        int endIndex = amountStr.indexOf('\n');
                        if (endIndex > 0) {
                            amountStr = amountStr.substring(0, endIndex);
                        }
                        amountStr = amountStr.replace(",", ".");
                        totalAmount = totalAmount.add(new BigDecimal(amountStr));
                    } catch (NumberFormatException e) {
                        result.addError("MT203 amount parsing hatası: " + e.getMessage());
                    }
                }
            }
            
            // Total amount ile message amount karşılaştırması
            if (message.getAmount() != null && totalAmount.compareTo(message.getAmount()) != 0) {
                result.addWarning("MT203 total amount (" + totalAmount + ") ile message amount (" + message.getAmount() + ") uyuşmuyor");
            }
        }
    }

    // Yardımcı metodlar
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

    private String extractBicFromField(String fieldValue) {
        if (fieldValue == null || fieldValue.length() < 8) return null;
        
        // BIC genellikle alan başında bulunur
        String[] lines = fieldValue.split("\\r?\\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.length() >= 8) {
                return firstLine.substring(0, Math.min(11, firstLine.length()));
            }
        }
        return null;
    }

    /**
     * Validation sonucu sınıfı
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private SwiftMessage savedMessage;

        public void addError(String error) {
            errors.add(error);
        }

        public void addWarning(String warning) {
            warnings.add(warning);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }

        public SwiftMessage getSavedMessage() {
            return savedMessage;
        }

        public void setSavedMessage(SwiftMessage savedMessage) {
            this.savedMessage = savedMessage;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ValidationResult{");
            sb.append("valid=").append(isValid());
            if (!errors.isEmpty()) {
                sb.append(", errors=").append(errors);
            }
            if (!warnings.isEmpty()) {
                sb.append(", warnings=").append(warnings);
            }
            sb.append("}");
            return sb.toString();
        }
    }
} 