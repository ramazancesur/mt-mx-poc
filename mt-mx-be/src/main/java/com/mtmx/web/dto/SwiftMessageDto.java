package com.mtmx.web.dto;

import com.mtmx.domain.enums.MessageType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for representing a SwiftMessage.
 */
@Data
public class SwiftMessageDto {

    private Long id;
    private MessageType messageType;
    private String senderBic;
    private String receiverBic;
    private BigDecimal amount;
    private String currency;
    private LocalDate valueDate;
    private String rawMtMessage;
    private String generatedMxMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 