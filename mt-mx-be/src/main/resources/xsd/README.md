# MT-MX Message Schemas and Samples

This directory contains XSD schemas and sample messages for MT to MX conversion.

## MT-MX Mapping

| MT Message Type | MX Equivalent | Description |
|----------------|---------------|-------------|
| MT102 | pacs.008.001.08 | Multiple Customer Credit Transfer |
| MT103 | pacs.008.001.08 | Single Customer Credit Transfer |
| MT202 | pacs.009.001.08 | Financial Institution Transfer |
| MT203 | pacs.009.001.08 | Multiple Financial Institution Transfer |
| MT202COV | pacs.009.001.08 | Cover Payment |

## Directory Structure

```
xsd/
├── pacs.008.001.08.xsd     # Schema for MT102/MT103 conversions
├── pacs.009.001.08.xsd     # Schema for MT202/MT203/MT202COV conversions
└── README.md               # This file

samples/
├── mt/                     # Sample MT messages
│   ├── MT102_sample.txt
│   ├── MT103_sample.txt
│   ├── MT202_sample.txt
│   ├── MT203_sample.txt
│   └── MT202COV_sample.txt
└── mx/                     # Sample MX messages
    ├── pacs.008.001.08_sample.xml
    └── pacs.009.001.08_sample.xml
```

## Usage

These schemas and samples are used by the MT-MX conversion service for:

1. **Validation**: XSD schemas validate the generated MX messages
2. **Testing**: Sample messages for unit and integration tests
3. **Documentation**: Reference examples for developers

## Message Standards

- **MT Messages**: SWIFT MT (Message Type) format
- **MX Messages**: ISO 20022 XML format
- **Version**: Using pacs.008.001.08 and pacs.009.001.08 schemas

## Prowide Integration

The project uses Prowide ISO20022 libraries for:
- MT message parsing and validation
- Field extraction and mapping
- MX message generation and validation
