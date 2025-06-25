package com.mtmx.service.converter;

/**
 * Interface for MX message validation
 */
public interface MxMessageValidator {

    /**
     * Validate MX message against XSD schema
     * 
     * @param mxMessage MX message to validate
     * @return true if valid, false otherwise
     */
    boolean isValid(String mxMessage);

    /**
     * Get message type from MX message
     * 
     * @param mxMessage MX message
     * @return Message type (e.g., "pacs.008.001.08", "pacs.009.001.08")
     */
    String getMessageType(String mxMessage);

    /**
     * Extract value from XML element
     * 
     * @param mxMessage   MX message
     * @param elementPath XPath-like path to element
     * @return Element value or null if not found
     */
    String extractElementValue(String mxMessage, String elementPath);

    /**
     * Extract attribute value from XML element
     * 
     * @param mxMessage     MX message
     * @param elementPath   XPath-like path to element
     * @param attributeName Attribute name
     * @return Attribute value or null if not found
     */
    String extractAttributeValue(String mxMessage, String elementPath, String attributeName);
}