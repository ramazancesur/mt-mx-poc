# MX Messages Documentation

## Overview

MX (ISO 20022) messages are XML-based financial messages that represent the modern standard for financial messaging. This document provides comprehensive information about the MX messages used in the MT-MX Conversion System.

## MX Message Types

### 1. pacs.008.001.08 - Customer Credit Transfer

**Purpose**: Used for single customer credit transfers (equivalent to MT103)

**Key Features**:
- Supports both domestic and international transfers
- Includes detailed party information (debtor/creditor)
- Supports multiple currencies
- Includes regulatory reporting fields

**Main Elements**:
```xml
<Document>
  <pacs.008.001.08>
    <GrpHdr>
      <!-- Group Header Information -->
    </GrpHdr>
    <CdtTrfTxInf>
      <!-- Credit Transfer Transaction Information -->
      <PmtId>
        <!-- Payment Identification -->
      </PmtId>
      <IntrBkSttlmAmt>
        <!-- Interbank Settlement Amount -->
      </IntrBkSttlmAmt>
      <ChrgBr>
        <!-- Charge Bearer -->
      </ChrgBr>
      <Dbtr>
        <!-- Debtor Information -->
      </Dbtr>
      <DbtrAcct>
        <!-- Debtor Account -->
      </DbtrAcct>
      <DbtrAgt>
        <!-- Debtor Agent -->
      </DbtrAgt>
      <CdtrAgt>
        <!-- Creditor Agent -->
      </CdtrAgt>
      <Cdtr>
        <!-- Creditor Information -->
      </Cdtr>
      <CdtrAcct>
        <!-- Creditor Account -->
      </CdtrAcct>
      <Purp>
        <!-- Purpose -->
      </Purp>
      <RgltryRptg>
        <!-- Regulatory Reporting -->
      </RgltryRptg>
      <RmtInf>
        <!-- Remittance Information -->
      </RmtInf>
    </CdtTrfTxInf>
  </pacs.008.001.08>
</Document>
```

**Mapping from MT103**:
- Field 20 → PmtId/InstrId
- Field 32A → IntrBkSttlmAmt
- Field 50K → Dbtr
- Field 52A → DbtrAgt
- Field 57A → CdtrAgt
- Field 59 → Cdtr
- Field 70 → RmtInf/Ustrd
- Field 71A → ChrgBr

### 2. pacs.009.001.08 - Financial Institution Credit Transfer

**Purpose**: Used for financial institution transfers (equivalent to MT202)

**Key Features**:
- Supports interbank transfers
- Includes settlement instructions
- Supports nostro/vostro account management
- Includes regulatory reporting

**Main Elements**:
```xml
<Document>
  <pacs.009.001.08>
    <GrpHdr>
      <!-- Group Header Information -->
    </GrpHdr>
    <FICdtTrf>
      <!-- Financial Institution Credit Transfer -->
      <GrpHdr>
        <!-- Group Header -->
      </GrpHdr>
      <CdtTrfTxInf>
        <!-- Credit Transfer Transaction Information -->
        <PmtId>
          <!-- Payment Identification -->
        </PmtId>
        <IntrBkSttlmAmt>
          <!-- Interbank Settlement Amount -->
        </IntrBkSttlmAmt>
        <IntrBkSttlmDt>
          <!-- Interbank Settlement Date -->
        </IntrBkSttlmDt>
        <SttlmTmIndctn>
          <!-- Settlement Time Indication -->
        </SttlmTmIndctn>
        <SttlmTmReq>
          <!-- Settlement Time Request -->
        </SttlmTmReq>
        <PrvsInstgAgt1>
          <!-- Previous Instructing Agent 1 -->
        </PrvsInstgAgt1>
        <PrvsInstgAgt1Acct>
          <!-- Previous Instructing Agent 1 Account -->
        </PrvsInstgAgt1Acct>
        <PrvsInstgAgt2>
          <!-- Previous Instructing Agent 2 -->
        </PrvsInstgAgt2>
        <PrvsInstgAgt2Acct>
          <!-- Previous Instructing Agent 2 Account -->
        </PrvsInstgAgt2Acct>
        <PrvsInstgAgt3>
          <!-- Previous Instructing Agent 3 -->
        </PrvsInstgAgt3>
        <PrvsInstgAgt3Acct>
          <!-- Previous Instructing Agent 3 Account -->
        </PrvsInstgAgt3Acct>
        <InstgAgt>
          <!-- Instructing Agent -->
        </InstgAgt>
        <InstdAgt>
          <!-- Instructed Agent -->
        </InstdAgt>
        <IntrmyAgt1>
          <!-- Intermediary Agent 1 -->
        </IntrmyAgt1>
        <IntrmyAgt1Acct>
          <!-- Intermediary Agent 1 Account -->
        </IntrmyAgt1Acct>
        <IntrmyAgt2>
          <!-- Intermediary Agent 2 -->
        </IntrmyAgt2>
        <IntrmyAgt2Acct>
          <!-- Intermediary Agent 2 Account -->
        </IntrmyAgt2Acct>
        <IntrmyAgt3>
          <!-- Intermediary Agent 3 -->
        </IntrmyAgt3>
        <IntrmyAgt3Acct>
          <!-- Intermediary Agent 3 Account -->
        </IntrmyAgt3Acct>
        <UltmtCdtr>
          <!-- Ultimate Creditor -->
        </UltmtCdtr>
        <InstrForCdtrAgt>
          <!-- Instruction for Creditor Agent -->
        </InstrForCdtrAgt>
        <Purp>
          <!-- Purpose -->
        </Purp>
        <RgltryRptg>
          <!-- Regulatory Reporting -->
        </RgltryRptg>
        <Tax>
          <!-- Tax -->
        </Tax>
        <RltdRmtInf>
          <!-- Related Remittance Information -->
        </RltdRmtInf>
        <RmtInf>
          <!-- Remittance Information -->
        </RmtInf>
        <SplmtryData>
          <!-- Supplementary Data -->
        </SplmtryData>
      </CdtTrfTxInf>
    </FICdtTrf>
  </pacs.009.001.08>
</Document>
```

