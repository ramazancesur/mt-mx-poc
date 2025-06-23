package com.mtmx.domain.entity;

import com.mtmx.domain.enums.MessageType;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single SWIFT message record in the database.
 */
@Entity
@Table(name = "swift_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwiftMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageType messageType;

    @Column(nullable = false, length = 12)
    private String senderBic;

    @Column(nullable = false, length = 12)
    private String receiverBic;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    private LocalDate valueDate;

    @Column(name = "raw_mt_message", columnDefinition = "TEXT")
    private String rawMtMessage;

    @Column(name = "generated_mx_message", columnDefinition = "TEXT")
    private String generatedMxMessage;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
