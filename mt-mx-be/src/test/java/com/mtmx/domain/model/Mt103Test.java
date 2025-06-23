package com.mtmx.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class Mt103Test {

    @Test
    void mt103_ShouldCreateWithAllFields() {
        // Given
        String transactionReference = "TXN123456789";
        String bankOperationCode = "CRED";
        LocalDate valueDate = LocalDate.of(2025, 6, 22);
        String currency = "EUR";
        BigDecimal amount = new BigDecimal("1000.00");
        String detailsOfCharges = "SHA";
        String orderingCustomerName = "ORDERING CUSTOMER";
        String beneficiaryName = "BENEFICIARY CUSTOMER";
        String remittanceInformation = "Payment for services";

        // When
        Mt103 mt103 = Mt103.builder()
                .transactionReference(transactionReference)
                .bankOperationCode(bankOperationCode)
                .valueDate(valueDate)
                .currency(currency)
                .amount(amount)
                .detailsOfCharges(detailsOfCharges)
                .orderingCustomerName(orderingCustomerName)
                .beneficiaryName(beneficiaryName)
                .remittanceInformation(remittanceInformation)
                .build();

        // Then
        assertThat(mt103.getTransactionReference()).isEqualTo(transactionReference);
        assertThat(mt103.getBankOperationCode()).isEqualTo(bankOperationCode);
        assertThat(mt103.getValueDate()).isEqualTo(valueDate);
        assertThat(mt103.getCurrency()).isEqualTo(currency);
        assertThat(mt103.getAmount()).isEqualTo(amount);
        assertThat(mt103.getDetailsOfCharges()).isEqualTo(detailsOfCharges);
        assertThat(mt103.getOrderingCustomerName()).isEqualTo(orderingCustomerName);
        assertThat(mt103.getBeneficiaryName()).isEqualTo(beneficiaryName);
        assertThat(mt103.getRemittanceInformation()).isEqualTo(remittanceInformation);
    }

    @Test
    void mt103_ShouldHandleNullValues() {
        // When
        Mt103 mt103 = new Mt103();

        // Then
        assertThat(mt103.getTransactionReference()).isNull();
        assertThat(mt103.getBankOperationCode()).isNull();
        assertThat(mt103.getValueDate()).isNull();
        assertThat(mt103.getCurrency()).isNull();
        assertThat(mt103.getAmount()).isNull();
        assertThat(mt103.getDetailsOfCharges()).isNull();
        assertThat(mt103.getOrderingCustomerName()).isNull();
        assertThat(mt103.getBeneficiaryName()).isNull();
        assertThat(mt103.getRemittanceInformation()).isNull();
    }

    @Test
    void mt103_ToString_ShouldContainMainFields() {
        // Given
        Mt103 mt103 = Mt103.builder()
                .transactionReference("TXN123")
                .currency("EUR")
                .amount(new BigDecimal("1000.00"))
                .build();

        // When
        String toString = mt103.toString();

        // Then
        assertThat(toString).contains("transactionReference=TXN123");
        assertThat(toString).contains("currency=EUR");
        assertThat(toString).contains("amount=1000.00");
    }

    @Test
    void mt103_EqualsAndHashCode_ShouldWorkCorrectly() {
        // Given
        Mt103 mt103a = Mt103.builder()
                .transactionReference("TXN123")
                .currency("EUR")
                .amount(new BigDecimal("1000.00"))
                .build();

        Mt103 mt103b = Mt103.builder()
                .transactionReference("TXN123")
                .currency("EUR")
                .amount(new BigDecimal("1000.00"))
                .build();

        Mt103 mt103c = Mt103.builder()
                .transactionReference("TXN456")
                .currency("USD")
                .amount(new BigDecimal("2000.00"))
                .build();

        // Then
        assertThat(mt103a).isEqualTo(mt103b);
        assertThat(mt103a).isNotEqualTo(mt103c);
        assertThat(mt103a.hashCode()).isEqualTo(mt103b.hashCode());
        assertThat(mt103a.hashCode()).isNotEqualTo(mt103c.hashCode());
    }

    @Test
    void mt103_WithMinimalFields_ShouldWork() {
        // Given
        Mt103 mt103 = Mt103.builder()
                .transactionReference("TXN123")
                .bankOperationCode("CRED")
                .valueDate(LocalDate.of(2025, 6, 22))
                .currency("EUR")
                .amount(new BigDecimal("1000.00"))
                .detailsOfCharges("SHA")
                .orderingCustomerName("ORDERING CUSTOMER")
                .beneficiaryName("BENEFICIARY CUSTOMER")
                .build();

        // Then
        assertThat(mt103.getTransactionReference()).isEqualTo("TXN123");
        assertThat(mt103.getBankOperationCode()).isEqualTo("CRED");
        assertThat(mt103.getDetailsOfCharges()).isEqualTo("SHA");
        assertThat(mt103.getOrderingCustomerName()).isEqualTo("ORDERING CUSTOMER");
        assertThat(mt103.getBeneficiaryName()).isEqualTo("BENEFICIARY CUSTOMER");
    }

    @Test
    void mt103_BusinessMethods_ShouldWork() {
        // Given
        Mt103 mt103 = Mt103.builder()
                .orderingCustomerName("ORDERING CUSTOMER")
                .beneficiaryName("BENEFICIARY CUSTOMER")
                .build();

        // Then
        assertThat(mt103.hasOrderingCustomer()).isTrue();
        assertThat(mt103.hasBeneficiary()).isTrue();
    }
}