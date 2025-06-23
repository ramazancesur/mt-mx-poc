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
import java.util.List;
import java.util.ArrayList;

/**
 * MT102 - Multiple Customer Credit Transfer mesajı için model sınıfı
 * SWIFT MT102 standardına uygun alanlar içerir
 * Veritabanı entity değil, sadece parsing ve validation için kullanılır
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mt102 {

    // MANDATORY FIELDS - Null olamaz

    /**
     * :20: Transaction Reference Number
     * Maksimum 16 karakter, alfanumerik ve özel karakterler
     * ZORUNLU ALAN
     */
    @NotBlank(message = "Transaction Reference (:20:) zorunludur")
    @Size(max = 16, message = "Transaction Reference (:20:) maksimum 16 karakter olabilir")
    @Pattern(regexp = "^[A-Z0-9/\\-\\?:\\(\\)\\.,'\\+ ]+$", message = "Transaction Reference (:20:) geçersiz karakterler içeriyor")
    private String transactionReference;

    /**
     * :23: Bank Operation Code
     * Geçerli değerler: CRED, CRTS, SPAY, SSTD
     * ZORUNLU ALAN
     */
    @NotBlank(message = "Bank Operation Code (:23:) zorunludur")
    @Pattern(regexp = "^(CRED|CRTS|SPAY|SSTD)$", message = "Bank Operation Code (:23:) geçersiz - CRED, CRTS, SPAY veya SSTD olmalı")
    private String bankOperationCode;

    /**
     * :32A: Value Date/Currency/Interbank Settled Amount
     * Valör tarihi, para birimi ve toplam tutar
     * ZORUNLU ALANLAR
     */
    @NotNull(message = "Value Date (:32A:) zorunludur")
    private LocalDate valueDate;

    @NotBlank(message = "Settlement Currency (:32A:) zorunludur")
    @Size(min = 3, max = 3, message = "Settlement Currency (:32A:) 3 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Settlement Currency (:32A:) geçersiz format")
    private String settlementCurrency;

    @NotNull(message = "Interbank Settled Amount (:32A:) zorunludur")
    @DecimalMin(value = "0.01", message = "Interbank Settled Amount (:32A:) pozitif olmalı")
    private BigDecimal interbankSettledAmount;

    /**
     * :19: Sum of Amounts
     * Tüm bireysel transferlerin toplamı
     * ZORUNLU ALAN
     */
    @NotNull(message = "Sum of Amounts (:19:) zorunludur")
    @DecimalMin(value = "0.01", message = "Sum of Amounts (:19:) pozitif olmalı")
    private BigDecimal sumOfAmounts;

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
     * :25: Account Identification
     * Hesap numarası
     * OPSİYONEL ALAN
     */
    @Size(max = 35, message = "Account Identification (:25:) maksimum 35 karakter olabilir")
    private String accountIdentification;

    /**
     * :51A: Sending Institution
     * BIC kodu (8 veya 11 karakter)
     * OPSİYONEL ALAN
     */
    @Size(min = 8, max = 11, message = "Sending Institution (:51A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Sending Institution (:51A:) geçersiz BIC formatı")
    private String sendingInstitution;

    /**
     * :50A/50F/50K: Ordering Customer
     * Müşteri bilgileri (BIC veya isim/adres)
     * En az birinin bulunması zorunlu ama hepsi opsiyonel
     */
    @Size(min = 8, max = 11, message = "Ordering Customer BIC (:50A:) 8-11 karakter olmalı")
    @Pattern(regexp = "^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Ordering Customer BIC (:50A:) geçersiz BIC formatı")
    private String orderingCustomerBic;

    @Size(max = 140, message = "Ordering Customer Name maksimum 140 karakter olabilir")
    private String orderingCustomerName;

    @Size(max = 210, message = "Ordering Customer Address maksimum 210 karakter olabilir")
    private String orderingCustomerAddress;

    @Size(max = 34, message = "Ordering Customer Account maksimum 34 karakter olabilir")
    private String orderingCustomerAccount;

    /**
     * :52A/52D: Ordering Institution
     * Gönderen banka bilgileri
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
     * :71A: Details of Charges
     * Masraf bilgileri: BEN, OUR, SHA
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

    // Bireysel Transfer Detayları
    @Builder.Default
    private List<Mt102Transaction> transactions = new ArrayList<>();

    // Business methods

    /**
     * Ordering Customer alanlarından en az birinin dolu olup olmadığını kontrol eder
     */
    public boolean hasOrderingCustomer() {
        return (orderingCustomerBic != null && !orderingCustomerBic.trim().isEmpty()) ||
               (orderingCustomerName != null && !orderingCustomerName.trim().isEmpty());
    }

    /**
     * Toplam transfer sayısını döndürür
     */
    public int getTransactionCount() {
        return transactions != null ? transactions.size() : 0;
    }

    /**
     * Tüm bireysel tutarların toplamını hesaplar
     */
    public BigDecimal calculateTotalAmount() {
        if (transactions == null || transactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return transactions.stream()
                .filter(t -> t.getAmount() != null)
                .map(Mt102Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Sum of Amounts ile hesaplanan toplam tutarı karşılaştırır
     */
    public boolean isAmountBalanced() {
        if (sumOfAmounts == null) return false;
        BigDecimal calculated = calculateTotalAmount();
        return sumOfAmounts.compareTo(calculated) == 0;
    }

    /**
     * Mesajın temel geçerlilik kontrolü
     */
    public boolean isValid() {
        return transactionReference != null && !transactionReference.trim().isEmpty() &&
               bankOperationCode != null && !bankOperationCode.trim().isEmpty() &&
               valueDate != null &&
               settlementCurrency != null && settlementCurrency.length() == 3 &&
               interbankSettledAmount != null && interbankSettledAmount.compareTo(BigDecimal.ZERO) > 0 &&
               sumOfAmounts != null && sumOfAmounts.compareTo(BigDecimal.ZERO) > 0 &&
               hasOrderingCustomer() &&
               transactions != null && !transactions.isEmpty();
    }

    /**
     * Null güvenli string dönüşümü
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * SWIFT MT102 formatında string oluşturur
     */
    public String toSwiftFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("{1:F01").append(safeString(sendingInstitution)).append("0000000000}");
        sb.append("{2:I102").append("N}");
        sb.append("{3:{108:MT102}}");
        sb.append("{4:\n");
        
        // Mandatory fields
        sb.append(":20:").append(transactionReference).append("\n");
        sb.append(":23:").append(bankOperationCode).append("\n");
        
        // Ordering Customer (en az biri zorunlu)
        if (orderingCustomerBic != null && !orderingCustomerBic.trim().isEmpty()) {
            sb.append(":50A:").append(orderingCustomerBic).append("\n");
        } else if (orderingCustomerName != null && !orderingCustomerName.trim().isEmpty()) {
            sb.append(":50K:").append(orderingCustomerName).append("\n");
        }
        
        sb.append(":32A:").append(valueDate.toString().replace("-", "").substring(2))
          .append(settlementCurrency).append(interbankSettledAmount.toString().replace(".", ",")).append("\n");
        
        sb.append(":19:").append(sumOfAmounts.toString().replace(".", ",")).append("\n");
        
        // Optional fields - null kontrolü ile
        if (relatedReference != null && !relatedReference.trim().isEmpty()) {
            sb.append(":21:").append(relatedReference).append("\n");
        }
        
        if (sendersCorrespondentBic != null && !sendersCorrespondentBic.trim().isEmpty()) {
            sb.append(":53A:").append(sendersCorrespondentBic).append("\n");
        }
        
        if (receiversCorrespondentBic != null && !receiversCorrespondentBic.trim().isEmpty()) {
            sb.append(":54A:").append(receiversCorrespondentBic).append("\n");
        }
        
        if (detailsOfCharges != null && !detailsOfCharges.trim().isEmpty()) {
            sb.append(":71A:").append(detailsOfCharges).append("\n");
        }
        
        if (senderToReceiverInfo != null && !senderToReceiverInfo.trim().isEmpty()) {
            sb.append(":72:").append(senderToReceiverInfo).append("\n");
        }
        
        // Transaction details
        if (transactions != null) {
            for (Mt102Transaction transaction : transactions) {
                if (transaction != null) {
                    sb.append(transaction.toSwiftFormat());
                }
            }
        }
        
        sb.append("-}");
        return sb.toString();
    }
} 