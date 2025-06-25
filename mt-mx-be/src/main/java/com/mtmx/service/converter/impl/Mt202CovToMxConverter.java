package com.mtmx.service.converter.impl;

import com.mtmx.domain.model.mx.Pacs009Message;
import com.mtmx.service.converter.ConversionException;
import com.mtmx.service.converter.MessageConverter;
import com.mtmx.service.converter.MtMessageValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter for MT202COV to MX pacs.009.001.08 (Financial Institution Credit
 * Transfer with Cover)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Mt202CovToMxConverter implements MessageConverter<String, String> {

    private final MtMessageValidator mtMessageValidator;

    private static final Pattern FIELD_32A_PATTERN = Pattern.compile(":32A:(\\d{6})(\\w{3})([\\d,\\.]+)");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    @Override
    public String convert(String mtMessage) throws ConversionException {
        if (!isValid(mtMessage)) {
            throw new ConversionException("Invalid MT202COV message");
        }

        try {
            // Extract fields from MT message
            String reference = mtMessageValidator.extractField(mtMessage, "20");
            String[] amountInfo = extractAmountInfo(mtMessage);

            if (reference == null || amountInfo == null) {
                throw new ConversionException("Required fields not found in MT202COV message");
            }

            // Create MX message using JAXB
            Pacs009Message mxMessage = createPacs009Message(reference, amountInfo);

            // Marshal to XML string
            return marshalToXml(mxMessage);

        } catch (Exception e) {
            log.error("Error converting MT202COV to MX: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MT202COV to MX", e);
        }
    }

    @Override
    public boolean isValid(String mtMessage) {
        if (!mtMessageValidator.isValid(mtMessage)) {
            return false;
        }

        String messageType = mtMessageValidator.getMessageType(mtMessage);
        return "202COV".equals(messageType);
    }

    @Override
    public String getSupportedMessageType() {
        return "202COV";
    }

    private String[] extractAmountInfo(String mtMessage) {
        Matcher matcher = FIELD_32A_PATTERN.matcher(mtMessage);
        if (matcher.find()) {
            String date = matcher.group(1);
            String currency = matcher.group(2);
            String amount = matcher.group(3).replace(",", "");
            return new String[] { date, currency, amount };
        }
        return null;
    }

    private Pacs009Message createPacs009Message(String reference, String[] amountInfo) {
        Pacs009Message message = new Pacs009Message();

        // Create group header
        Pacs009Message.GroupHeader groupHeader = new Pacs009Message.GroupHeader();
        groupHeader.setMsgId(reference);
        groupHeader.setCreDtTm(LocalDateTime.now());
        groupHeader.setNbOfTxs("1");

        // Create total amount
        Pacs009Message.ActiveOrHistoricCurrencyAndAmount totalAmount = new Pacs009Message.ActiveOrHistoricCurrencyAndAmount();
        totalAmount.setCcy(amountInfo[1]);
        totalAmount.setValue(new BigDecimal(amountInfo[2]));
        groupHeader.setTtlIntrBkSttlmAmt(totalAmount);

        // Create transaction information
        Pacs009Message.CreditTransferTransactionInformation txInfo = new Pacs009Message.CreditTransferTransactionInformation();

        // Payment ID
        Pacs009Message.PaymentIdentification paymentId = new Pacs009Message.PaymentIdentification();
        paymentId.setInstrId(reference);
        paymentId.setEndToEndId(reference);
        txInfo.setPmtId(paymentId);

        // Amount
        Pacs009Message.ActiveOrHistoricCurrencyAndAmount amount = new Pacs009Message.ActiveOrHistoricCurrencyAndAmount();
        amount.setCcy(amountInfo[1]);
        amount.setValue(new BigDecimal(amountInfo[2]));
        txInfo.setIntrBkSttlmAmt(amount);

        // Instructed Agent (MT202COV ordering institution)
        Pacs009Message.BranchAndFinancialInstitutionIdentification4 instdAgent = new Pacs009Message.BranchAndFinancialInstitutionIdentification4();
        Pacs009Message.FinancialInstitutionIdentification7 instdFinInstnId = new Pacs009Message.FinancialInstitutionIdentification7();
        instdFinInstnId.setBicfi("ORDINSTXX");
        instdAgent.setFinInstnId(instdFinInstnId);
        txInfo.setInstdAgt(instdAgent);

        // Set components
        Pacs009Message.FICdtTrf fiCdtTrf = new Pacs009Message.FICdtTrf();
        fiCdtTrf.setGrpHdr(groupHeader);
        fiCdtTrf.setCdtTrfTxInf(Arrays.asList(txInfo));

        message.setFiCdtTrf(fiCdtTrf);

        return message;
    }

    private String marshalToXml(Pacs009Message message) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Pacs009Message.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        StringWriter writer = new StringWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        marshaller.marshal(message, writer);

        return writer.toString();
    }
}