package com.mtmx.service.converter;

/**
 * Generic message converter interface following SOLID principles
 * 
 * @param <T> Source message type
 * @param <R> Target message type
 */
public interface MessageConverter<T, R> {

    /**
     * Convert source message to target format
     * 
     * @param sourceMessage Source message
     * @return Converted target message
     * @throws ConversionException if conversion fails
     */
    R convert(T sourceMessage) throws ConversionException;

    /**
     * Validate source message before conversion
     * 
     * @param sourceMessage Source message to validate
     * @return true if valid, false otherwise
     */
    boolean isValid(T sourceMessage);

    /**
     * Get supported message type
     * 
     * @return Message type identifier
     */
    String getSupportedMessageType();
}