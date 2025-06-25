package com.mtmx.service.converter.impl;

import com.mtmx.domain.model.mx.Pacs008Message;
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
 * Converter for MT103 to MX pacs.008.001.08
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Mt103ToMxConverter implements MessageConverter<String, String> {

    private final MtMessageValidator mtMessageValidator;

    private static final Pattern FIELD_32A_PATTERN = Pattern.compile(":32A:(\\d{6})(\\w{3})([\\d,\\.]+)");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");

    @Override
    public String convert(String mtMessage) throws ConversionException {
        if (!isValid(mtMessage)) {
            throw new ConversionException("Invalid MT103 message");
        }

        try {
            // Extract fields from MT message
            String reference = mtMessageValidator.extractField(mtMessage, "20");
            String[] amountInfo = extractAmountInfo(mtMessage);

            if (reference == null || amountInfo == null) {
                throw new ConversionException("Required fields not found in MT103 message");
            }

            // Create MX message using JAXB
            Pacs008Message mxMessage = createPacs008Message(reference, amountInfo);

            // Marshal to XML string
            return marshalToXml(mxMessage);

        } catch (Exception e) {
            log.error("Error converting MT103 to MX: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MT103 to MX", e);
        }
    }

    @Override
    public boolean isValid(String mtMessage) {
        if (!mtMessageValidator.isValid(mtMessage)) {
            return false;
        }

        String messageType = mtMessageValidator.getMessageType(mtMessage);
        return "103".equals(messageType);
    }

    @Override
    public String getSupportedMessageType() {
        return "103";
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

    private Pacs008Message createPacs008Message(String reference, String[] amountInfo) {
        Pacs008Message message = new Pacs008Message();

        // Create group header
        Pacs008Message.GroupHeader groupHeader = new Pacs008Message.GroupHeader();
        groupHeader.setMsgId(reference);
        groupHeader.setCreDtTm(LocalDateTime.now());
        groupHeader.setNbOfTxs("1");

        // Create total amount
        Pacs008Message.ActiveOrHistoricCurrencyAndAmount totalAmount = new Pacs008Message.ActiveOrHistoricCurrencyAndAmount();
        totalAmount.setCcy(amountInfo[1]);
        totalAmount.setValue(new BigDecimal(amountInfo[2]));
        groupHeader.setTtlIntrBkSttlmAmt(totalAmount);

        // Create transaction information
        Pacs008Message.CreditTransferTransactionInformation txInfo = new Pacs008Message.CreditTransferTransactionInformation();

        // Payment ID
        Pacs008Message.PaymentIdentification paymentId = new Pacs008Message.PaymentIdentification();
        paymentId.setInstrId(reference);
        paymentId.setEndToEndId(reference);
        txInfo.setPmtId(paymentId);

        // Amount
        Pacs008Message.ActiveOrHistoricCurrencyAndAmount amount = new Pacs008Message.ActiveOrHistoricCurrencyAndAmount();
        amount.setCcy(amountInfo[1]);
        amount.setValue(new BigDecimal(amountInfo[2]));
        txInfo.setIntrBkSttlmAmt(amount);

        // Debtor
        Pacs008Message.PartyIdentification43 debtor = new Pacs008Message.PartyIdentification43();
        debtor.setNm("DEBTOR NAME");
        txInfo.setDbtr(debtor);

        // Creditor
        Pacs008Message.PartyIdentification43 creditor = new Pacs008Message.PartyIdentification43();
        creditor.setNm("CREDITOR NAME");
        txInfo.setCdtr(creditor);

        // Set components
        Pacs008Message.FIToFICstmrCdtTrf fiToFi = new Pacs008Message.FIToFICstmrCdtTrf();
        fiToFi.setGrpHdr(groupHeader);
        fiToFi.setCdtTrfTxInf(Arrays.asList(txInfo));

        message.setFiToFICstmrCdtTrf(fiToFi);

        return message;
    }

    private String marshalToXml(Pacs008Message message) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Pacs008Message.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

        StringWriter writer = new StringWriter();
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        marshaller.marshal(message, writer);

        return writer.toString();
    }
}