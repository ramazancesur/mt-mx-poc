package com.mtmx.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class Mt102Test {

    private Mt102 mt102;
    private Mt102Transaction transaction;

    @BeforeEach
    void setUp() {
        mt102 = new Mt102();
        transaction = new Mt102Transaction();
    }

    @Test
    void testMt102Creation() {
        // Arrange & Act
        Mt102 mt102 = Mt102.builder()
                .transactionReference("TXN123456789")
                .bankOperationCode("CRED")
                .valueDate(LocalDate.of(2023, 12, 15))
                .settlementCurrency("USD")
                .interbankSettledAmount(new BigDecimal("1000.00"))
                .sumOfAmounts(new BigDecimal("1000.00"))
                .orderingCustomerBic("DEUTDEFF")
                .orderingCustomerName("Test Customer")
                .orderingCustomerAddress("123 Test Street, Test City")
                .build();

        // Assert
        assertNotNull(mt102);
        assertEquals("TXN123456789", mt102.getTransactionReference());
        assertEquals("CRED", mt102.getBankOperationCode());
        assertEquals(LocalDate.of(2023, 12, 15), mt102.getValueDate());
        assertEquals("USD", mt102.getSettlementCurrency());
        assertEquals(new BigDecimal("1000.00"), mt102.getInterbankSettledAmount());
        assertEquals(new BigDecimal("1000.00"), mt102.getSumOfAmounts());
        assertEquals("DEUTDEFF", mt102.getOrderingCustomerBic());
        assertEquals("Test Customer", mt102.getOrderingCustomerName());
        assertEquals("123 Test Street, Test City", mt102.getOrderingCustomerAddress());
    }

    @Test
    void testMt102DefaultValues() {
        // Arrange & Act
        Mt102 mt102 = new Mt102();

        // Assert
        assertNull(mt102.getTransactionReference());
        assertNull(mt102.getBankOperationCode());
        assertNull(mt102.getValueDate());
        assertNull(mt102.getSettlementCurrency());
        assertNull(mt102.getInterbankSettledAmount());
        assertNull(mt102.getSumOfAmounts());
        assertNull(mt102.getOrderingCustomerBic());
        assertNull(mt102.getOrderingCustomerName());
        assertNull(mt102.getOrderingCustomerAddress());
    }

    @Test
    void testMt102SettersAndGetters() {
        // Arrange
        Mt102 mt102 = new Mt102();

        // Act
        mt102.setTransactionReference("REF123456");
        mt102.setBankOperationCode("CRED");
        mt102.setValueDate(LocalDate.of(2023, 12, 15));
        mt102.setSettlementCurrency("EUR");
        mt102.setInterbankSettledAmount(new BigDecimal("2500.00"));
        mt102.setSumOfAmounts(new BigDecimal("2500.00"));

        // Assert
        assertEquals("REF123456", mt102.getTransactionReference());
        assertEquals("CRED", mt102.getBankOperationCode());
        assertEquals(LocalDate.of(2023, 12, 15), mt102.getValueDate());
        assertEquals("EUR", mt102.getSettlementCurrency());
        assertEquals(new BigDecimal("2500.00"), mt102.getInterbankSettledAmount());
        assertEquals(new BigDecimal("2500.00"), mt102.getSumOfAmounts());
    }

    @Test
    void testMt102Equality() {
        // Arrange
        Mt102 mt102a = Mt102.builder()
                .transactionReference("TXN123")
                .bankOperationCode("CRED")
                .settlementCurrency("USD")
                .interbankSettledAmount(new BigDecimal("1000.00"))
                .build();

        Mt102 mt102b = Mt102.builder()
                .transactionReference("TXN123")
                .bankOperationCode("CRED")
                .settlementCurrency("USD")
                .interbankSettledAmount(new BigDecimal("1000.00"))
                .build();

        Mt102 mt102c = Mt102.builder()
                .transactionReference("TXN456")
                .bankOperationCode("CRED")
                .settlementCurrency("EUR")
                .interbankSettledAmount(new BigDecimal("2000.00"))
                .build();

        // Assert
        assertEquals(mt102a, mt102b);
        assertNotEquals(mt102a, mt102c);
        assertEquals(mt102a.hashCode(), mt102b.hashCode());
    }

    @Test
    void testMt102ToString() {
        // Arrange
        Mt102 mt102 = new Mt102();
        mt102.setTransactionReference("TXN123");

        // Act
        String result = mt102.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Mt102"));
        assertTrue(result.contains("TXN123"));
    }

    @Test
    void testMt102WithTransactions() {
        // Arrange
        Mt102 mt102 = new Mt102();
        mt102.setTransactionReference("TXN123");

        Mt102Transaction transaction = new Mt102Transaction();
        transaction.setTransactionReference("TXN123-01");
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setBeneficiaryName("Beneficiary Name");

        List<Mt102Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        mt102.setTransactions(transactions);

        // Act & Assert
        assertNotNull(mt102.getTransactions());
        assertEquals(1, mt102.getTransactions().size());
        assertEquals("TXN123-01", mt102.getTransactions().get(0).getTransactionReference());
        assertEquals(new BigDecimal("500.00"), mt102.getTransactions().get(0).getAmount());
        assertEquals("Beneficiary Name", mt102.getTransactions().get(0).getBeneficiaryName());
    }

    @Test
    void testHasOrderingCustomer() {
        // Arrange
        Mt102 mt102WithBic = new Mt102();
        mt102WithBic.setOrderingCustomerBic("DEUTDEFF");

        Mt102 mt102WithName = new Mt102();
        mt102WithName.setOrderingCustomerName("Test Customer");

        Mt102 mt102Empty = new Mt102();

        // Act & Assert
        assertTrue(mt102WithBic.hasOrderingCustomer());
        assertTrue(mt102WithName.hasOrderingCustomer());
        assertFalse(mt102Empty.hasOrderingCustomer());
    }

    @Test
    void testGetTransactionCount() {
        // Arrange
        Mt102 mt102 = new Mt102();
        List<Mt102Transaction> transactions = new ArrayList<>();
        transactions.add(new Mt102Transaction());
        transactions.add(new Mt102Transaction());
        mt102.setTransactions(transactions);

        // Act & Assert
        assertEquals(2, mt102.getTransactionCount());
    }

    @Test
    void testCalculateTotalAmount() {
        // Arrange
        Mt102 mt102 = new Mt102();
        List<Mt102Transaction> transactions = new ArrayList<>();

        Mt102Transaction tx1 = new Mt102Transaction();
        tx1.setAmount(new BigDecimal("100.00"));
        transactions.add(tx1);

        Mt102Transaction tx2 = new Mt102Transaction();
        tx2.setAmount(new BigDecimal("200.00"));
        transactions.add(tx2);

        mt102.setTransactions(transactions);

        // Act
        BigDecimal total = mt102.calculateTotalAmount();

        // Assert
        assertEquals(new BigDecimal("300.00"), total);
    }

    @Test
    void testIsAmountBalanced() {
        // Arrange
        Mt102 mt102Balanced = new Mt102();
        mt102Balanced.setSumOfAmounts(new BigDecimal("300.00"));
        List<Mt102Transaction> transactions = new ArrayList<>();

        Mt102Transaction tx1 = new Mt102Transaction();
        tx1.setAmount(new BigDecimal("100.00"));
        transactions.add(tx1);

        Mt102Transaction tx2 = new Mt102Transaction();
        tx2.setAmount(new BigDecimal("200.00"));
        transactions.add(tx2);

        mt102Balanced.setTransactions(transactions);

        Mt102 mt102Unbalanced = new Mt102();
        mt102Unbalanced.setSumOfAmounts(new BigDecimal("500.00"));
        mt102Unbalanced.setTransactions(transactions);

        // Act & Assert
        assertTrue(mt102Balanced.isAmountBalanced());
        assertFalse(mt102Unbalanced.isAmountBalanced());
    }
}