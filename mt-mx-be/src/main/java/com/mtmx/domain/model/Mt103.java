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
import java.time.LocalDate;

/**
 * MT103 - Single Customer Credit Transfer mesajı için model sınıfı
 * SWIFT MT103 standardına uygun alanlar içerir
 * Veritabanı entity değil, sadece parsing ve validation için kullanılır
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt103 {

    // MANDATORY FIELDS - Null olamaz

    /**
     * :20: Transaction Reference Number
     * Sender'ın referans numarası
     * ZORUNLU ALAN
     */
    @NotBlank(message = "Transaction Reference (:20:) zorunludur")
    @Size(max = 16, message = "Transaction Reference (:20:) maksimum 16 karakter olabilir")
    @Pattern(regexp = "^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ ]+$", message = "Transaction Reference (:20:) geçersiz karakterler içeriyor")
    private String transactionReference;

    /**
     * :23B: Bank Operation Code
     * Banka işlem kodu: CRED, CRTS, SPAY, SSTD
     * ZORUNLU ALAN
     */
    @NotBlank(message = "Bank Operation Code (:23B:) zorunludur")
    @Pattern(regexp = "^(CRED|CRTS|SPAY|SSTD)$", message = "Bank Operation Code (:23B:) geçersiz - CRED, CRTS, SPAY veya SSTD olmalı")
    private String bankOperationCode;

    /**
     * :32A: Value Date/Currency/Interbank Settled Amount
     * Valör tarihi, para birimi ve tutar
     * ZORUNLU ALANLAR
     */
    @NotNull(message = "Value Date (:32A:) zorunludur")
    private LocalDate valueDate;

    @NotBlank(message = "Currency (:32A:) zorunludur")
    @Size(min = 3, max = 3, message = "Currency (:32A:) 3 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency (:32A:) geçersiz format")
    private String currency;

    @NotNull(message = "Amount (:32A:) zorunludur")
    @DecimalMin(value = "0.01", message = "Amount (:32A:) pozitif olmalı")
    private BigDecimal amount;

    /**
     * :71A: Details of Charges
     * Masraf detayları: BEN, OUR, SHA
     * MT103 için ZORUNLU ALAN
     */
    @NotBlank(message = "Details of Charges (:71A:) MT103 için zorunludur")
    @Pattern(regexp = "^(BEN|OUR|SHA)$", message = "Details of Charges (:71A:) geçersiz - BEN, OUR veya SHA olmalı")
    private String detailsOfCharges;

    // CONDITIONAL MANDATORY FIELDS - Belirli durumlar için zorunlu

    /**
     * :50A/50F/50K: Ordering Customer
     * Müşteri bilgileri - en az birinin bulunması zorunlu
     */
    @Size(min = 8, max = 11, message = "Ordering Customer BIC (:50A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Ordering Customer BIC (:50A:) geçersiz BIC formatı")
    private String orderingCustomerBic;

    @Size(max = 34, message = "Ordering Customer Account maksimum 34 karakter olabilir")
    private String orderingCustomerAccount;

    @Size(max = 140, message = "Ordering Customer Name maksimum 140 karakter olabilir")
    private String orderingCustomerName;

    @Size(max = 210, message = "Ordering Customer Address maksimum 210 karakter olabilir")
    private String orderingCustomerAddress;

    /**
     * :59/59A/59F: Beneficiary Customer
     * Alıcı müşteri bilgileri - en az birinin bulunması zorunlu
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
     * :21: Related Reference
     * İlgili referans numarası
     * OPSİYONEL ALAN
     */
    @Size(max = 16, message = "Related Reference (:21:) maksimum 16 karakter olabilir")
    @Pattern(regexp = "^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ ]*$", message = "Related Reference (:21:) geçersiz karakterler içeriyor")
    private String relatedReference;

    /**
     * :23E: Instruction Code
     * Talimat kodu
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
     * :25: Account Identification
     * Hesap numarası
     * OPSİYONEL ALAN
     */
    @Size(max = 35, message = "Account Identification (:25:) maksimum 35 karakter olabilir")
    private String accountIdentification;

    /**
     * :51A: Sending Institution
     * Gönderen kurum
     * OPSİYONEL ALAN
     */
    @Size(min = 8, max = 11, message = "Sending Institution (:51A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Sending Institution (:51A:) geçersiz BIC formatı")
    private String sendingInstitution;

    /**
     * :52A/52D: Ordering Institution
     * Gönderen banka
     * OPSİYONEL ALANLAR
     */
    @Size(min = 8, max = 11, message = "Ordering Institution BIC (:52A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Ordering Institution BIC (:52A:) geçersiz BIC formatı")
    private String orderingInstitutionBic;

    @Size(max = 140, message = "Ordering Institution Name maksimum 140 karakter olabilir")
    private String orderingInstitutionName;

    @Size(max = 210, message = "Ordering Institution Address maksimum 210 karakter olabilir")
    private String orderingInstitutionAddress;

    /**
     * :53A/53B/53D: Sender's Correspondent
     * Gönderen muhabir banka
     * OPSİYONEL ALANLAR
     */
    @Size(min = 8, max = 11, message = "Sender's Correspondent BIC (:53A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Sender's Correspondent BIC (:53A:) geçersiz BIC formatı")
    private String sendersCorrespondentBic;

    @Size(max = 35, message = "Sender's Correspondent Account maksimum 35 karakter olabilir")
    private String sendersCorrespondentAccount;

    @Size(max = 140, message = "Sender's Correspondent Name maksimum 140 karakter olabilir")
    private String sendersCorrespondentName;

    /**
     * :54A/54B/54D: Receiver's Correspondent
     * Alıcı muhabir banka
     * OPSİYONEL ALANLAR
     */
    @Size(min = 8, max = 11, message = "Receiver's Correspondent BIC (:54A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Receiver's Correspondent BIC (:54A:) geçersiz BIC formatı")
    private String receiversCorrespondentBic;

    @Size(max = 35, message = "Receiver's Correspondent Account maksimum 35 karakter olabilir")
    private String receiversCorrespondentAccount;

    @Size(max = 140, message = "Receiver's Correspondent Name maksimum 140 karakter olabilir")
    private String receiversCorrespondentName;

    /**
     * :56A/56C/56D: Intermediary
     * Aracı banka
     * OPSİYONEL ALANLAR
     */
    @Size(min = 8, max = 11, message = "Intermediary BIC (:56A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Intermediary BIC (:56A:) geçersiz BIC formatı")
    private String intermediaryBic;

    @Size(max = 35, message = "Intermediary Account maksimum 35 karakter olabilir")
    private String intermediaryAccount;

    @Size(max = 140, message = "Intermediary Name maksimum 140 karakter olabilir")
    private String intermediaryName;

    /**
     * :57A/57B/57C/57D: Account With Institution
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
     * :71F: Sender's Charges
     * Gönderen masrafları
     * OPSİYONEL ALANLAR
     */
    @Size(min = 3, max = 3, message = "Sender's Charges Currency 3 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Sender's Charges Currency geçersiz format")
    private String sendersChargesCurrency;

    @DecimalMin(value = "0.00", message = "Sender's Charges Amount negatif olamaz")
    private BigDecimal sendersChargesAmount;

    /**
     * :71G: Receiver's Charges
     * Alıcı masrafları
     * OPSİYONEL ALANLAR
     */
    @Size(min = 3, max = 3, message = "Receiver's Charges Currency 3 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Receiver's Charges Currency geçersiz format")
    private String receiversChargesCurrency;

    @DecimalMin(value = "0.00", message = "Receiver's Charges Amount negatif olamaz")
    private BigDecimal receiversChargesAmount;

    /**
     * :72: Sender to Receiver Information
     * Gönderen-alıcı bilgileri (maksimum 6 satır, her satır 35 karakter)
     * OPSİYONEL ALAN
     */
    @Size(max = 210, message = "Sender to Receiver Information (:72:) maksimum 210 karakter olabilir")
    private String senderToReceiverInfo;

    /**
     * :77B: Regulatory Reporting
     * Düzenleyici raporlama bilgileri (maksimum 3 satır, her satır 35 karakter)
     * OPSİYONEL ALAN
     */
    @Size(max = 105, message = "Regulatory Reporting (:77B:) maksimum 105 karakter olabilir")
    private String regulatoryReporting;

    // Business methods

    /**
     * Ordering Customer alanlarından en az birinin dolu olup olmadığını kontrol eder
     */
    public boolean hasOrderingCustomer() {
        return (orderingCustomerBic != null && !orderingCustomerBic.trim().isEmpty()) ||
               (orderingCustomerName != null && !orderingCustomerName.trim().isEmpty());
    }

    /**
     * Beneficiary Customer alanlarından en az birinin dolu olup olmadığını kontrol eder
     */
    public boolean hasBeneficiary() {
        return (beneficiaryBic != null && !beneficiaryBic.trim().isEmpty()) ||
               (beneficiaryAccount != null && !beneficiaryAccount.trim().isEmpty()) ||
               (beneficiaryName != null && !beneficiaryName.trim().isEmpty());
    }

    /**
     * Mesajın temel geçerlilik kontrolü
     */
    public boolean isValid() {
        return transactionReference != null && !transactionReference.trim().isEmpty() &&
               bankOperationCode != null && !bankOperationCode.trim().isEmpty() &&
               valueDate != null &&
               currency != null && currency.length() == 3 &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               detailsOfCharges != null && !detailsOfCharges.trim().isEmpty() &&
               hasOrderingCustomer() &&
               hasBeneficiary();
    }

    /**
     * Null güvenli string dönüşümü
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * SWIFT MT103 formatında string oluşturur
     */
    public String toSwiftFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("{1:F01").append(safeString(sendingInstitution)).append("0000000000}");
        sb.append("{2:I103").append("N}");
        sb.append("{3:{108:MT103}}");
        sb.append("{4:\n");
        
        // Mandatory fields
        sb.append(":20:").append(transactionReference).append("\n");
        sb.append(":23B:").append(bankOperationCode).append("\n");
        
        // Optional fields
        if (relatedReference != null && !relatedReference.trim().isEmpty()) {
            sb.append(":21:").append(relatedReference).append("\n");
        }
        
        if (instructionCode != null && !instructionCode.trim().isEmpty()) {
            sb.append(":23E:").append(instructionCode).append("\n");
        }
        
        if (transactionTypeCode != null && !transactionTypeCode.trim().isEmpty()) {
            sb.append(":26T:").append(transactionTypeCode).append("\n");
        }
        
        // Value Date, Currency, Amount
        sb.append(":32A:").append(valueDate.toString().replace("-", "").substring(2))
          .append(currency).append(amount.toString().replace(".", ",")).append("\n");
        
        // Ordering Customer
        if (orderingCustomerBic != null && !orderingCustomerBic.trim().isEmpty()) {
            sb.append(":50A:").append(orderingCustomerBic).append("\n");
        } else if (orderingCustomerName != null && !orderingCustomerName.trim().isEmpty()) {
            sb.append(":50K:").append(orderingCustomerName).append("\n");
        }
        
        // Optional correspondent banks
        if (sendersCorrespondentBic != null && !sendersCorrespondentBic.trim().isEmpty()) {
            sb.append(":53A:").append(sendersCorrespondentBic).append("\n");
        }
        
        if (receiversCorrespondentBic != null && !receiversCorrespondentBic.trim().isEmpty()) {
            sb.append(":54A:").append(receiversCorrespondentBic).append("\n");
        }
        
        if (intermediaryBic != null && !intermediaryBic.trim().isEmpty()) {
            sb.append(":56A:").append(intermediaryBic).append("\n");
        }
        
        if (accountWithInstitutionBic != null && !accountWithInstitutionBic.trim().isEmpty()) {
            sb.append(":57A:").append(accountWithInstitutionBic).append("\n");
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
        
        // Remittance Information
        if (remittanceInformation != null && !remittanceInformation.trim().isEmpty()) {
            sb.append(":70:").append(remittanceInformation).append("\n");
        }
        
        // Details of Charges (mandatory)
        sb.append(":71A:").append(detailsOfCharges).append("\n");
        
        // Optional charges
        if (sendersChargesCurrency != null && sendersChargesAmount != null) {
            sb.append(":71F:").append(sendersChargesCurrency)
              .append(sendersChargesAmount.toString().replace(".", ",")).append("\n");
        }
        
        if (receiversChargesCurrency != null && receiversChargesAmount != null) {
            sb.append(":71G:").append(receiversChargesCurrency)
              .append(receiversChargesAmount.toString().replace(".", ",")).append("\n");
        }
        
        // Sender to Receiver Information
        if (senderToReceiverInfo != null && !senderToReceiverInfo.trim().isEmpty()) {
            sb.append(":72:").append(senderToReceiverInfo).append("\n");
        }
        
        // Regulatory Reporting
        if (regulatoryReporting != null && !regulatoryReporting.trim().isEmpty()) {
            sb.append(":77B:").append(regulatoryReporting).append("\n");
        }
        
        sb.append("-}");
        return sb.toString();
    }
} 