**Mapping from MT202**:
- Field 20 → PmtId/InstrId
- Field 21 → PmtId/EndToEndId
- Field 32A → IntrBkSttlmAmt + IntrBkSttlmDt
- Field 52A → InstgAgt
- Field 53A → PrvsInstgAgt1
- Field 54A → PrvsInstgAgt2
- Field 57A → InstdAgt
- Field 58A → UltmtCdtr
- Field 72 → InstrForCdtrAgt

### 3. pacs.004.001.02 - Payment Return

**Purpose**: Used for payment returns and rejections

**Key Features**:
- Supports payment returns
- Includes return reason codes
- Supports partial returns
- Includes regulatory reporting

**Main Elements**:
```xml
<Document>
  <pacs.004.001.02>
    <GrpHdr>
      <!-- Group Header Information -->
    </GrpHdr>
    <TxInf>
      <!-- Transaction Information -->
      <RtrId>
        <!-- Return Identification -->
      </RtrId>
      <OrgnlTxId>
        <!-- Original Transaction Identification -->
      </OrgnlTxId>
      <RtrdIntrBkSttlmAmt>
        <!-- Returned Interbank Settlement Amount -->
      </RtrdIntrBkSttlmAmt>
      <IntrBkSttlmDt>
        <!-- Interbank Settlement Date -->
      </IntrBkSttlmDt>
      <SttlmInf>
        <!-- Settlement Information -->
      </SttlmInf>
      <RtrRsnInf>
        <!-- Return Reason Information -->
      </RtrRsnInf>
      <OrgnlTxRef>
        <!-- Original Transaction Reference -->
      </OrgnlTxRef>
    </TxInf>
  </pacs.004.001.02>
</Document>
```

## XML Schema Validation

All MX messages are validated against their respective XSD schemas:

- **pacs.008.001.08.xsd**: Customer Credit Transfer schema
- **pacs.009.001.08.xsd**: Financial Institution Credit Transfer schema
- **pacs.004.001.02.xsd**: Payment Return schema

### Validation Rules

1. **Structure Validation**: XML must conform to the XSD schema
2. **Business Rule Validation**: 
   - BIC codes must be valid
   - Amounts must be positive
   - Dates must be valid
   - Currency codes must be ISO 4217 compliant
3. **Cross-Field Validation**:
   - Settlement amount must match transaction amount
   - Agent BICs must be valid
   - Account numbers must be properly formatted

## Message Conversion Process

### MT to MX Conversion

1. **Parse MT Message**: Extract fields from MT format
2. **Validate MT Content**: Check field validity and business rules
3. **Map to MX Structure**: Transform MT fields to MX XML elements
4. **Generate XML**: Create valid XML document
5. **Validate MX**: Ensure XML conforms to XSD schema
6. **Store Result**: Save both MT and MX versions

### MX to MT Conversion

1. **Parse XML**: Extract data from MX XML structure
2. **Validate XML**: Check against XSD schema
3. **Map to MT Fields**: Transform XML elements to MT format
4. **Generate MT**: Create valid MT message
5. **Validate MT**: Ensure MT format compliance
6. **Store Result**: Save both versions

