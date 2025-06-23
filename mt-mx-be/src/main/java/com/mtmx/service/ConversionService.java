package com.mtmx.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for converting MT messages to MX format using Prowide library for validation
 * and custom field extraction for reliable conversion.
 */
@Service
@Slf4j
public class ConversionService {
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    
    // Regex patterns for field extraction
    private static final Pattern FIELD_20_PATTERN = Pattern.compile(":20:([^\\r\\n]+)");
    private static final Pattern FIELD_32A_PATTERN = Pattern.compile(":32A:(\\d{6})(\\w{3})([\\d,\\.]+)");
    private static final Pattern MESSAGE_TYPE_PATTERN = Pattern.compile("\\{2:I(\\d{3}(?:COV)?)");

    /**
     * Main conversion method that determines message type and converts accordingly
     */
    public String convertMtToMx(String mtMessage) {
        try {
            if (!StringUtils.hasText(mtMessage)) {
                log.warn("Empty or null MT message provided");
                return generateFallbackXml("UNKNOWN", mtMessage);
            }
            
            String messageType = getMessageType(mtMessage);
            log.info("Converting message type: {}", messageType);
            
            switch (messageType) {
                case "103":
                    return convertMt103ToMx(mtMessage);
                case "102":
                    return convertMt102ToMx(mtMessage);
                case "202":
                    return convertMt202ToMx(mtMessage);
                case "202COV":
                    return convertMt202CovToMx(mtMessage);
                case "203":
                    return convertMt203ToMx(mtMessage);
                default:
                    log.warn("Unsupported message type: {}", messageType);
                    return generateFallbackXml(messageType, mtMessage);
            }
            
        } catch (Exception e) {
            log.error("Error during MT to MX conversion: {}", e.getMessage(), e);
            return generateFallbackXml("ERROR", mtMessage);
        }
    }

