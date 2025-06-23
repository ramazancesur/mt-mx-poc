package com.mtmx.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * MT102 bireysel transfer detayları için model sınıfı
 * Her MT102 mesajı birden fazla transfer içerebilir
 * Veritabanı entity değil, sadece parsing ve validation için kullanılır
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt102Transaction {

    // MANDATORY FIELDS - Her transaction için zorunlu

    /**
     * :21: Transaction Reference Number
     * Her transfer için benzersiz referans
     * ZORUNLU ALAN
     */
    @NotBlank(message = "Transaction Reference (:21:) zorunludur")
    @Size(max = 16, message = "Transaction Reference (:21:) maksimum 16 karakter olabilir")
    @Pattern(regexp = "^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ ]+$", message = "Transaction Reference (:21:) geçersiz karakterler içeriyor")
    private String transactionReference;

    /**
     * :32B: Currency/Instructed Amount
     * Para birimi ve tutar
     * ZORUNLU ALANLAR
     */
    @NotBlank(message = "Currency (:32B:) zorunludur")
    @Size(min = 3, max = 3, message = "Currency (:32B:) 3 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency (:32B:) geçersiz format")
    private String currency;

    @NotNull(message = "Amount (:32B:) zorunludur")
    @DecimalMin(value = "0.01", message = "Amount (:32B:) pozitif olmalı")
    private BigDecimal amount;

    /**
     * :59/59A: Beneficiary Customer
     * Alıcı müşteri bilgileri
     * En az birinin bulunması zorunlu
     */
    @Size(min = 8, max = 11, message = "Beneficiary BIC (:59A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Beneficiary BIC (:59A:) geçersiz BIC formatı")
    private String beneficiaryBic;

    @Size(max = 34, message = "Beneficiary Account (:59:) maksimum 34 karakter olabilir")
    private String beneficiaryAccount;

    @Size(max = 140, message = "Beneficiary Name (:59:) maksimum 140 karakter olabilir")
    private String beneficiaryName;

    @Size(max = 210, message = "Beneficiary Address (:59:) maksimum 210 karakter olabilir")
    private String beneficiaryAddress;

    // OPTIONAL FIELDS - Null olabilir

    /**
     * :23E: Instruction Code
     * Talimat kodu (CHQB, HOLD, PHOB, TELB, etc.)
     * OPSİYONEL ALAN
     */
    @Size(max = 4, message = "Instruction Code (:23E:) maksimum 4 karakter olabilir")
    @Pattern(regexp = "^(CHQB|HOLD|PHOB|TELB|REPA|SDVA|INTC|PHON|TELI|TELE|PHOI|PHOT|REQU|URGP|OTHR)$", 
             message = "Instruction Code (:23E:) geçersiz kod")
    private String instructionCode;

    /**
     * :26T: Transaction Type Code
     * İşlem tipi kodu
     * OPSİYONEL ALAN
     */
    @Size(max = 3, message = "Transaction Type Code (:26T:) maksimum 3 karakter olabilir")
    private String transactionTypeCode;

    /**
     * :50A/50F/50K: Ordering Customer (Transaction level)
     * Transfer seviyesinde ordering customer
     * OPSİYONEL ALANLAR
     */
    @Size(min = 8, max = 11, message = "Ordering Customer BIC (:50A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Ordering Customer BIC (:50A:) geçersiz BIC formatı")
    private String orderingCustomerBic;

    @Size(max = 34, message = "Ordering Customer Account maksimum 34 karakter olabilir")
    private String orderingCustomerAccount;

    @Size(max = 140, message = "Ordering Customer Name maksimum 140 karakter olabilir")
    private String orderingCustomerName;

    /**
     * :57A/57C/57D: Account With Institution
     * Hesap sahibi kurum
     * OPSİYONEL ALANLAR
     */
    @Size(min = 8, max = 11, message = "Account With Institution BIC (:57A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Account With Institution BIC (:57A:) geçersiz BIC formatı")
    private String accountWithInstitutionBic;

    @Size(max = 35, message = "Account With Institution Account maksimum 35 karakter olabilir")
    private String accountWithInstitutionAccount;

    @Size(max = 140, message = "Account With Institution Name maksimum 140 karakter olabilir")
    private String accountWithInstitutionName;

    /**
     * :70: Remittance Information
     * Havale bilgileri (maksimum 4 satır, her satır 35 karakter)
     * OPSİYONEL ALAN
     */
    @Size(max = 140, message = "Remittance Information (:70:) maksimum 140 karakter olabilir")
    private String remittanceInformation;

    /**
     * :77B: Regulatory Reporting
     * Düzenleyici raporlama bilgileri
     * OPSİYONEL ALAN
     */
    @Size(max = 105, message = "Regulatory Reporting (:77B:) maksimum 105 karakter olabilir")
    private String regulatoryReporting;

    // Business methods

    /**
     * Beneficiary Customer alanlarından en az birinin dolu olup olmadığını kontrol eder
     */
    public boolean hasBeneficiary() {
        return (beneficiaryBic != null && !beneficiaryBic.trim().isEmpty()) ||
               (beneficiaryAccount != null && !beneficiaryAccount.trim().isEmpty()) ||
               (beneficiaryName != null && !beneficiaryName.trim().isEmpty());
    }

    /**
     * Transaction'ın temel geçerlilik kontrolü
     */
    public boolean isValid() {
        return transactionReference != null && !transactionReference.trim().isEmpty() &&
               currency != null && currency.length() == 3 &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               hasBeneficiary();
    }

    /**
     * Null güvenli string dönüşümü
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * SWIFT formatında transaction string oluşturur
     */
    public String toSwiftFormat() {
        StringBuilder sb = new StringBuilder();
        
        // Transaction Reference
        sb.append(":21:").append(transactionReference).append("\n");
        
        // Instruction Code (opsiyonel)
        if (instructionCode != null && !instructionCode.trim().isEmpty()) {
            sb.append(":23E:").append(instructionCode).append("\n");
        }
        
        // Transaction Type Code (opsiyonel)
        if (transactionTypeCode != null && !transactionTypeCode.trim().isEmpty()) {
            sb.append(":26T:").append(transactionTypeCode).append("\n");
        }
        
        // Currency and Amount
        sb.append(":32B:").append(currency).append(amount.toString().replace(".", ",")).append("\n");
        
        // Ordering Customer (transaction level - opsiyonel)
        if (orderingCustomerBic != null && !orderingCustomerBic.trim().isEmpty()) {
            sb.append(":50A:").append(orderingCustomerBic).append("\n");
        } else if (orderingCustomerName != null && !orderingCustomerName.trim().isEmpty()) {
            sb.append(":50K:").append(orderingCustomerName).append("\n");
        }
        
        // Account With Institution (opsiyonel)
        if (accountWithInstitutionBic != null && !accountWithInstitutionBic.trim().isEmpty()) {
            sb.append(":57A:").append(accountWithInstitutionBic).append("\n");
        } else if (accountWithInstitutionName != null && !accountWithInstitutionName.trim().isEmpty()) {
            sb.append(":57D:").append(accountWithInstitutionName).append("\n");
        }
        
        // Beneficiary Customer
        if (beneficiaryBic != null && !beneficiaryBic.trim().isEmpty()) {
            sb.append(":59A:").append(beneficiaryBic).append("\n");
        } else {
            sb.append(":59:");
            if (beneficiaryAccount != null && !beneficiaryAccount.trim().isEmpty()) {
                sb.append("/").append(beneficiaryAccount).append("\n");
            }
            if (beneficiaryName != null && !beneficiaryName.trim().isEmpty()) {
                sb.append(beneficiaryName).append("\n");
            }
            if (beneficiaryAddress != null && !beneficiaryAddress.trim().isEmpty()) {
                sb.append(beneficiaryAddress).append("\n");
            }
        }
        
        // Remittance Information (opsiyonel)
        if (remittanceInformation != null && !remittanceInformation.trim().isEmpty()) {
            sb.append(":70:").append(remittanceInformation).append("\n");
        }
        
        // Regulatory Reporting (opsiyonel)
        if (regulatoryReporting != null && !regulatoryReporting.trim().isEmpty()) {
            sb.append(":77B:").append(regulatoryReporting).append("\n");
        }
        
        return sb.toString();
    }
} 