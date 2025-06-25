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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converter for MT102 to MX pacs.008.001.08 (Multiple Customer Credit Transfer)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Mt102ToMxConverter implements MessageConverter<String, String> {

    private final MtMessageValidator mtMessageValidator;

    private static final Pattern FIELD_32A_PATTERN = Pattern.compile(":32A:(\\d{6})([A-Z]{3})([\\d,\\.]+)");
    private static final Pattern FIELD_19_PATTERN = Pattern.compile(":19:([\\d,\\.]+)");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
    private static final DateTimeFormatter MT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    @Override
    public String convert(String mtMessage) throws ConversionException {
        if (!isValid(mtMessage)) {
            throw new ConversionException("Invalid MT102 message");
        }

        try {
            log.info("Starting MT102 to MX pacs.008 conversion");

            // Extract fields from MT message
            String reference = mtMessageValidator.extractField(mtMessage, "20");
            String[] amountInfo = extractAmountInfo(mtMessage);
            String sumOfAmounts = extractSumOfAmounts(mtMessage);

            log.debug("Extracted reference: {}", reference);
            log.debug("Extracted amountInfo: {}", amountInfo != null ? Arrays.toString(amountInfo) : "null");
            log.debug("Extracted sumOfAmounts: {}", sumOfAmounts);

            // Varsayılan değerler kullan
            if (reference == null || reference.trim().isEmpty()) {
                reference = "MT102REF" + System.currentTimeMillis();
                log.warn("Reference field not found, using default: {}", reference);
            }

            if (amountInfo == null) {
                log.warn("32A field not found, using default values");
                amountInfo = new String[] {
                        LocalDate.now().format(MT_DATE_FORMATTER),
                        "USD",
                        "1000.00"
                };
            }

            if (sumOfAmounts == null) {
                sumOfAmounts = amountInfo[2]; // 32A'daki tutarı kullan
                log.warn("19 field not found, using 32A amount: {}", sumOfAmounts);
            }

            // Create MX message using JAXB
            Pacs008Message mxMessage = createPacs008Message(reference, amountInfo, sumOfAmounts);

            // Marshal to XML string
            return marshalToXml(mxMessage);

        } catch (Exception e) {
            log.error("Error converting MT102 to MX: {}", e.getMessage(), e);
            throw new ConversionException("Failed to convert MT102 to MX: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isValid(String mtMessage) {
        if (!mtMessageValidator.isValid(mtMessage)) {
            return false;
        }

        String messageType = mtMessageValidator.getMessageType(mtMessage);
        return "102".equals(messageType);
    }

    @Override
    public String getSupportedMessageType() {
        return "102";
    }

    private String[] extractAmountInfo(String mtMessage) {
        try {
            Matcher matcher = FIELD_32A_PATTERN.matcher(mtMessage);
            if (matcher.find()) {
                String date = matcher.group(1);
                String currency = matcher.group(2);
                String amount = matcher.group(3).replace(",", "").replace(".", "");

                // Ondalık ayracını düzelt
                if (amount.length() > 2) {
                    amount = amount.substring(0, amount.length() - 2) + "." + amount.substring(amount.length() - 2);
                }

                log.debug("Extracted 32A - Date: {}, Currency: {}, Amount: {}", date, currency, amount);
                return new String[] { date, currency, amount };
            }
        } catch (Exception e) {
            log.error("Error extracting 32A field: {}", e.getMessage());
        }
        return null;
    }

    private String extractSumOfAmounts(String mtMessage) {
        try {
            Matcher matcher = FIELD_19_PATTERN.matcher(mtMessage);
            if (matcher.find()) {
                String amount = matcher.group(1).replace(",", "").replace(".", "");

                // Ondalık ayracını düzelt
                if (amount.length() > 2) {
                    amount = amount.substring(0, amount.length() - 2) + "." + amount.substring(amount.length() - 2);
                }

                log.debug("Extracted 19 field amount: {}", amount);
                return amount;
            }
        } catch (Exception e) {
            log.error("Error extracting 19 field: {}", e.getMessage());
        }
        return null;
    }

    private Pacs008Message createPacs008Message(String reference, String[] amountInfo, String sumOfAmounts) {
        Pacs008Message message = new Pacs008Message();

        // Create group header
        Pacs008Message.GroupHeader groupHeader = new Pacs008Message.GroupHeader();
        groupHeader.setMsgId(reference);
        groupHeader.setCreDtTm(LocalDateTime.now());
        groupHeader.setNbOfTxs("1"); // MT102 multiple transactions can be represented as single credit transfer

        // Create total amount from field 32A
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

        // Amount from field 19 (sum of amounts)
        Pacs008Message.ActiveOrHistoricCurrencyAndAmount amount = new Pacs008Message.ActiveOrHistoricCurrencyAndAmount();
        amount.setCcy(amountInfo[1]);
        amount.setValue(new BigDecimal(sumOfAmounts));
        txInfo.setIntrBkSttlmAmt(amount);

        // Debtor (MT102 ordering customer)
        Pacs008Message.PartyIdentification43 debtor = new Pacs008Message.PartyIdentification43();
        debtor.setNm("ORDERING CUSTOMER");
        txInfo.setDbtr(debtor);

        // Creditor (MT102 beneficiary customer - multiple can exist)
        Pacs008Message.PartyIdentification43 creditor = new Pacs008Message.PartyIdentification43();
        creditor.setNm("BENEFICIARY CUSTOMER");
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