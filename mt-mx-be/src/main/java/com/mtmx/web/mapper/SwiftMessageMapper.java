package com.mtmx.web.mapper;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.web.dto.SwiftMessageDto;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper for the entity {@link SwiftMessage} and its DTO
 * {@link SwiftMessageDto}.
 * Uses MapStruct for automatic implementation.
 * Spring component model is used for dependency injection.
 */
@Mapper(componentModel = "spring")
public interface SwiftMessageMapper {

    SwiftMessageDto toDto(SwiftMessage swiftMessage);

    SwiftMessage toEntity(SwiftMessageDto swiftMessageDto);

    List<SwiftMessageDto> toDtoList(List<SwiftMessage> swiftMessages);

    List<SwiftMessage> toEntityList(List<SwiftMessageDto> swiftMessageDtos);
}