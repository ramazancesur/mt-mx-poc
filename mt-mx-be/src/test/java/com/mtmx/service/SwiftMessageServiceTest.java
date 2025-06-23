package com.mtmx.service;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;
import com.mtmx.repository.SwiftMessageRepository;
import com.mtmx.web.dto.SwiftMessageDto;
import com.mtmx.web.mapper.SwiftMessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SwiftMessageServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SwiftMessageServiceTest {

    @Mock
    private SwiftMessageRepository swiftMessageRepository;

    @Mock
    private SwiftMessageMapper swiftMessageMapper;

    @Mock
    private ConversionService conversionService;

    @Mock
    private XsdValidationService xsdValidationService;

    @InjectMocks
    private SwiftMessageServiceImpl swiftMessageService;

    private SwiftMessage swiftMessage;
    private SwiftMessageDto swiftMessageDto;
    private XsdValidationService.ValidationResult validValidationResult;

    @BeforeEach
    void setUp() {
        swiftMessage = new SwiftMessage();
        swiftMessage.setId(1L);
        swiftMessage.setMessageType(MessageType.MT103);
        swiftMessage.setSenderBic("BANKBEBB");
        swiftMessage.setReceiverBic("BANKDEFF");
        swiftMessage.setAmount(BigDecimal.valueOf(5000.00));
        swiftMessage.setCurrency("EUR");
        swiftMessage.setValueDate(LocalDate.of(2025, 6, 22));
        swiftMessage.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF123-}");
        swiftMessage.setGeneratedMxMessage("<?xml version=\"1.0\"?><Document>...</Document>");
        swiftMessage.setCreatedAt(LocalDateTime.now());
        swiftMessage.setUpdatedAt(LocalDateTime.now());

        swiftMessageDto = new SwiftMessageDto();
        swiftMessageDto.setId(1L);
        swiftMessageDto.setMessageType(MessageType.MT103);
        swiftMessageDto.setSenderBic("BANKBEBB");
        swiftMessageDto.setReceiverBic("BANKDEFF");
        swiftMessageDto.setAmount(BigDecimal.valueOf(5000.00));
        swiftMessageDto.setCurrency("EUR");
        swiftMessageDto.setValueDate(LocalDate.of(2025, 6, 22));
        swiftMessageDto.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I103BANKDEFFN}{4::20:REF123-}");
        swiftMessageDto.setGeneratedMxMessage("<?xml version=\"1.0\"?><Document>...</Document>");
        
        // Mock valid validation result
        validValidationResult = XsdValidationService.ValidationResult.valid("Valid");
    }

    @Test
    void save_WithValidDto_ShouldReturnSavedDto() {
        // Given
        when(swiftMessageMapper.toEntity(any(SwiftMessageDto.class))).thenReturn(swiftMessage);
        when(conversionService.convertMtToMx(anyString())).thenReturn("<?xml>converted</xml>");
        when(xsdValidationService.validateByMtType(anyString(), anyString())).thenReturn(validValidationResult);
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(swiftMessage);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        SwiftMessageDto result = swiftMessageService.save(swiftMessageDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessageType()).isEqualTo(MessageType.MT103);
        verify(swiftMessageRepository).save(any(SwiftMessage.class));
        verify(conversionService).convertMtToMx(anyString());
        verify(xsdValidationService).validateByMtType(anyString(), anyString());
    }

    @Test
    void save_WithBlankMtMessage_ShouldNotCallConversion() {
        // Given
        swiftMessage.setRawMtMessage("");
        swiftMessageDto.setRawMtMessage("");
        when(swiftMessageMapper.toEntity(any(SwiftMessageDto.class))).thenReturn(swiftMessage);
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(swiftMessage);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        SwiftMessageDto result = swiftMessageService.save(swiftMessageDto);

        // Then
        assertThat(result).isNotNull();
        verify(conversionService, never()).convertMtToMx(anyString());
        verify(xsdValidationService, never()).validateByMtType(anyString(), anyString());
    }

    @Test
    void save_WithNullMessageType_ShouldDetermineMessageType() {
        // Given
        swiftMessage.setMessageType(null);
        swiftMessage.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I102BANKDEFFN}{4::20:REF123-}");
        when(swiftMessageMapper.toEntity(any(SwiftMessageDto.class))).thenReturn(swiftMessage);
        when(conversionService.getMessageType(anyString())).thenReturn("102");
        when(conversionService.convertMtToMx(anyString())).thenReturn("<?xml>converted</xml>");
        when(xsdValidationService.validateByMtType(anyString(), anyString())).thenReturn(validValidationResult);
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(swiftMessage);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        SwiftMessageDto result = swiftMessageService.save(swiftMessageDto);

        // Then
        assertThat(result).isNotNull();
        verify(conversionService).convertMtToMx(anyString());
        verify(xsdValidationService).validateByMtType(anyString(), anyString());
    }

    @Test
    void convertMtToMx_WithValidId_ShouldReturnConvertedMessage() {
        // Given
        String newMtMessage = "{1:F01BANKBEBB0000000000}{2:I202BANKDEFFN}{4::20:REF456-}";
        when(swiftMessageRepository.findById(1L)).thenReturn(Optional.of(swiftMessage));
        when(conversionService.getMessageType(anyString())).thenReturn("202");
        when(conversionService.convertMtToMx(anyString())).thenReturn("<?xml>converted MT202</xml>");
        when(xsdValidationService.validateByMtType(anyString(), anyString())).thenReturn(validValidationResult);
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(swiftMessage);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        SwiftMessageDto result = swiftMessageService.convertMtToMx(1L, newMtMessage);

        // Then
        assertThat(result).isNotNull();
        verify(swiftMessageRepository).findById(1L);
        verify(conversionService).convertMtToMx(newMtMessage);
        verify(xsdValidationService).validateByMtType(anyString(), anyString());
        verify(swiftMessageRepository).save(any(SwiftMessage.class));
    }

    @Test
    void convertMtToMx_WithInvalidId_ShouldThrowException() {
        // Given
        when(swiftMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
            swiftMessageService.convertMtToMx(999L, "some message"));
    }

    @Test
    void findAll_ShouldReturnPageOfDtos() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<SwiftMessage> messagePage = new PageImpl<>(Arrays.asList(swiftMessage));
        when(swiftMessageRepository.findAll(pageable)).thenReturn(messagePage);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        Page<SwiftMessageDto> result = swiftMessageService.findAll(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void findByMessageType_ShouldReturnFilteredMessages() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<SwiftMessage> messagePage = new PageImpl<>(Arrays.asList(swiftMessage));
        when(swiftMessageRepository.findByMessageType(MessageType.MT103, pageable)).thenReturn(messagePage);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        Page<SwiftMessageDto> result = swiftMessageService.findByMessageType("MT103", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(swiftMessageRepository).findByMessageType(MessageType.MT103, pageable);
    }

    @Test
    void findByMessageType_WithInvalidType_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<SwiftMessageDto> result = swiftMessageService.findByMessageType("INVALID", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findOne_WithValidId_ShouldReturnDto() {
        // Given
        when(swiftMessageRepository.findById(1L)).thenReturn(Optional.of(swiftMessage));
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        // When
        Optional<SwiftMessageDto> result = swiftMessageService.findOne(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void findOne_WithInvalidId_ShouldReturnEmpty() {
        // Given
        when(swiftMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<SwiftMessageDto> result = swiftMessageService.findOne(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldCallRepository() {
        // When
        swiftMessageService.delete(1L);

        // Then
        verify(swiftMessageRepository).deleteById(1L);
    }

    @Test
    void determineMessageType_ShouldIdentifyCorrectTypes() {
        // Given
        SwiftMessage mt102Message = new SwiftMessage();
        mt102Message.setMessageType(null);
        mt102Message.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I102BANKDEFFN}{4::20:REF123-}");
        
        when(swiftMessageMapper.toEntity(any(SwiftMessageDto.class))).thenReturn(mt102Message);
        when(conversionService.getMessageType(anyString())).thenReturn("102");
        when(conversionService.convertMtToMx(anyString())).thenReturn("<?xml>converted</xml>");
        when(xsdValidationService.validateByMtType(anyString(), anyString())).thenReturn(validValidationResult);
        when(swiftMessageRepository.save(any(SwiftMessage.class))).thenReturn(mt102Message);
        when(swiftMessageMapper.toDto(any(SwiftMessage.class))).thenReturn(swiftMessageDto);

        SwiftMessageDto testDto = new SwiftMessageDto();
        testDto.setRawMtMessage("{1:F01BANKBEBB0000000000}{2:I102BANKDEFFN}{4::20:REF123-}");

        // When
        SwiftMessageDto result = swiftMessageService.save(testDto);

        // Then
        assertThat(result).isNotNull();
        verify(conversionService).getMessageType(anyString());
    }

    @Test
    void save_WithConversionError_ShouldSetErrorMessage() {
        // Given
        when(swiftMessageMapper.toEntity(any(SwiftMessageDto.class))).thenReturn(swiftMessage);
        when(conversionService.convertMtToMx(anyString())).thenThrow(new RuntimeException("Conversion failed"));
        
        // When & Then
        assertThrows(RuntimeException.class, () -> swiftMessageService.save(swiftMessageDto));
    }
}
