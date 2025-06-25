package com.mtmx.service.converter.impl;

import com.mtmx.service.converter.MtMessageValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of MT message validator
 */
@Component
@Slf4j
public class MtMessageValidatorImpl implements MtMessageValidator {

    // Regex patterns for field extraction
    private static final Pattern FIELD_20_PATTERN = Pattern.compile(":20:([^\\r\\n]+)");
    private static final Pattern FIELD_32A_PATTERN = Pattern.compile(":32A:(\\d{6})(\\w{3})([\\d,\\.]+)");
    private static final Pattern MESSAGE_TYPE_PATTERN = Pattern.compile("\\{2:I(\\d{3}(?:COV)?)");
    private static final Pattern GENERAL_FIELD_PATTERN = Pattern.compile(":(\\d{2}[A-Z]?):([^\\r\\n]+)");

    @Override
    public boolean isValid(String mtMessage) {
        if (!StringUtils.hasText(mtMessage)) {
            log.warn("Empty or null MT message provided");
            return false;
        }

        // Check if message has basic SWIFT format - {3: is optional
        if (!mtMessage.contains("{1:") || !mtMessage.contains("{2:") || !mtMessage.contains("{4:")) {
            log.warn("Invalid SWIFT message format - missing required blocks");
            return false;
        }

        // Check if message type is present
        String messageType = getMessageType(mtMessage);
        if (messageType == null || messageType.isEmpty()) {
            log.warn("Could not extract message type from MT message");
            return false;
        }

        return true;
    }

    @Override
    public String getMessageType(String mtMessage) {
        if (!StringUtils.hasText(mtMessage)) {
            return null;
        }

        Matcher matcher = MESSAGE_TYPE_PATTERN.matcher(mtMessage);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    @Override
    public String extractField(String mtMessage, String fieldName) {
        if (!StringUtils.hasText(mtMessage) || !StringUtils.hasText(fieldName)) {
            return null;
        }

        Pattern pattern = Pattern.compile(":" + fieldName + ":([^\\r\\n]+)");
        Matcher matcher = pattern.matcher(mtMessage);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
}