package com.mtmx.service;

import com.mtmx.web.dto.SwiftMessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.mtmx.domain.entity.SwiftMessage}.
 */
public interface SwiftMessageService {

    /**
     * Save a swiftMessage.
     * @param swiftMessageDto the entity to save.
     * @return the persisted entity.
     */
    SwiftMessageDto save(SwiftMessageDto swiftMessageDto);

    /**
     * Convert MT message to MX format using existing message content.
     * @param id the id of the message.
     * @return the updated entity with MX message.
     */
    SwiftMessageDto convertMtToMx(Long id);

    /**
     * Convert MT message to MX format with provided content.
     * @param id the id of the message.
     * @param rawMtMessage the raw MT message content.
     * @return the updated entity with MX message.
     */
    SwiftMessageDto convertMtToMx(Long id, String rawMtMessage);

    /**
     * Convert MX message to MT format using existing message content.
     * @param id the id of the message.
     * @return the updated entity with MT message.
     */
    SwiftMessageDto convertMxToMt(Long id);

    /**
     * Get all the swiftMessages with pagination.
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<SwiftMessageDto> findAll(Pageable pageable);

    /**
     * Get all messages by message type with pagination.
     * @param messageType the message type to filter by.
     * @param pageable the pagination information.
     * @return the page of entities.
     */
    Page<SwiftMessageDto> findByMessageType(String messageType, Pageable pageable);

    /**
     * Get the "id" swiftMessage.
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SwiftMessageDto> findOne(Long id);

    /**
     * Delete the "id" swiftMessage.
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Update XML content of an existing message.
     * @param id the id of the message.
     * @param xmlContent the new XML content.
     * @return the updated entity.
     */
    SwiftMessageDto updateXmlContent(Long id, String xmlContent);
} 