package com.mtmx.service.converter.impl;

import com.mtmx.domain.model.mx.Pacs008Message;
import com.mtmx.service.converter.ConversionException;
import com.mtmx.service.converter.MessageConverter;
import com.mtmx.service.converter.MxMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Converter for MX pacs.008.001.08 to MT103
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MxToMt103Converter implements MessageConverter<String, String> {

    private final MxMessageValidator mxMessageValidator;

    private static final DateTimeFormatter SWIFT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    @Override
    public String convert(String mxMessage) throws ConversionException {
        if (!isValid(mxMessage)) {
            throw new ConversionException("Invalid MX pacs.008.001.08 message");
        }

        try {
            // Unmarshal XML to JAXB object
            Pacs008Message pacs008Message = unmarshalFromXml(mxMessage);

            // Extract data from JAXB object
            String reference = extractReference(pacs008Message);
            String amount = extractAmount(pacs008Message);
            String currency = extractCurrency(pacs008Message);

            if (reference == null || amount == null || currency == null) {
                throw new ConversionException("Required fields not found in MX message");
            }

            // Create MT103 message
            return createMt103Message(reference, amount, currency);

        } catch (Exception e) {
            log.error("Error converting MX to MT103: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MX to MT103", e);
        }
    }

    @Override
    public boolean isValid(String mxMessage) {
        if (!mxMessageValidator.isValid(mxMessage)) {
            return false;
        }

        String messageType = mxMessageValidator.getMessageType(mxMessage);
        return "pacs.008.001.08".equals(messageType);
    }

    @Override
    public String getSupportedMessageType() {
        return "pacs.008.001.08";
    }

    private Pacs008Message unmarshalFromXml(String mxMessage) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Pacs008Message.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Pacs008Message) unmarshaller.unmarshal(new StringReader(mxMessage));
    }

    private String extractReference(Pacs008Message message) {
        if (message.getFiToFICstmrCdtTrf() != null &&
                message.getFiToFICstmrCdtTrf().getCdtTrfTxInf() != null &&
                !message.getFiToFICstmrCdtTrf().getCdtTrfTxInf().isEmpty()) {

            Pacs008Message.CreditTransferTransactionInformation txInfo = message.getFiToFICstmrCdtTrf().getCdtTrfTxInf()
                    .get(0);

            if (txInfo.getPmtId() != null) {
                return txInfo.getPmtId().getEndToEndId();
            }
        }
        return null;
    }

    private String extractAmount(Pacs008Message message) {
        if (message.getFiToFICstmrCdtTrf() != null &&
                message.getFiToFICstmrCdtTrf().getCdtTrfTxInf() != null &&
                !message.getFiToFICstmrCdtTrf().getCdtTrfTxInf().isEmpty()) {

            Pacs008Message.CreditTransferTransactionInformation txInfo = message.getFiToFICstmrCdtTrf().getCdtTrfTxInf()
                    .get(0);

            if (txInfo.getIntrBkSttlmAmt() != null && txInfo.getIntrBkSttlmAmt().getValue() != null) {
                return txInfo.getIntrBkSttlmAmt().getValue().toString();
            }
        }
        return null;
    }

    private String extractCurrency(Pacs008Message message) {
        if (message.getFiToFICstmrCdtTrf() != null &&
                message.getFiToFICstmrCdtTrf().getCdtTrfTxInf() != null &&
                !message.getFiToFICstmrCdtTrf().getCdtTrfTxInf().isEmpty()) {

            Pacs008Message.CreditTransferTransactionInformation txInfo = message.getFiToFICstmrCdtTrf().getCdtTrfTxInf()
                    .get(0);

            if (txInfo.getIntrBkSttlmAmt() != null) {
                return txInfo.getIntrBkSttlmAmt().getCcy();
            }
        }
        return null;
    }

    private String createMt103Message(String reference, String amount, String currency) {
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(SWIFT_DATE_FORMATTER);

        StringBuilder mtMessage = new StringBuilder();
        mtMessage.append("{1:F01BANKTRISAXXX1234567890}");
        mtMessage.append("{2:I103BANKTRISAXXXN}");
        mtMessage.append("{3:{108:MT103CONVERSION}}");
        mtMessage.append("{4:");
        mtMessage.append(":20:").append(reference).append("\r\n");
        mtMessage.append(":32A:").append(date).append(currency).append(amount).append("\r\n");
        mtMessage.append(":50K:/1234567890123456\r\n");
        mtMessage.append("DEBTOR NAME\r\n");
        mtMessage.append("DEBTOR ADDRESS\r\n");
        mtMessage.append(":59:/9876543210987654\r\n");
        mtMessage.append("CREDITOR NAME\r\n");
        mtMessage.append("CREDITOR ADDRESS\r\n");
        mtMessage.append(":71A:SHA\r\n");
        mtMessage.append(":72:/ACC/PAYMENT DETAILS\r\n");
        mtMessage.append("-}");

        return mtMessage.toString();
    }
}