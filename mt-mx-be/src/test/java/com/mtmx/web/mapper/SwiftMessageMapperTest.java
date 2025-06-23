package com.mtmx.web.mapper;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.web.dto.SwiftMessageDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftMessageMapperTest {

    private final SwiftMessageMapper mapper = Mappers.getMapper(SwiftMessageMapper.class);

    @Test
    void toDto_WithValidEntity_ShouldMapCorrectly() {
        // Given
        SwiftMessage entity = new SwiftMessage();
        entity.setId(1L);
        entity.setMessageType(MessageType.MT103);
        entity.setSenderBic("BANKBEBB");
        entity.setReceiverBic("BANKDEFF");
        entity.setAmount(BigDecimal.valueOf(1000.50));
        entity.setCurrency("EUR");
        entity.setValueDate(LocalDate.of(2025, 6, 22));
        entity.setRawMtMessage("Raw MT message");
        entity.setGeneratedMxMessage("Generated MX message");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // When
        SwiftMessageDto dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getMessageType()).isEqualTo(MessageType.MT103);
        assertThat(dto.getSenderBic()).isEqualTo("BANKBEBB");
        assertThat(dto.getReceiverBic()).isEqualTo("BANKDEFF");
        assertThat(dto.getAmount()).isEqualTo(BigDecimal.valueOf(1000.50));
        assertThat(dto.getCurrency()).isEqualTo("EUR");
        assertThat(dto.getValueDate()).isEqualTo(LocalDate.of(2025, 6, 22));
        assertThat(dto.getRawMtMessage()).isEqualTo("Raw MT message");
        assertThat(dto.getGeneratedMxMessage()).isEqualTo("Generated MX message");
    }

    @Test
    void toEntity_WithValidDto_ShouldMapCorrectly() {
        // Given
        SwiftMessageDto dto = new SwiftMessageDto();
        dto.setId(1L);
        dto.setMessageType(MessageType.MT102);
        dto.setSenderBic("BANKDEFF");
        dto.setReceiverBic("BANKBEBB");
        dto.setAmount(BigDecimal.valueOf(2000.75));
        dto.setCurrency("USD");
        dto.setValueDate(LocalDate.of(2025, 6, 23));
        dto.setRawMtMessage("Raw MT message");
        dto.setGeneratedMxMessage("Generated MX message");

        // When
        SwiftMessage entity = mapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getMessageType()).isEqualTo(MessageType.MT102);
        assertThat(entity.getSenderBic()).isEqualTo("BANKDEFF");
        assertThat(entity.getReceiverBic()).isEqualTo("BANKBEBB");
        assertThat(entity.getAmount()).isEqualTo(BigDecimal.valueOf(2000.75));
        assertThat(entity.getCurrency()).isEqualTo("USD");
        assertThat(entity.getValueDate()).isEqualTo(LocalDate.of(2025, 6, 23));
        assertThat(entity.getRawMtMessage()).isEqualTo("Raw MT message");
        assertThat(entity.getGeneratedMxMessage()).isEqualTo("Generated MX message");
    }

    @Test
    void toDto_WithNullEntity_ShouldReturnNull() {
        // When
        SwiftMessageDto dto = mapper.toDto(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    void toEntity_WithNullDto_ShouldReturnNull() {
        // When
        SwiftMessage entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    void toDto_WithMinimalEntity_ShouldMapCorrectly() {
        // Given
        SwiftMessage entity = new SwiftMessage();
        entity.setId(2L);
        entity.setMessageType(MessageType.MT202);
        entity.setSenderBic("BANKTEST");
        entity.setReceiverBic("BANKPROD");

        // When
        SwiftMessageDto dto = mapper.toDto(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getMessageType()).isEqualTo(MessageType.MT202);
        assertThat(dto.getSenderBic()).isEqualTo("BANKTEST");
        assertThat(dto.getReceiverBic()).isEqualTo("BANKPROD");
        assertThat(dto.getAmount()).isNull();
        assertThat(dto.getCurrency()).isNull();
        assertThat(dto.getValueDate()).isNull();
    }
}
