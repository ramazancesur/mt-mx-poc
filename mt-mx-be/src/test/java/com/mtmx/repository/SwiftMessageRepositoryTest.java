package com.mtmx.repository;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class SwiftMessageRepositoryTest {

    @Autowired
    private SwiftMessageRepository swiftMessageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private SwiftMessage swiftMessage1;
    private SwiftMessage swiftMessage2;
    private SwiftMessage swiftMessage3;

    @BeforeEach
    void setUp() {
        // Create test data
        swiftMessage1 = new SwiftMessage();
        swiftMessage1.setMessageType(MessageType.MT103);
        swiftMessage1.setSenderBic("BANKBEBB0000");
        swiftMessage1.setReceiverBic("BANKDEFF0000");
        swiftMessage1.setAmount(new BigDecimal("1000.00"));
        swiftMessage1.setCurrency("EUR");
        swiftMessage1.setValueDate(LocalDate.now());
        swiftMessage1.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:TEST1-}");
        swiftMessage1.setCreatedAt(LocalDateTime.now());
        swiftMessage1.setUpdatedAt(LocalDateTime.now());

        swiftMessage2 = new SwiftMessage();
        swiftMessage2.setMessageType(MessageType.MT102);
        swiftMessage2.setSenderBic("BANKFRPP0000");
        swiftMessage2.setReceiverBic("BANKDEFF0000");
        swiftMessage2.setAmount(new BigDecimal("2000.00"));
        swiftMessage2.setCurrency("EUR");
        swiftMessage2.setValueDate(LocalDate.now());
        swiftMessage2.setRawMtMessage("{1:F01BANKFRPP0000000000}{2:I102BANKDEFFN}{4::20:TEST2-}");
        swiftMessage2.setCreatedAt(LocalDateTime.now());
        swiftMessage2.setUpdatedAt(LocalDateTime.now());

        swiftMessage3 = new SwiftMessage();
        swiftMessage3.setMessageType(MessageType.MT103);
        swiftMessage3.setSenderBic("BANKGB2L0000");
        swiftMessage3.setReceiverBic("BANKDEFF0000");
        swiftMessage3.setAmount(new BigDecimal("3000.00"));
        swiftMessage3.setCurrency("USD");
        swiftMessage3.setValueDate(LocalDate.now());
        swiftMessage3.setRawMtMessage("{1:F01BANKGB2L0000000000}{2:I103BANKDEFFN}{4::20:TEST3-}");
        swiftMessage3.setCreatedAt(LocalDateTime.now());
        swiftMessage3.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findByMessageType_ShouldReturnFilteredResults() {
        // Given
        entityManager.persistAndFlush(swiftMessage1);
        entityManager.persistAndFlush(swiftMessage2);
        entityManager.persistAndFlush(swiftMessage3);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<SwiftMessage> mt103Messages = swiftMessageRepository.findByMessageType(MessageType.MT103, pageable);
        Page<SwiftMessage> mt102Messages = swiftMessageRepository.findByMessageType(MessageType.MT102, pageable);

        // Then
        assertThat(mt103Messages.getContent()).hasSize(2);
        assertThat(mt102Messages.getContent()).hasSize(1);
        
        assertThat(mt103Messages.getContent().get(0).getMessageType()).isEqualTo(MessageType.MT103);
        assertThat(mt103Messages.getContent().get(1).getMessageType()).isEqualTo(MessageType.MT103);
        assertThat(mt102Messages.getContent().get(0).getMessageType()).isEqualTo(MessageType.MT102);
    }

    @Test
    void findByMessageType_WithNonexistentType_ShouldReturnEmpty() {
        // Given
        entityManager.persistAndFlush(swiftMessage1);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<SwiftMessage> messages = swiftMessageRepository.findByMessageType(MessageType.MT202, pageable);

        // Then
        assertThat(messages.getContent()).isEmpty();
        assertThat(messages.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findByMessageType_WithPagination_ShouldRespectPageSize() {
        // Given
        entityManager.persistAndFlush(swiftMessage1);
        entityManager.persistAndFlush(swiftMessage3); // Both are MT103
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<SwiftMessage> page = swiftMessageRepository.findByMessageType(MessageType.MT103, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void findAll_ShouldReturnAllMessages() {
        // Given
        entityManager.persistAndFlush(swiftMessage1);
        entityManager.persistAndFlush(swiftMessage2);
        entityManager.persistAndFlush(swiftMessage3);

        // When
        List<SwiftMessage> allMessages = swiftMessageRepository.findAll();

        // Then
        assertThat(allMessages).hasSize(3);
    }

    @Test
    void findById_WithExistingId_ShouldReturnMessage() {
        // Given
        SwiftMessage savedMessage = entityManager.persistAndFlush(swiftMessage1);

        // When
        Optional<SwiftMessage> foundMessage = swiftMessageRepository.findById(savedMessage.getId());

        // Then
        assertThat(foundMessage).isPresent();
        assertThat(foundMessage.get().getMessageType()).isEqualTo(MessageType.MT103);
        assertThat(foundMessage.get().getSenderBic()).isEqualTo("BANKBEBB0000");
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<SwiftMessage> foundMessage = swiftMessageRepository.findById(999L);

        // Then
        assertThat(foundMessage).isEmpty();
    }

    @Test
    void save_ShouldPersistMessage() {
        // Given
        SwiftMessage newMessage = new SwiftMessage();
        newMessage.setMessageType(MessageType.MT202);
        newMessage.setSenderBic("BANKIT2M0000");
        newMessage.setReceiverBic("BANKDEFF0000");
        newMessage.setAmount(new BigDecimal("5000.00"));
        newMessage.setCurrency("EUR");
        newMessage.setValueDate(LocalDate.now());
        newMessage.setRawMtMessage("{1:F01BANKIT2M0000000000}{2:I202BANKDEFFN}{4::20:NEWTEST-}");
        newMessage.setCreatedAt(LocalDateTime.now());
        newMessage.setUpdatedAt(LocalDateTime.now());

        // When
        SwiftMessage savedMessage = swiftMessageRepository.save(newMessage);

        // Then
        assertThat(savedMessage.getId()).isNotNull();
        assertThat(savedMessage.getMessageType()).isEqualTo(MessageType.MT202);
        assertThat(savedMessage.getSenderBic()).isEqualTo("BANKIT2M0000");
    }

    @Test
    void delete_ShouldRemoveMessage() {
        // Given
        SwiftMessage savedMessage = entityManager.persistAndFlush(swiftMessage1);
        Long messageId = savedMessage.getId();

        // When
        swiftMessageRepository.delete(savedMessage);
        entityManager.flush();

        // Then
        Optional<SwiftMessage> deletedMessage = swiftMessageRepository.findById(messageId);
        assertThat(deletedMessage).isEmpty();
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedResults() {
        // Given
        entityManager.persistAndFlush(swiftMessage1); // 1000.00
        entityManager.persistAndFlush(swiftMessage2); // 2000.00
        entityManager.persistAndFlush(swiftMessage3); // 3000.00

        // When
        List<SwiftMessage> messagesSortedByAmountAsc = swiftMessageRepository.findAll(Sort.by("amount").ascending());
        List<SwiftMessage> messagesSortedByAmountDesc = swiftMessageRepository.findAll(Sort.by("amount").descending());

        // Then
        assertThat(messagesSortedByAmountAsc).hasSize(3);
        assertThat(messagesSortedByAmountAsc.get(0).getAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(messagesSortedByAmountAsc.get(2).getAmount()).isEqualTo(new BigDecimal("3000.00"));

        assertThat(messagesSortedByAmountDesc).hasSize(3);
        assertThat(messagesSortedByAmountDesc.get(0).getAmount()).isEqualTo(new BigDecimal("3000.00"));
        assertThat(messagesSortedByAmountDesc.get(2).getAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // Given
        entityManager.persistAndFlush(swiftMessage1);
        entityManager.persistAndFlush(swiftMessage2);

        // When
        long count = swiftMessageRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // Given
        SwiftMessage savedMessage = entityManager.persistAndFlush(swiftMessage1);

        // When
        boolean exists = swiftMessageRepository.existsById(savedMessage.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // When
        boolean exists = swiftMessageRepository.existsById(999L);

        // Then
        assertThat(exists).isFalse();
    }
} 