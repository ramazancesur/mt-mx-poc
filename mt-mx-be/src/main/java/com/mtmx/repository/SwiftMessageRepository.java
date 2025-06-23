package com.mtmx.repository;

import com.mtmx.domain.entity.SwiftMessage;
import com.mtmx.domain.enums.MessageType;import com.mtmx.domain.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link SwiftMessage} entity.
 * Provides standard CRUD operations and pagination support out of the box.
 */
@Repository
public interface SwiftMessageRepository extends JpaRepository<SwiftMessage, Long> {
    
    /**
     * Find all messages by message type with pagination.
     * @param messageType the message type to filter by.
     * @param pageable the pagination information.
     * @return the page of entities.
     */
    Page<SwiftMessage> findByMessageType(MessageType messageType, Pageable pageable);
} 