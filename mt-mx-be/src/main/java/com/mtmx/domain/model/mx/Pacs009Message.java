package com.mtmx.domain.model.mx;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JAXB model for pacs.009.001.08 message
 */
@Data
@XmlRootElement(name = "Document", namespace = "urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pacs009Message {

    @XmlElement(name = "FICdtTrf", required = true)
    private FICdtTrf fiCdtTrf;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FICdtTrf {

        @XmlElement(name = "GrpHdr", required = true)
        private GroupHeader grpHdr;

        @XmlElement(name = "CdtTrfTxInf", required = true)
        private List<CreditTransferTransactionInformation> cdtTrfTxInf;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GroupHeader {

        @XmlElement(name = "MsgId", required = true)
        private String msgId;

        @XmlElement(name = "CreDtTm", required = true)
        private LocalDateTime creDtTm;

        @XmlElement(name = "NbOfTxs", required = true)
        private String nbOfTxs;

        @XmlElement(name = "TtlIntrBkSttlmAmt", required = true)
        private ActiveOrHistoricCurrencyAndAmount ttlIntrBkSttlmAmt;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreditTransferTransactionInformation {

        @XmlElement(name = "PmtId", required = true)
        private PaymentIdentification pmtId;

        @XmlElement(name = "IntrBkSttlmAmt", required = true)
        private ActiveOrHistoricCurrencyAndAmount intrBkSttlmAmt;

        @XmlElement(name = "InstdAgt")
        private BranchAndFinancialInstitutionIdentification4 instdAgt;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PaymentIdentification {

        @XmlElement(name = "InstrId")
        private String instrId;

        @XmlElement(name = "EndToEndId", required = true)
        private String endToEndId;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ActiveOrHistoricCurrencyAndAmount {

        @XmlAttribute(name = "Ccy", required = true)
        private String ccy;

        @XmlValue
        private BigDecimal value;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class BranchAndFinancialInstitutionIdentification4 {

        @XmlElement(name = "FinInstnId", required = true)
        private FinancialInstitutionIdentification7 finInstnId;
    }

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FinancialInstitutionIdentification7 {

        @XmlElement(name = "BICFI")
        private String bicfi;
    }
}