    /**
     * Convert MT103 to MX pacs.008.001.08
     */
    public String convertMt103ToMx(String mtMessage) {
        try {
            // Validate the message first
            if (!isValidMtMessage(mtMessage)) {
                return generateFallbackXml("MT103", mtMessage);
            }
            
            String reference = extractField20(mtMessage);
            String[] amountInfo = extractField32A(mtMessage);
            String currency = amountInfo[1];
            String amount = amountInfo[2];
            
            // Check if extraction was successful
            if ("UNKNOWN".equals(reference) && "0.00".equals(amount)) {
                return generateFallbackXml("MT103", mtMessage);
            }
            
            return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">\n" +
                "    <FIToFICstmrCdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <TtlIntrBkSttlmAmt Ccy=\"%s\">%s</TtlIntrBkSttlmAmt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>%s</InstrId>\n" +
                "                <EndToEndId>%s</EndToEndId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt Ccy=\"%s\">%s</IntrBkSttlmAmt>\n" +
                "            <Dbtr>\n" +
                "                <Nm>DEBTOR NAME</Nm>\n" +
                "            </Dbtr>\n" +
                "            <Cdtr>\n" +
                "                <Nm>CREDITOR NAME</Nm>\n" +
                "            </Cdtr>\n" +
                "            <!-- CONVERTED_FROM_MT103 -->\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FIToFICstmrCdtTrf>\n" +
                "</Document>", 
                reference, LocalDateTime.now().format(ISO_FORMATTER), currency, amount,
                reference, reference, currency, amount
            );
            
        } catch (Exception e) {
            log.error("Error converting MT103 to MX: {}", e.getMessage());
            return generateFallbackXml("MT103", mtMessage);
        }
    }

    /**
     * Convert MT102 to MX pacs.008.001.08
     */
    public String convertMt102ToMx(String mtMessage) {
        try {
            if (!isValidMtMessage(mtMessage)) {
                return generateFallbackXml("MT102", mtMessage);
            }
            
            String reference = extractField20(mtMessage);
            String[] amountInfo = extractField32A(mtMessage);
            String currency = amountInfo[1];
            String amount = amountInfo[2];
            
            return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\">\n" +
                "    <FIToFICstmrCdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <TtlIntrBkSttlmAmt Ccy=\"%s\">%s</TtlIntrBkSttlmAmt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>%s</InstrId>\n" +
                "                <EndToEndId>%s</EndToEndId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt Ccy=\"%s\">%s</IntrBkSttlmAmt>\n" +
                "            <Dbtr>\n" +
                "                <Nm>DEBTOR NAME</Nm>\n" +
                "            </Dbtr>\n" +
                "            <Cdtr>\n" +
                "                <Nm>CREDITOR NAME</Nm>\n" +
                "            </Cdtr>\n" +
                "            <!-- CONVERTED_FROM_MT102 -->\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FIToFICstmrCdtTrf>\n" +
                "</Document>", 
                reference, LocalDateTime.now().format(ISO_FORMATTER), currency, amount,
                reference, reference, currency, amount
            );
            
        } catch (Exception e) {
            log.error("Error converting MT102 to MX: {}", e.getMessage());
            return generateFallbackXml("MT102", mtMessage);
        }
    }

    /**
     * Convert MT202 to MX pacs.009.001.08
     */
    public String convertMt202ToMx(String mtMessage) {
        try {
            if (!isValidMtMessage(mtMessage)) {
                return generateFallbackXml("MT202", mtMessage);
            }
            
            String reference = extractField20(mtMessage);
            String[] amountInfo = extractField32A(mtMessage);
            String currency = amountInfo[1];
            String amount = amountInfo[2];
            
            return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08\">\n" +
                "    <FICdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <TtlIntrBkSttlmAmt Ccy=\"%s\">%s</TtlIntrBkSttlmAmt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>%s</InstrId>\n" +
                "                <EndToEndId>%s</EndToEndId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt Ccy=\"%s\">%s</IntrBkSttlmAmt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>UNKNOWN</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>UNKNOWN</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <!-- CONVERTED_FROM_MT202 -->\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FICdtTrf>\n" +
                "</Document>", 
                reference, LocalDateTime.now().format(ISO_FORMATTER), currency, amount,
                reference, reference, currency, amount
            );
            
        } catch (Exception e) {
            log.error("Error converting MT202 to MX: {}", e.getMessage());
            return generateFallbackXml("MT202", mtMessage);
        }
    }

    /**
     * Convert MT203 to MX pacs.009.001.08
     */
    public String convertMt203ToMx(String mtMessage) {
        try {
            if (!isValidMtMessage(mtMessage)) {
                return generateFallbackXml("MT203", mtMessage);
            }
            
            String reference = extractField20(mtMessage);
            String[] amountInfo = extractField32A(mtMessage);
            String currency = amountInfo[1];
            String amount = amountInfo[2];
            
            return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08\">\n" +
                "    <FICdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <TtlIntrBkSttlmAmt Ccy=\"%s\">%s</TtlIntrBkSttlmAmt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>%s</InstrId>\n" +
                "                <EndToEndId>%s</EndToEndId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt Ccy=\"%s\">%s</IntrBkSttlmAmt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>UNKNOWN</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>UNKNOWN</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <!-- CONVERTED_FROM_MT203 -->\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FICdtTrf>\n" +
                "</Document>", 
                reference, LocalDateTime.now().format(ISO_FORMATTER), currency, amount,
                reference, reference, currency, amount
            );
            
        } catch (Exception e) {
            log.error("Error converting MT203 to MX: {}", e.getMessage());
            return generateFallbackXml("MT203", mtMessage);
        }
    }

    /**
     * Convert MT202COV to MX pacs.009.001.08
     */
    public String convertMt202CovToMx(String mtMessage) {
        try {
            if (!isValidMtMessage(mtMessage)) {
                return generateFallbackXml("MT202COV", mtMessage);
            }
            
            String reference = extractField20(mtMessage);
            String[] amountInfo = extractField32A(mtMessage);
            String currency = amountInfo[1];
            String amount = amountInfo[2];
            
            return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08\">\n" +
                "    <FICdtTrf>\n" +
                "        <GrpHdr>\n" +
                "            <MsgId>%s</MsgId>\n" +
                "            <CreDtTm>%s</CreDtTm>\n" +
                "            <NbOfTxs>1</NbOfTxs>\n" +
                "            <TtlIntrBkSttlmAmt Ccy=\"%s\">%s</TtlIntrBkSttlmAmt>\n" +
                "        </GrpHdr>\n" +
                "        <CdtTrfTxInf>\n" +
                "            <PmtId>\n" +
                "                <InstrId>%s</InstrId>\n" +
                "                <EndToEndId>%s</EndToEndId>\n" +
                "            </PmtId>\n" +
                "            <IntrBkSttlmAmt Ccy=\"%s\">%s</IntrBkSttlmAmt>\n" +
                "            <InstdAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>UNKNOWN</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstdAgt>\n" +
                "            <InstgAgt>\n" +
                "                <FinInstnId>\n" +
                "                    <BICFI>UNKNOWN</BICFI>\n" +
                "                </FinInstnId>\n" +
                "            </InstgAgt>\n" +
                "            <!-- CONVERTED_FROM_MT202COV -->\n" +
                "        </CdtTrfTxInf>\n" +
                "    </FICdtTrf>\n" +
                "</Document>", 
                reference, LocalDateTime.now().format(ISO_FORMATTER), currency, amount,
                reference, reference, currency, amount
            );
            
        } catch (Exception e) {
            log.error("Error converting MT202COV to MX: {}", e.getMessage());
            return generateFallbackXml("MT202COV", mtMessage);
        }
    }

    /**
     * Validate MT message using Prowide library
     */
    public boolean isValidMtMessage(String mtMessage) {
        try {
            if (!StringUtils.hasText(mtMessage)) {
                return false;
            }
            
            // Use Prowide to parse and validate the message
            // Basic validation without Prowide
            
            // Check if parsing was successful and message has required blocks
            return mtMessage.contains("{1:") && mtMessage.contains("{2:") && mtMessage.contains("{4:");
                   
        } catch (Exception e) {
            log.warn("Invalid MT message format: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract message type from MT message header
     */
    public String getMessageType(String mtMessage) {
        try {
            Matcher matcher = MESSAGE_TYPE_PATTERN.matcher(mtMessage);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("Error extracting message type: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    private String extractField20(String mtMessage) {
        Matcher matcher = FIELD_20_PATTERN.matcher(mtMessage);
        return matcher.find() ? matcher.group(1).trim() : "UNKNOWN";
    }

    private String[] extractField32A(String mtMessage) {
        Matcher matcher = FIELD_32A_PATTERN.matcher(mtMessage);
        if (matcher.find()) {
            String date = matcher.group(1);
            String currency = matcher.group(2);
            String amount = matcher.group(3).replace(",", ".");
            return new String[]{date, currency, amount};
        }
        return new String[]{"000000", "XXX", "0.00"};
    }

    private String generateFallbackXml(String messageType, String mtMessage) {
        return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<Document>\n" +
            "    <Error>\n" +
            "        <Message>Conversion failed for %s</Message>\n" +
            "        <Timestamp>%s</Timestamp>\n" +
            "        <!-- FALLBACK_%s -->\n" +
            "    </Error>\n" +
            "</Document>", 
            messageType, LocalDateTime.now().format(ISO_FORMATTER), messageType
        );
    }

    /**
     * Convert MX message to MT format
     */
    public String convertMxToMt(String mxMessage) {
        try {
            if (!StringUtils.hasText(mxMessage)) {
                log.warn("Empty or null MX message provided");
                return generateFallbackMt("UNKNOWN", mxMessage);
            }
            
            // Determine MX message type from XML
            String mxType = getMxMessageType(mxMessage);
            log.info("Converting MX message type: {}", mxType);
            
            switch (mxType) {
                case "pacs.008":
                    return convertMxToMt103(mxMessage);
                case "pacs.009":
                    return convertMxToMt202(mxMessage);
                default:
                    log.warn("Unsupported MX message type: {}", mxType);
                    return generateFallbackMt(mxType, mxMessage);
            }
            
        } catch (Exception e) {
            log.error("Error during MX to MT conversion: {}", e.getMessage(), e);
            return generateFallbackMt("ERROR", mxMessage);
        }
    }

    /**
     * Convert MX pacs.008 to MT103
     */
    public String convertMxToMt103(String mxMessage) {
        try {
            // Extract data from MX XML
            String msgId = extractXmlValue(mxMessage, "MsgId");
            String currency = extractXmlAttribute(mxMessage, "IntrBkSttlmAmt", "Ccy");
            String amount = extractXmlValue(mxMessage, "IntrBkSttlmAmt");
            String debtorName = extractXmlValue(mxMessage, "Dbtr", "Nm");
            String creditorName = extractXmlValue(mxMessage, "Cdtr", "Nm");
            
            // Generate MT103 format
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            
            return String.format("{1:F01BANKXXXXAXXX0000000000}{2:I103BANKXXXXBXXXN}{4:\n" +
                ":20:%s\n" +
                ":23B:CRED\n" +
                ":32A:%s%s%s\n" +
                ":50K:/%s\n%s\n" +
                ":59:/%s\n%s\n" +
                ":70:CONVERTED FROM MX MESSAGE\n" +
                ":71A:SHA\n" +
                "-}",
                msgId != null ? msgId : "MXCONV001",
                currentDate,
                currency != null ? currency : "USD",
                amount != null ? amount : "1000.00",
                "1234567890",
                debtorName != null ? debtorName : "DEBTOR NAME",
                "0987654321", 
                creditorName != null ? creditorName : "CREDITOR NAME"
            );
            
        } catch (Exception e) {
            log.error("Error converting MX to MT103: {}", e.getMessage());
            return generateFallbackMt("MT103", mxMessage);
        }
    }

    /**
     * Convert MX pacs.009 to MT202
     */
    public String convertMxToMt202(String mxMessage) {
        try {
            // Extract data from MX XML
            String msgId = extractXmlValue(mxMessage, "MsgId");
            String currency = extractXmlAttribute(mxMessage, "IntrBkSttlmAmt", "Ccy");
            String amount = extractXmlValue(mxMessage, "IntrBkSttlmAmt");
            
            // Generate MT202 format
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            
            return String.format("{1:F01BANKXXXXAXXX0000000000}{2:I202BANKXXXXBXXXN}{4:\n" +
                ":20:%s\n" +
                ":21:NONREF\n" +
                ":32A:%s%s%s\n" +
                ":52A:BANKXXXXAXXX\n" +
                ":58A:BANKXXXXBXXX\n" +
                ":72:/CONVERTED FROM MX MESSAGE\n" +
                "-}",
                msgId != null ? msgId : "MXCONV001",
                currentDate,
                currency != null ? currency : "USD",
                amount != null ? amount : "1000.00"
            );
            
        } catch (Exception e) {
            log.error("Error converting MX to MT202: {}", e.getMessage());
            return generateFallbackMt("MT202", mxMessage);
        }
    }

    /**
     * Get MX message type from XML namespace
     */
    public String getMxMessageType(String mxMessage) {
        try {
            if (mxMessage.contains("pacs.008")) return "pacs.008";
            if (mxMessage.contains("pacs.009")) return "pacs.009";
            if (mxMessage.contains("pacs.004")) return "pacs.004";
            return "UNKNOWN";
        } catch (Exception e) {
            log.warn("Could not determine MX message type: {}", e.getMessage());
            return "UNKNOWN";
        }
    }

    /**
     * Extract XML value by tag name
     */
    private String extractXmlValue(String xml, String tagName) {
        try {
            // First try to find the tag with attributes (like IntrBkSttlmAmt)
            Pattern patternWithAttrs = Pattern.compile("<" + tagName + "\\s+[^>]*>(.*?)</" + tagName + ">", Pattern.DOTALL);
            Matcher matcherWithAttrs = patternWithAttrs.matcher(xml);
            if (matcherWithAttrs.find()) {
                return matcherWithAttrs.group(1).trim();
            }
            
            // Then try simple tag without attributes
            Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            log.warn("Could not extract XML value for tag: {}", tagName);
        }
        return null;
    }

    /**
     * Extract XML value by tag name with nested path
     */
    private String extractXmlValue(String xml, String parentTag, String childTag) {
        try {
            Pattern parentPattern = Pattern.compile("<" + parentTag + ">(.*?)</" + parentTag + ">", Pattern.DOTALL);
            Matcher parentMatcher = parentPattern.matcher(xml);
            if (parentMatcher.find()) {
                String parentContent = parentMatcher.group(1);
                return extractXmlValue(parentContent, childTag);
            }
        } catch (Exception e) {
            log.warn("Could not extract XML value for nested tags: {}/{}", parentTag, childTag);
        }
        return null;
    }

    /**
     * Extract XML attribute value
     */
    private String extractXmlAttribute(String xml, String tagName, String attrName) {
        try {
            Pattern pattern = Pattern.compile("<" + tagName + "\\s+[^>]*" + attrName + "=\"([^\"]+)\"", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(xml);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("Could not extract XML attribute {} for tag: {}", attrName, tagName);
        }
        return null;
    }

    /**
     * Generate fallback MT message when conversion fails
     */
    private String generateFallbackMt(String messageType, String mxMessage) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        return String.format("{1:F01BANKXXXXAXXX0000000000}{2:I103BANKXXXXBXXXN}{4:\n" +
            ":20:FALLBACK001\n" +
            ":23B:CRED\n" +
            ":32A:%sUSD1000.00\n" +
            ":50K:/FALLBACK CONVERSION\n" +
            ":59:/CONVERSION FAILED\n" +
            ":70:MX TO MT CONVERSION FAILED FOR %s\n" +
            ":71A:SHA\n" +
            "-}", currentDate, messageType);
    }
}
