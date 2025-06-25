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
 * Converter for MT203 to MX pacs.009.001.08 (Multiple Financial Institution
 * Credit Transfer)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Mt203ToMxConverter implements MessageConverter<String, String> {

    private final MtMessageValidator mtMessageValidator;

    private static final Pattern FIELD_32A_PATTERN = Pattern.compile(":32A:(\\d{6})(\\w{3})([\\d,\\.]+)");
    private static final Pattern FIELD_32B_PATTERN = Pattern.compile(":32B:(\\w{3})([\\d,\\.]+)");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    @Override
    public String convert(String mtMessage) throws ConversionException {
        log.info("Starting MT203 to MX conversion");

        if (!isValid(mtMessage)) {
            log.error("Invalid MT203 message");
            throw new ConversionException("Invalid MT203 message");
        }

        try {
            // Extract fields from MT message
            String reference = mtMessageValidator.extractField(mtMessage, "20");
            log.info("Extracted reference: {}", reference);

            String[] amountInfo = extractAmountInfo(mtMessage);
            log.info("Extracted amount info: {}", Arrays.toString(amountInfo));

            if (reference == null) {
                log.error("Reference field (20) not found in MT203 message");
                throw new ConversionException("Reference field (20) not found in MT203 message");
            }

            if (amountInfo == null) {
                log.error("Amount information not found in MT203 message");
                throw new ConversionException("Amount information not found in MT203 message");
            }

            // Create MX message using JAXB
            Pacs009Message mxMessage = createPacs009Message(reference, amountInfo);
            log.info("Created Pacs009Message successfully");

            // Marshal to XML string
            String result = marshalToXml(mxMessage);
            log.info("MT203 to MX conversion completed successfully");
            return result;

        } catch (Exception e) {
            log.error("Error converting MT203 to MX: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MT203 to MX: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isValid(String mtMessage) {
        if (!mtMessageValidator.isValid(mtMessage)) {
            log.warn("MT message validation failed");
            return false;
        }

        String messageType = mtMessageValidator.getMessageType(mtMessage);
        log.info("Message type detected: {}", messageType);
        return "203".equals(messageType);
    }

    @Override
    public String getSupportedMessageType() {
        return "203";
    }

    private String[] extractAmountInfo(String mtMessage) {
        log.info("Extracting amount info from MT203 message");

        // First try to get amount from field 32A (total amount)
        Matcher matcher = FIELD_32A_PATTERN.matcher(mtMessage);
        if (matcher.find()) {
            String date = matcher.group(1);
            String currency = matcher.group(2);
            String amount = matcher.group(3).replace(",", "");
            log.info("Found field 32A: date={}, currency={}, amount={}", date, currency, amount);
            return new String[] { date, currency, amount };
        }

        log.warn("Field 32A not found, trying field 32B");

        // If field 32A not found, try to get from field 32B (individual transaction
        // amount)
        Matcher matcher32B = FIELD_32B_PATTERN.matcher(mtMessage);
        if (matcher32B.find()) {
            String currency = matcher32B.group(1);
            String amount = matcher32B.group(2).replace(",", "");
            log.info("Found field 32B: currency={}, amount={}", currency, amount);
            return new String[] { "250626", currency, amount }; // Default date if not found
        }

        log.error("Neither field 32A nor 32B found in MT203 message");
        return null;
    }

    private Pacs009Message createPacs009Message(String reference, String[] amountInfo) {
        log.info("Creating Pacs009Message with reference: {}, amountInfo: {}", reference, Arrays.toString(amountInfo));

        Pacs009Message message = new Pacs009Message();

        // Create group header
        Pacs009Message.GroupHeader groupHeader = new Pacs009Message.GroupHeader();
        groupHeader.setMsgId(reference);
        groupHeader.setCreDtTm(LocalDateTime.now());
        groupHeader.setNbOfTxs("1"); // MT203 multiple FI transfers can be represented as single transfer

        // Create total amount from field 32A or 32B
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

        // Amount from field 32A or 32B
        Pacs009Message.ActiveOrHistoricCurrencyAndAmount amount = new Pacs009Message.ActiveOrHistoricCurrencyAndAmount();
        amount.setCcy(amountInfo[1]);
        amount.setValue(new BigDecimal(amountInfo[2]));
        txInfo.setIntrBkSttlmAmt(amount);

        // Instructed Agent (MT203 ordering institution)
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

        log.info("Pacs009Message created successfully");
        return message;
    }

    private String marshalToXml(Pacs009Message message) throws JAXBException {
        log.info("Marshaling Pacs009Message to XML");

        JAXBContext context = JAXBContext.newInstance(Pacs009Message.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        StringWriter writer = new StringWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        marshaller.marshal(message, writer);

        String result = writer.toString();
        log.info("XML marshaling completed, result length: {}", result.length());
        return result;
    }
}