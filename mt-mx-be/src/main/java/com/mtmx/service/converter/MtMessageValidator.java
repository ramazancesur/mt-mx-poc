package com.mtmx.service.converter;

/**
 * Interface for MT message validation
 */
public interface MtMessageValidator {

    /**
     * Validate MT message format and content
     * 
     * @param mtMessage MT message to validate
     * @return true if valid, false otherwise
     */
    boolean isValid(String mtMessage);

    /**
     * Extract message type from MT message
     * 
     * @param mtMessage MT message
     * @return Message type (e.g., "103", "202", etc.)
     */
    String getMessageType(String mtMessage);

    /**
     * Extract specific field from MT message
     * 
     * @param mtMessage MT message
     * @param fieldName Field name (e.g., "20", "32A")
     * @return Field value or null if not found
     */
    String extractField(String mtMessage, String fieldName);
}