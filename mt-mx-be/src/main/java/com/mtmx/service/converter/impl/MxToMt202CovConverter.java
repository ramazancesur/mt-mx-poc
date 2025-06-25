package com.mtmx.service.converter.impl;

import com.mtmx.domain.model.mx.Pacs009Message;
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
 * Converter for MX pacs.009.001.08 to MT202COV (Financial Institution Credit
 * Transfer with Cover)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MxToMt202CovConverter implements MessageConverter<String, String> {

    private final MxMessageValidator mxMessageValidator;

    private static final DateTimeFormatter MT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    @Override
    public String convert(String mxMessage) throws ConversionException {
        if (!isValid(mxMessage)) {
            throw new ConversionException("Invalid MX message for MT202COV conversion");
        }

        try {
            // Parse MX message
            Pacs009Message pacs009 = unmarshalFromXml(mxMessage);

            // Extract data from MX message
            return createMt202CovMessage(pacs009);

        } catch (Exception e) {
            log.error("Error converting MX to MT202COV: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MX to MT202COV", e);
        }
    }

    @Override
    public boolean isValid(String mxMessage) {
        if (!mxMessageValidator.isValid(mxMessage)) {
            return false;
        }

        String messageType = mxMessageValidator.getMessageType(mxMessage);
        return "pacs.009.001.08".equals(messageType);
    }

    @Override
    public String getSupportedMessageType() {
        return "pacs.009.001.08";
    }

    private Pacs009Message unmarshalFromXml(String xmlMessage) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Pacs009Message.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xmlMessage);
        return (Pacs009Message) unmarshaller.unmarshal(reader);
    }

    private String createMt202CovMessage(Pacs009Message pacs009) {
        StringBuilder mt202Cov = new StringBuilder();

        // Basic header
        mt202Cov.append("{1:F01BANKDEFFXXXX0000000000}\n");
        mt202Cov.append("{2:I202BANKUS33XXXXN}\n");
        mt202Cov.append("{4:\n");

        // Extract group header and transaction info
        Pacs009Message.FICdtTrf fiCdtTrf = pacs009.getFiCdtTrf();
        Pacs009Message.GroupHeader groupHeader = fiCdtTrf.getGrpHdr();

        // Field 20: Transaction Reference Number
        String msgId = groupHeader.getMsgId();
        if (msgId != null && !msgId.isEmpty()) {
            mt202Cov.append(":20:").append(msgId).append("\n");
        } else {
            mt202Cov.append(":20:").append("NOTPROVIDED").append("\n");
        }

        // Extract first transaction for basic info
        if (fiCdtTrf.getCdtTrfTxInf() != null && !fiCdtTrf.getCdtTrfTxInf().isEmpty()) {
            Pacs009Message.CreditTransferTransactionInformation txInfo = fiCdtTrf.getCdtTrfTxInf().get(0);

            // Field 32A: Value Date/Currency/Interbank Settled Amount
            String valueDate = LocalDate.now().format(MT_DATE_FORMATTER);
            Pacs009Message.ActiveOrHistoricCurrencyAndAmount settlementAmount = groupHeader.getTtlIntrBkSttlmAmt();
            if (settlementAmount != null) {
                String currency = settlementAmount.getCcy();
                BigDecimal amount = settlementAmount.getValue();
                mt202Cov.append(":32A:").append(valueDate).append(currency).append(amount.toPlainString()).append("\n");
            }

            // Field 52A: Ordering Institution (from instructed agent)
            Pacs009Message.BranchAndFinancialInstitutionIdentification4 instdAgent = txInfo.getInstdAgt();
            if (instdAgent != null && instdAgent.getFinInstnId() != null) {
                String bic = instdAgent.getFinInstnId().getBicfi();
                if (bic != null && !bic.isEmpty()) {
                    mt202Cov.append(":52A:").append(bic).append("\n");
                }
            }

            // Field 58A: Beneficiary Institution
            mt202Cov.append(":58A:").append("BENINSTXX").append("\n");

            // Field 50K: Underlying Customer Credit Transfer - ordering customer
            mt202Cov.append(":50K:/").append("ORDCUSTOMACC").append("\n");
            mt202Cov.append("ORDERING CUSTOMER NAME").append("\n");

            // Field 59: Underlying Customer Credit Transfer - beneficiary customer
            mt202Cov.append(":59:/").append("BENCUSTOMACC").append("\n");
            mt202Cov.append("BENEFICIARY CUSTOMER NAME").append("\n");

            // Field 70: Remittance Information
            mt202Cov.append(":70:PAYMENT FOR INVOICE\n");

            // Field 71A: Details of Charges (default)
            mt202Cov.append(":71A:SHA\n");
        }

        mt202Cov.append("-}\n");

        return mt202Cov.toString();
    }
}