## Error Handling

### Common Validation Errors

1. **Schema Validation Errors**:
   - Missing required elements
   - Invalid element order
   - Incorrect data types

2. **Business Rule Errors**:
   - Invalid BIC codes
   - Negative amounts
   - Invalid currency codes
   - Future settlement dates

3. **Cross-Reference Errors**:
   - Mismatched amounts
   - Invalid account references
   - Inconsistent party information

### Error Response Format

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "IntrBkSttlmAmt",
      "code": "INVALID_AMOUNT",
      "message": "Amount must be positive"
    },
    {
      "field": "DbtrAgt/FinInstnId/BICFI",
      "code": "INVALID_BIC",
      "message": "Invalid BIC code format"
    }
  ]
}
```

## Usage Examples

### Creating a Customer Credit Transfer

```javascript
const mxMessage = {
  messageType: 'pacs.008.001.08',
  content: {
    GrpHdr: {
      MsgId: 'MSG001',
      CreDtTm: '2024-01-15T10:30:00Z',
      NbOfTxs: '1',
      SttlmInf: {
        SttlmMtd: 'CLRG'
      }
    },
    CdtTrfTxInf: {
      PmtId: {
        InstrId: 'INSTR001',
        EndToEndId: 'E2E001'
      },
      IntrBkSttlmAmt: {
        Ccy: 'EUR',
        Amt: '1000.00'
      },
      ChrgBr: 'SLEV',
      Dbtr: {
        Nm: 'John Doe'
      },
      DbtrAcct: {
        Id: {
          IBAN: 'DE89370400440532013000'
        }
      },
      DbtrAgt: {
        FinInstnId: {
          BICFI: 'DEUTDEFF'
        }
      },
      CdtrAgt: {
        FinInstnId: {
          BICFI: 'CHASUS33'
        }
      },
      Cdtr: {
        Nm: 'Jane Smith'
      },
      CdtrAcct: {
        Id: {
          IBAN: 'US64NFCU0000000001234567'
        }
      },
      RmtInf: {
        Ustrd: 'Invoice payment'
      }
    }
  }
};
```

### Validating an MX Message

```javascript
const validationResult = await validateMXMessage(mxMessage);
if (validationResult.success) {
  console.log('MX message is valid');
} else {
  console.log('Validation errors:', validationResult.errors);
}
```

## Best Practices

1. **Always validate** MX messages before processing
2. **Use proper BIC codes** for financial institutions
3. **Include all required fields** according to the schema
4. **Handle errors gracefully** with proper error messages
5. **Log all conversions** for audit purposes
6. **Use consistent naming** for message IDs and references
7. **Test with sample data** before production use

## Security Considerations

1. **Input Validation**: Always validate all input data
2. **XML Injection**: Prevent XML injection attacks
3. **Data Encryption**: Encrypt sensitive data in transit
4. **Access Control**: Implement proper access controls
5. **Audit Logging**: Log all message processing activities
6. **Error Handling**: Don't expose sensitive information in error messages

## Performance Considerations

1. **Schema Caching**: Cache XSD schemas for faster validation
2. **Message Size**: Monitor message sizes for performance impact
3. **Batch Processing**: Use batch processing for large volumes
4. **Memory Management**: Properly handle large XML documents
5. **Database Optimization**: Optimize database queries for message storage

## Troubleshooting

### Common Issues

1. **Schema Validation Failures**:
   - Check XML structure against XSD
   - Verify all required elements are present
   - Ensure proper element ordering

2. **BIC Code Issues**:
   - Verify BIC code format (8 or 11 characters)
   - Check if BIC is in valid registry
   - Ensure proper country codes

3. **Amount Format Issues**:
   - Use proper decimal separators
   - Ensure positive amounts
   - Check currency code validity

4. **Date Format Issues**:
   - Use ISO 8601 format
   - Ensure valid dates
   - Check timezone handling

### Debug Tools

1. **XML Validator**: Use online XML validators
2. **Schema Validator**: Validate against XSD schemas
3. **Log Analysis**: Review application logs
4. **Message Inspector**: Use message inspection tools

## References

- [ISO 20022 Official Documentation](https://www.iso20022.org/)
- [SWIFT MX Message Standards](https://www.swift.com/standards/data-standards/iso-20022)
- [XML Schema Definition (XSD)](https://www.w3.org/XML/Schema)
- [BIC Code Registry](https://www.swift.com/standards/data-standards/bic)
- [ISO 4217 Currency Codes](https://www.iso.org/iso-4217-currency-codes.html) 