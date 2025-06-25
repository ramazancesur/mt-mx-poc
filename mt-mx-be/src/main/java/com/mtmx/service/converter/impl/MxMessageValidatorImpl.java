package com.mtmx.service.converter.impl;

import com.mtmx.service.converter.MxMessageValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of MX message validator using XSD validation
 */
@Component
@Slf4j
public class MxMessageValidatorImpl implements MxMessageValidator {

    private static final Pattern MESSAGE_TYPE_PATTERN = Pattern
            .compile("xmlns=\"urn:iso:std:iso:20022:tech:xsd:([^\"]+)\"");
    private static final Pattern ROOT_ELEMENT_PATTERN = Pattern.compile("<([^\\s>]+)");

    // XSD schema mapping
    private static final Map<String, String> SCHEMA_MAPPING = new HashMap<>();

    static {
        SCHEMA_MAPPING.put("pacs.008.001.08", "pacs.008.001.08.xsd");
        SCHEMA_MAPPING.put("pacs.009.001.08", "pacs.009.001.08.xsd");
        SCHEMA_MAPPING.put("pacs.004.001.02", "pacs.004.001.02.xsd");
    }

    @Override
    public boolean isValid(String mxMessage) {
        if (!StringUtils.hasText(mxMessage)) {
            log.warn("Empty or null MX message provided");
            return false;
        }

        try {
            String messageType = getMessageType(mxMessage);
            if (messageType == null) {
                log.warn("Could not determine MX message type");
                return false;
            }

            String schemaFile = SCHEMA_MAPPING.get(messageType);
            if (schemaFile == null) {
                log.warn("No XSD schema found for message type: {}", messageType);
                return false;
            }

            return validateAgainstXsd(mxMessage, schemaFile);

        } catch (Exception e) {
            log.error("Error validating MX message: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getMessageType(String mxMessage) {
        if (!StringUtils.hasText(mxMessage)) {
            return null;
        }

        Matcher matcher = MESSAGE_TYPE_PATTERN.matcher(mxMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    @Override
    public String extractElementValue(String mxMessage, String elementPath) {
        if (!StringUtils.hasText(mxMessage) || !StringUtils.hasText(elementPath)) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(mxMessage.getBytes()));

            String[] pathParts = elementPath.split("/");
            Element currentElement = document.getDocumentElement();

            for (String part : pathParts) {
                if (currentElement == null) {
                    return null;
                }

                NodeList children = currentElement.getElementsByTagName(part);
                if (children.getLength() == 0) {
                    return null;
                }

                currentElement = (Element) children.item(0);
            }

            return currentElement != null ? currentElement.getTextContent() : null;

        } catch (Exception e) {
            log.error("Error extracting element value: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String extractAttributeValue(String mxMessage, String elementPath, String attributeName) {
        if (!StringUtils.hasText(mxMessage) || !StringUtils.hasText(elementPath)
                || !StringUtils.hasText(attributeName)) {
            return null;
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(mxMessage.getBytes()));

            String[] pathParts = elementPath.split("/");
            Element currentElement = document.getDocumentElement();

            for (String part : pathParts) {
                if (currentElement == null) {
                    return null;
                }

                NodeList children = currentElement.getElementsByTagName(part);
                if (children.getLength() == 0) {
                    return null;
                }

                currentElement = (Element) children.item(0);
            }

            return currentElement != null ? currentElement.getAttribute(attributeName) : null;

        } catch (Exception e) {
            log.error("Error extracting attribute value: {}", e.getMessage(), e);
            return null;
        }
    }

    private boolean validateAgainstXsd(String mxMessage, String schemaFile) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

            // Load XSD schema
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("xsd/" + schemaFile);
            if (schemaStream == null) {
                log.error("XSD schema file not found: {}", schemaFile);
                return false;
            }

            Schema schema = factory.newSchema(new javax.xml.transform.stream.StreamSource(schemaStream));
            javax.xml.validation.Validator validator = schema.newValidator();

            // Parse and validate XML
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new ByteArrayInputStream(mxMessage.getBytes()));

            validator.validate(new DOMSource(document));
            return true;

        } catch (Exception e) {
            log.error("XSD validation failed: {}", e.getMessage(), e);
            return false;
        }
    }
}