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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Converter for MX pacs.008.001.08 to MT102 (Multiple Customer Credit Transfer)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MxToMt102Converter implements MessageConverter<String, String> {

    private final MxMessageValidator mxMessageValidator;

    private static final DateTimeFormatter MT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    @Override
    public String convert(String mxMessage) throws ConversionException {
        if (!isValid(mxMessage)) {
            throw new ConversionException("Invalid MX message for MT102 conversion");
        }

        try {
            // Parse MX message
            Pacs008Message pacs008 = unmarshalFromXml(mxMessage);

            // Extract data from MX message
            return createMt102Message(pacs008);

        } catch (Exception e) {
            log.error("Error converting MX to MT102: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MX to MT102", e);
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

    private Pacs008Message unmarshalFromXml(String xmlMessage) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Pacs008Message.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xmlMessage);
        return (Pacs008Message) unmarshaller.unmarshal(reader);
    }

    private String createMt102Message(Pacs008Message pacs008) {
        StringBuilder mt102 = new StringBuilder();

        // Basic header
        mt102.append("{1:F01BANKDEFFXXXX0000000000}\n");
        mt102.append("{2:I102BANKUS33XXXXN}\n");
        mt102.append("{4:\n");

        // Extract group header and transaction info
        Pacs008Message.FIToFICstmrCdtTrf fiToFi = pacs008.getFiToFICstmrCdtTrf();
        Pacs008Message.GroupHeader groupHeader = fiToFi.getGrpHdr();

        // Field 20: Transaction Reference Number
        String msgId = groupHeader.getMsgId();
        if (msgId != null && !msgId.isEmpty()) {
            mt102.append(":20:").append(msgId).append("\n");
        } else {
            mt102.append(":20:").append("NOTPROVIDED").append("\n");
        }

        // Field 23: Bank Operation Code (default for credit transfers)
        mt102.append(":23:CRED\n");

        // Extract first transaction for basic info
        if (fiToFi.getCdtTrfTxInf() != null && !fiToFi.getCdtTrfTxInf().isEmpty()) {
            Pacs008Message.CreditTransferTransactionInformation txInfo = fiToFi.getCdtTrfTxInf().get(0);

            // Field 32A: Value Date/Currency/Interbank Settled Amount
            String valueDate = LocalDate.now().format(MT_DATE_FORMATTER);
            Pacs008Message.ActiveOrHistoricCurrencyAndAmount settlementAmount = groupHeader.getTtlIntrBkSttlmAmt();
            if (settlementAmount != null) {
                String currency = settlementAmount.getCcy();
                BigDecimal amount = settlementAmount.getValue();
                mt102.append(":32A:").append(valueDate).append(currency).append(amount.toPlainString()).append("\n");
            }

            // Field 19: Sum of Amounts
            Pacs008Message.ActiveOrHistoricCurrencyAndAmount txAmount = txInfo.getIntrBkSttlmAmt();
            if (txAmount != null) {
                mt102.append(":19:").append(txAmount.getValue().toPlainString()).append("\n");
            }

            // Field 50K: Ordering Customer (from debtor)
            Pacs008Message.PartyIdentification43 debtor = txInfo.getDbtr();
            if (debtor != null && debtor.getNm() != null) {
                mt102.append(":50K:/").append("NOTPROVIDED").append("\n");
                mt102.append(debtor.getNm()).append("\n");
            }

            // Field 59: Beneficiary Customer (from creditor)
            Pacs008Message.PartyIdentification43 creditor = txInfo.getCdtr();
            if (creditor != null && creditor.getNm() != null) {
                mt102.append(":59:/").append("NOTPROVIDED").append("\n");
                mt102.append(creditor.getNm()).append("\n");
            }

            // Field 32B: Currency/Amount for individual transaction
            if (txAmount != null) {
                mt102.append(":32B:").append(txAmount.getCcy()).append(txAmount.getValue().toPlainString())
                        .append("\n");
            }

            // Field 71A: Details of Charges (default)
            mt102.append(":71A:SHA\n");
        }

        mt102.append("-}\n");

        return mt102.toString();
    }
}