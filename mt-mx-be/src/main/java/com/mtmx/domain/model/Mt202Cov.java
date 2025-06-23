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
 * MT202COV - Financial Institution Transfer with Cover Payment mesajı için model sınıfı
 * SWIFT MT202COV standardına uygun alanlar içerir
 * Veritabanı entity değil, sadece parsing ve validation için kullanılır
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt202Cov {

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
     * :21: Related Reference
     * İlgili referans numarası - MT202COV için zorunlu
     * ZORUNLU ALAN
     */
    @NotBlank(message = "Related Reference (:21:) MT202COV için zorunludur")
    @Size(max = 16, message = "Related Reference (:21:) maksimum 16 karakter olabilir")
    @Pattern(regexp = "^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ ]+$", message = "Related Reference (:21:) geçersiz karakterler içeriyor")
    private String relatedReference;

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
     * :58A/58D: Beneficiary Institution
     * Alıcı finansal kurum - MT202COV için zorunlu
     */
    @Size(min = 8, max = 11, message = "Beneficiary Institution BIC (:58A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Beneficiary Institution BIC (:58A:) geçersiz BIC formatı")
    private String beneficiaryInstitutionBic;

    @Size(max = 140, message = "Beneficiary Institution Name maksimum 140 karakter olabilir")
    private String beneficiaryInstitutionName;

    @Size(max = 210, message = "Beneficiary Institution Address maksimum 210 karakter olabilir")
    private String beneficiaryInstitutionAddress;

    // UNDERLYING CUSTOMER CREDIT TRANSFER FIELDS - MT202COV için zorunlu

    /**
     * :33B: Currency/Original Ordered Amount
     * Orijinal sipariş edilen tutar - MT202COV için zorunlu
     */
    @NotBlank(message = "Original Ordered Currency (:33B:) MT202COV için zorunludur")
    @Size(min = 3, max = 3, message = "Original Ordered Currency (:33B:) 3 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Original Ordered Currency (:33B:) geçersiz format")
    private String originalOrderedCurrency;

    @NotNull(message = "Original Ordered Amount (:33B:) MT202COV için zorunludur")
    @DecimalMin(value = "0.01", message = "Original Ordered Amount (:33B:) pozitif olmalı")
    private BigDecimal originalOrderedAmount;

    /**
     * :50A/50F/50K: Ordering Customer
     * Müşteri bilgileri - MT202COV için en az birinin bulunması zorunlu
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
     * :59/59A: Beneficiary Customer
     * Alıcı müşteri bilgileri - MT202COV için en az birinin bulunması zorunlu
     */
    @Size(min = 8, max = 11, message = "Beneficiary Customer BIC (:59A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Beneficiary Customer BIC (:59A:) geçersiz BIC formatı")
    private String beneficiaryCustomerBic;

    @Size(max = 34, message = "Beneficiary Customer Account maksimum 34 karakter olabilir")
    private String beneficiaryCustomerAccount;

    @Size(max = 140, message = "Beneficiary Customer Name maksimum 140 karakter olabilir")
    private String beneficiaryCustomerName;

    @Size(max = 210, message = "Beneficiary Customer Address maksimum 210 karakter olabilir")
    private String beneficiaryCustomerAddress;

    /**
     * :70: Remittance Information
     * Havale bilgileri - MT202COV için zorunlu
     */
    @NotBlank(message = "Remittance Information (:70:) MT202COV için zorunludur")
    @Size(max = 140, message = "Remittance Information (:70:) maksimum 140 karakter olabilir")
    private String remittanceInformation;

    // OPTIONAL FIELDS - Null olabilir

    /**
     * :13C: Time Indication
     * Zaman göstergesi
     * OPSİYONEL ALAN
     */
    @Size(max = 8, message = "Time Indication (:13C:) maksimum 8 karakter olabilir")
    @Pattern(regexp = "^(/CLSTIME/|/SNDTIME/|/RNCTIME/)[0-9]{4}$", message = "Time Indication (:13C:) geçersiz format")
    private String timeIndication;

    /**
     * :25: Account Identification
     * Hesap numarası
     * OPSİYONEL ALAN
     */
    @Size(max = 35, message = "Account Identification (:25:) maksimum 35 karakter olabilir")
    private String accountIdentification;

    /**
     * :26T: Transaction Type Code
     * İşlem tipi kodu
     * OPSİYONEL ALAN
     */
    @Size(max = 3, message = "Transaction Type Code (:26T:) maksimum 3 karakter olabilir")
    private String transactionTypeCode;

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

    /**
     * :71A: Details of Charges
     * Masraf detayları
     * OPSİYONEL ALAN
     */
    @Pattern(regexp = "^(BEN|OUR|SHA)$", message = "Details of Charges (:71A:) geçersiz - BEN, OUR veya SHA olmalı")
    private String detailsOfCharges;

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
     * :72: Sender to Receiver Information
     * Gönderen-alıcı bilgileri
     * OPSİYONEL ALAN
     */
    @Size(max = 210, message = "Sender to Receiver Information (:72:) maksimum 210 karakter olabilir")
    private String senderToReceiverInfo;

    /**
     * :77B: Regulatory Reporting
     * Düzenleyici raporlama bilgileri
     * OPSİYONEL ALAN
     */
    @Size(max = 105, message = "Regulatory Reporting (:77B:) maksimum 105 karakter olabilir")
    private String regulatoryReporting;

    // Business methods

    /**
     * Beneficiary Institution alanlarından en az birinin dolu olup olmadığını kontrol eder
     */
    public boolean hasBeneficiaryInstitution() {
        return (beneficiaryInstitutionBic != null && !beneficiaryInstitutionBic.trim().isEmpty()) ||
               (beneficiaryInstitutionName != null && !beneficiaryInstitutionName.trim().isEmpty());
    }

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
    public boolean hasBeneficiaryCustomer() {
        return (beneficiaryCustomerBic != null && !beneficiaryCustomerBic.trim().isEmpty()) ||
               (beneficiaryCustomerAccount != null && !beneficiaryCustomerAccount.trim().isEmpty()) ||
               (beneficiaryCustomerName != null && !beneficiaryCustomerName.trim().isEmpty());
    }

    /**
     * Mesajın temel geçerlilik kontrolü
     */
    public boolean isValid() {
        return transactionReference != null && !transactionReference.trim().isEmpty() &&
               relatedReference != null && !relatedReference.trim().isEmpty() &&
               valueDate != null &&
               currency != null && currency.length() == 3 &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               originalOrderedCurrency != null && originalOrderedCurrency.length() == 3 &&
               originalOrderedAmount != null && originalOrderedAmount.compareTo(BigDecimal.ZERO) > 0 &&
               remittanceInformation != null && !remittanceInformation.trim().isEmpty() &&
               hasBeneficiaryInstitution() &&
               hasOrderingCustomer() &&
               hasBeneficiaryCustomer();
    }

    /**
     * Null güvenli string dönüşümü
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * SWIFT MT202COV formatında string oluşturur
     */
    public String toSwiftFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("{1:F01").append(safeString(sendingInstitution)).append("0000000000}");
        sb.append("{2:I202").append("N}");
        sb.append("{3:{108:MT202COV}}");
        sb.append("{4:\n");
        
        // Mandatory fields
        sb.append(":20:").append(transactionReference).append("\n");
        sb.append(":21:").append(relatedReference).append("\n");
        
        // Optional fields
        if (timeIndication != null && !timeIndication.trim().isEmpty()) {
            sb.append(":13C:").append(timeIndication).append("\n");
        }
        
        if (accountIdentification != null && !accountIdentification.trim().isEmpty()) {
            sb.append(":25:").append(accountIdentification).append("\n");
        }
        
        if (transactionTypeCode != null && !transactionTypeCode.trim().isEmpty()) {
            sb.append(":26T:").append(transactionTypeCode).append("\n");
        }
        
        // Value Date, Currency, Amount
        sb.append(":32A:").append(valueDate.toString().replace("-", "").substring(2))
          .append(currency).append(amount.toString().replace(".", ",")).append("\n");
        
        // Original Ordered Amount
        sb.append(":33B:").append(originalOrderedCurrency)
          .append(originalOrderedAmount.toString().replace(".", ",")).append("\n");
        
        // Ordering Customer (mandatory for COV)
        if (orderingCustomerBic != null && !orderingCustomerBic.trim().isEmpty()) {
            sb.append(":50A:").append(orderingCustomerBic).append("\n");
        } else if (orderingCustomerName != null && !orderingCustomerName.trim().isEmpty()) {
            sb.append(":50K:").append(orderingCustomerName).append("\n");
        }
        
        // Optional institutions
        if (orderingInstitutionBic != null && !orderingInstitutionBic.trim().isEmpty()) {
            sb.append(":52A:").append(orderingInstitutionBic).append("\n");
        }
        
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
        
        // Beneficiary Institution (mandatory)
        if (beneficiaryInstitutionBic != null && !beneficiaryInstitutionBic.trim().isEmpty()) {
            sb.append(":58A:").append(beneficiaryInstitutionBic).append("\n");
        } else if (beneficiaryInstitutionName != null && !beneficiaryInstitutionName.trim().isEmpty()) {
            sb.append(":58D:").append(beneficiaryInstitutionName).append("\n");
        }
        
        // Beneficiary Customer (mandatory for COV)
        if (beneficiaryCustomerBic != null && !beneficiaryCustomerBic.trim().isEmpty()) {
            sb.append(":59A:").append(beneficiaryCustomerBic).append("\n");
        } else {
            sb.append(":59:");
            if (beneficiaryCustomerAccount != null && !beneficiaryCustomerAccount.trim().isEmpty()) {
                sb.append("/").append(beneficiaryCustomerAccount).append("\n");
            }
            if (beneficiaryCustomerName != null && !beneficiaryCustomerName.trim().isEmpty()) {
                sb.append(beneficiaryCustomerName).append("\n");
            }
        }
        
        // Remittance Information (mandatory for COV)
        sb.append(":70:").append(remittanceInformation).append("\n");
        
        // Optional charges and information
        if (detailsOfCharges != null && !detailsOfCharges.trim().isEmpty()) {
            sb.append(":71A:").append(detailsOfCharges).append("\n");
        }
        
        if (sendersChargesCurrency != null && sendersChargesAmount != null) {
            sb.append(":71F:").append(sendersChargesCurrency)
              .append(sendersChargesAmount.toString().replace(".", ",")).append("\n");
        }
        
        if (senderToReceiverInfo != null && !senderToReceiverInfo.trim().isEmpty()) {
            sb.append(":72:").append(senderToReceiverInfo).append("\n");
        }
        
        if (regulatoryReporting != null && !regulatoryReporting.trim().isEmpty()) {
            sb.append(":77B:").append(regulatoryReporting).append("\n");
        }
        
        sb.append("-}");
        return sb.toString();
    }
} 