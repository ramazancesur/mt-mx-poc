-- Extended test data for MT-MX application (~200 messages)
-- This script adds comprehensive test data including both MT and MX messages

-- =============================================================================
-- ADDITIONAL MT103 MESSAGES (20 more examples)
-- =============================================================================

-- MT103 Examples with various currencies and countries
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES 
-- Turkish Banks Internal Transfers
('MT103', 'TGBATRIXXX', 'AKBKTRISKXXX', 75000.00, 'TRY', CURRENT_DATE + 4,
'{1:F01TGBATRIXXX0000000000}{2:I103AKBKTRISKXXXXN}{4:
:20:MT103TRY001
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 4, 'YYMMDD') || 'TRY75000,00
:50K:/TR330006200000000001234567
ANKARA TICARET ODASI
ATATURK BULVARI NO:67
KIZILAY/ANKARA
:52A:TGBATRIXXX
:57A:AKBKTRISKXXX
:59:/TR640001300000000001234567
ISTANBUL SANAYI ODASI
MESRUTIYET CAD NO:118
TEPEBASI/ISTANBUL
:70:MEMBERSHIP FEE PAYMENT Q4 2024
:71A:SHA
-}', NULL, NOW(), NOW()),

('MT103', 'ISBKTRISAHXXX', 'YAPITRISXXX', 120000.00, 'TRY', CURRENT_DATE + 5,
'{1:F01ISBKTRISAHXXX0000000000}{2:I103YAPITRISXXXXN}{4:
:20:MT103TRY002
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 5, 'YYMMDD') || 'TRY120000,00
:50K:/TR250001500000000009876543
IZMIR BUYUKSEHIR BELEDIYESI
KONAK MEYDANI NO:1
KONAK/IZMIR
:52A:ISBKTRISAHXXX
:57A:YAPITRISXXX
:59:/TR980001400000000001234567
ANKARA BUYUKSEHIR BELEDIYESI
KIZILAY MEYDANI NO:1
KIZILAY/ANKARA
:70:MUNICIPALITY COOPERATION PAYMENT
:71A:SHA
-}', NULL, NOW(), NOW()),

-- International Transfers
('MT103', 'TGBATRIXXX', 'CHASUS33XXX', 35000.00, 'USD', CURRENT_DATE + 6,
'{1:F01TGBATRIXXX0000000000}{2:I103CHASUS33XXXXN}{4:
:20:MT103USD003
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 6, 'YYMMDD') || 'USD35000,00
:50K:/TR330006200000000001234567
ANKARA UNIVERSITY
TANDOGAN CAMPUS
TANDOGAN/ANKARA
:52A:TGBATRIXXX
:57A:CHASUS33XXX
:59:/US64NFCU0000000001234567
HARVARD UNIVERSITY
MASSACHUSETTS HALL
CAMBRIDGE/MA USA
:70:RESEARCH COLLABORATION PAYMENT
:71A:SHA
-}', NULL, NOW(), NOW()),

('MT103', 'AKBKTRISKXXX', 'DEUTDEFFXXX', 28000.00, 'EUR', CURRENT_DATE + 7,
'{1:F01AKBKTRISKXXX0000000000}{2:I103DEUTDEFFXXXXN}{4:
:20:MT103EUR004
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 7, 'YYMMDD') || 'EUR28000,00
:50K:/TR640001300000000001234567
BOSCH TURKEY
ORGANIZE SANAYI BOLGESI
BURSA/TURKEY
:52A:AKBKTRISKXXX
:57A:DEUTDEFFXXX
:59:/DE89370400440532013000
BOSCH GMBH
ROBERT BOSCH PLATZ 1
STUTTGART/GERMANY
:70:AUTOMOTIVE PARTS PAYMENT
:71A:SHA
-}', NULL, NOW(), NOW()),

('MT103', 'YAPITRISXXX', 'BARCGB22XXX', 42000.00, 'GBP', CURRENT_DATE + 8,
'{1:F01YAPITRISXXX0000000000}{2:I103BARCGB22XXXXN}{4:
:20:MT103GBP005
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 8, 'YYMMDD') || 'GBP42000,00
:50K:/TR980001400000000001122334
TURKISH AIRLINES
ATATURK AIRPORT
YESILKOY/ISTANBUL
:52A:YAPITRISXXX
:57A:BARCGB22XXX
:59:/GB29NWBK60161331926819
ROLLS ROYCE PLC
PO BOX 31
DERBY/UNITED KINGDOM
:70:ENGINE MAINTENANCE PAYMENT
:71A:SHA
-}', NULL, NOW(), NOW());

-- Add 15 more MT103 messages with loop
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX', 'GARBTRISKXXX'];
    currencies TEXT[] := ARRAY['TRY', 'USD', 'EUR', 'GBP'];
    companies TEXT[] := ARRAY['ANKARA LTD STI', 'ISTANBUL AS', 'IZMIR KOOP', 'BURSA SAN TIC', 'ANTALYA LTD'];
BEGIN
    FOR i IN 1..15 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT103',
            bic_codes[((i-1) % 5) + 1],
            bic_codes[(i % 5) + 1],
            (RANDOM() * 90000 + 10000)::DECIMAL(10,2),
            currencies[((i-1) % 4) + 1],
            CURRENT_DATE + i,
            '{1:F01' || bic_codes[((i-1) % 5) + 1] || '0000000000}{2:I103' || bic_codes[(i % 5) + 1] || 'N}{4:
:20:MT103AUTO' || LPAD(i::TEXT, 3, '0') || '
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + i, 'YYMMDD') || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 90000 + 10000) || ',00
:50K:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
' || companies[((i-1) % 5) + 1] || '
ANKARA/TURKEY
:52A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || bic_codes[(i % 5) + 1] || '
:59:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
' || companies[(i % 5) + 1] || '
ISTANBUL/TURKEY
:70:AUTOMATED TEST PAYMENT ' || i || '
:71A:SHA
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- =============================================================================
-- ADDITIONAL MT102 MESSAGES (22 more examples)
-- =============================================================================

-- Large Salary Batches
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES 
('MT102', 'TGBATRIXXX', 'AKBKTRISKXXX', 450000.00, 'TRY', CURRENT_DATE + 10,
'{1:F01TGBATRIXXX0000000000}{2:I102AKBKTRISKXXXXN}{4:
:20:MT102SAL002
:21:SALARY202412
:32A:' || TO_CHAR(CURRENT_DATE + 10, 'YYMMDD') || 'TRY450000,00
:50A:TGBATRIXXX
:52A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/ACC/MONTHLY SALARY PAYMENTS
:21:EMP010
:32B:TRY45000,00
:50K:/TR330006200000000001234567
ANKARA UNIVERSITY
HR DEPARTMENT
:59:/TR640001300000000001111111
DR MEHMET YILMAZ
PROFESSOR
:70:DECEMBER 2024 SALARY
:21:EMP011
:32B:TRY40000,00
:50K:/TR330006200000000001234567
ANKARA UNIVERSITY
HR DEPARTMENT
:59:/TR640001300000000002222222
DR AYSE DEMIR
ASSOCIATE PROFESSOR
:70:DECEMBER 2024 SALARY
:21:EMP012
:32B:TRY35000,00
:50K:/TR330006200000000001234567
ANKARA UNIVERSITY
HR DEPARTMENT
:59:/TR640001300000000003333333
DR ALI CAN
ASSISTANT PROFESSOR
:70:DECEMBER 2024 SALARY
-}', NULL, NOW(), NOW());

-- Generate 21 more MT102 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX', 'GARBTRISKXXX'];
    currencies TEXT[] := ARRAY['TRY', 'USD', 'EUR'];
    purposes TEXT[] := ARRAY['SALARY', 'SUPPLIER', 'DIVIDEND', 'PENSION', 'BONUS'];
BEGIN
    FOR i IN 1..21 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT102',
            bic_codes[((i-1) % 5) + 1],
            bic_codes[(i % 5) + 1],
            (RANDOM() * 400000 + 100000)::DECIMAL(10,2),
            currencies[((i-1) % 3) + 1],
            CURRENT_DATE + i + 10,
            '{1:F01' || bic_codes[((i-1) % 5) + 1] || '0000000000}{2:I102' || bic_codes[(i % 5) + 1] || 'N}{4:
:20:MT102BATCH' || LPAD(i::TEXT, 3, '0') || '
:21:' || purposes[((i-1) % 5) + 1] || '2024' || i || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 10, 'YYMMDD') || currencies[((i-1) % 3) + 1] || TRUNC(RANDOM() * 400000 + 100000) || ',00
:50A:' || bic_codes[((i-1) % 5) + 1] || '
:52A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || bic_codes[(i % 5) + 1] || '
:72:/ACC/' || purposes[((i-1) % 5) + 1] || ' PAYMENTS BATCH
:21:TXN001
:32B:' || currencies[((i-1) % 3) + 1] || TRUNC(RANDOM() * 50000 + 10000) || ',00
:50K:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
COMPANY' || i || ' LTD STI
ANKARA
:59:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
BENEFICIARY' || i || '
ISTANBUL
:70:PAYMENT DESCRIPTION ' || i || '
:21:TXN002
:32B:' || currencies[((i-1) % 3) + 1] || TRUNC(RANDOM() * 50000 + 10000) || ',00
:50K:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
COMPANY' || i || ' LTD STI
ANKARA
:59:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
BENEFICIARY' || (i + 100) || '
IZMIR
:70:PAYMENT DESCRIPTION ' || (i + 100) || '
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- =============================================================================
-- ADDITIONAL MT202 MESSAGES (21 more examples)
-- =============================================================================

-- Generate 21 MT202 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX', 'GARBTRISKXXX'];
    intl_bics TEXT[] := ARRAY['DEUTDEFFXXX', 'CHASUS33XXX', 'BARCGB22XXX', 'BNPAFRPPXXX', 'UBSWCHZH80A'];
    currencies TEXT[] := ARRAY['TRY', 'USD', 'EUR', 'GBP', 'CHF'];
    purposes TEXT[] := ARRAY['SETTLEMENT', 'NOSTRO', 'LIQUIDITY', 'CLEARING', 'FUNDING'];
BEGIN
    FOR i IN 1..21 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT202',
            bic_codes[((i-1) % 5) + 1],
            intl_bics[((i-1) % 5) + 1],
            (RANDOM() * 9000000 + 1000000)::DECIMAL(10,2),
            currencies[((i-1) % 5) + 1],
            CURRENT_DATE + i + 20,
            '{1:F01' || bic_codes[((i-1) % 5) + 1] || '0000000000}{2:I202' || intl_bics[((i-1) % 5) + 1] || 'N}{4:
:20:MT202REF' || LPAD(i::TEXT, 3, '0') || '
:21:' || purposes[((i-1) % 5) + 1] || i || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 20, 'YYMMDD') || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 9000000 + 1000000) || ',00
:52A:' || bic_codes[((i-1) % 5) + 1] || '
:53A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || intl_bics[((i-1) % 5) + 1] || '
:58A:' || intl_bics[((i-1) % 5) + 1] || '
:72:/INS/' || purposes[((i-1) % 5) + 1] || ' OPERATION ' || i || '
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- =============================================================================
-- ADDITIONAL MT202COV MESSAGES (22 more examples)
-- =============================================================================

-- Generate 22 MT202COV messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX', 'GARBTRISKXXX'];
    intl_bics TEXT[] := ARRAY['DEUTDEFFXXX', 'CHASUS33XXX', 'BARCGB22XXX', 'BNPAFRPPXXX', 'UBSWCHZH80A'];
    currencies TEXT[] := ARRAY['USD', 'EUR', 'GBP', 'CHF'];
BEGIN
    FOR i IN 1..22 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT202COV',
            bic_codes[((i-1) % 5) + 1],
            intl_bics[((i-1) % 5) + 1],
            (RANDOM() * 5000000 + 500000)::DECIMAL(10,2),
            currencies[((i-1) % 4) + 1],
            CURRENT_DATE + i + 30,
            '{1:F01' || bic_codes[((i-1) % 5) + 1] || '0000000000}{2:I202' || intl_bics[((i-1) % 5) + 1] || 'N}{4:
:20:MT202COVREF' || LPAD(i::TEXT, 3, '0') || '
:21:COV2024' || i || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 30, 'YYMMDD') || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 5000000 + 500000) || ',00
:52A:' || bic_codes[((i-1) % 5) + 1] || '
:53A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || intl_bics[((i-1) % 5) + 1] || '
:58A:' || intl_bics[((i-1) % 5) + 1] || '
:50A:' || bic_codes[((i-1) % 5) + 1] || '
:59A:' || intl_bics[((i-1) % 5) + 1] || '
:70:COVER FOR CUSTOMER PAYMENT ' || i || '
:72:/INS/' || currencies[((i-1) % 4) + 1] || ' COVER PAYMENT
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- =============================================================================
-- ADDITIONAL MT203 MESSAGES (21 more examples)
-- =============================================================================

-- Generate 21 MT203 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX', 'GARBTRISKXXX'];
    intl_bics TEXT[] := ARRAY['DEUTDEFFXXX', 'CHASUS33XXX', 'BARCGB22XXX', 'BNPAFRPPXXX', 'UBSWCHZH80A'];
    currencies TEXT[] := ARRAY['TRY', 'USD', 'EUR', 'GBP', 'CHF'];
BEGIN
    FOR i IN 1..21 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT203',
            bic_codes[((i-1) % 5) + 1],
            intl_bics[((i-1) % 5) + 1],
            (RANDOM() * 8000000 + 2000000)::DECIMAL(10,2),
            currencies[((i-1) % 5) + 1],
            CURRENT_DATE + i + 40,
            '{1:F01' || bic_codes[((i-1) % 5) + 1] || '0000000000}{2:I203' || intl_bics[((i-1) % 5) + 1] || 'N}{4:
:20:MT203BATCH' || LPAD(i::TEXT, 3, '0') || '
:21:MULTI2024' || i || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 40, 'YYMMDD') || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 8000000 + 2000000) || ',00
:52A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || intl_bics[((i-1) % 5) + 1] || '
:72:/INS/MULTIPLE ' || currencies[((i-1) % 5) + 1] || ' TRANSFERS
:21:TRF001
:32B:' || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 2000000 + 500000) || ',00
:56A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || intl_bics[((i-1) % 5) + 1] || '
:72:/INS/TRANSFER 1 OF BATCH ' || i || '
:21:TRF002
:32B:' || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 2000000 + 500000) || ',00
:56A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || intl_bics[((i-1) % 5) + 1] || '
:72:/INS/TRANSFER 2 OF BATCH ' || i || '
:21:TRF003
:32B:' || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 2000000 + 500000) || ',00
:56A:' || bic_codes[((i-1) % 5) + 1] || '
:57A:' || intl_bics[((i-1) % 5) + 1] || '
:72:/INS/TRANSFER 3 OF BATCH ' || i || '
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- =============================================================================
-- MX MESSAGES (75 examples - converted messages)
-- =============================================================================

-- Generate MX messages as converted from MT messages
DO $$
DECLARE 
    i INTEGER;
    msg_types TEXT[] := ARRAY['MT103', 'MT102', 'MT202', 'MT202COV', 'MT203'];
    currencies TEXT[] := ARRAY['TRY', 'USD', 'EUR', 'GBP', 'CHF'];
    current_timestamp_str TEXT;
BEGIN
    current_timestamp_str := TO_CHAR(NOW(), 'YYYY-MM-DD"T"HH24:MI:SS');
    
    FOR i IN 1..75 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            msg_types[((i-1) % 5) + 1],
            'TGBATRIXXX',
            'AKBKTRISKXXX',
            (RANDOM() * 500000 + 50000)::DECIMAL(10,2),
            currencies[((i-1) % 5) + 1],
            CURRENT_DATE + i,
            '{1:F01TGBATRIXXX0000000000}{2:I' || SUBSTRING(msg_types[((i-1) % 5) + 1], 3) || 'AKBKTRISKXXXXN}{4:
:20:MXREF' || LPAD(i::TEXT, 6, '0') || '
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + i, 'YYMMDD') || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 500000 + 50000) || ',00
:50K:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
SAMPLE SENDER ' || i || '
ANKARA/TURKEY
:52A:TGBATRIXXX
:57A:AKBKTRISKXXX
:59:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
SAMPLE RECEIVER ' || i || '
ISTANBUL/TURKEY
:70:MX CONVERTED MESSAGE ' || i || '
:71A:SHA
-}',
            '<?xml version="1.0" encoding="UTF-8"?>
<Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
    <FIToFICstmrCdtTrf>
        <GrpHdr>
            <MsgId>MXREF' || LPAD(i::TEXT, 6, '0') || '</MsgId>
            <CreDtTm>' || current_timestamp_str || '</CreDtTm>
            <NbOfTxs>1</NbOfTxs>
            <TtlIntrBkSttlmAmt Ccy="' || currencies[((i-1) % 5) + 1] || '">' || TRUNC(RANDOM() * 500000 + 50000) || '.00</TtlIntrBkSttlmAmt>
        </GrpHdr>
        <CdtTrfTxInf>
            <PmtId>
                <InstrId>MXREF' || LPAD(i::TEXT, 6, '0') || '</InstrId>
                <EndToEndId>MXREF' || LPAD(i::TEXT, 6, '0') || '</EndToEndId>
            </PmtId>
            <IntrBkSttlmAmt Ccy="' || currencies[((i-1) % 5) + 1] || '">' || TRUNC(RANDOM() * 500000 + 50000) || '.00</IntrBkSttlmAmt>
            <IntrBkSttlmDt>' || TO_CHAR(CURRENT_DATE + i, 'YYYY-MM-DD') || '</IntrBkSttlmDt>
            <Dbtr>
                <Nm>SAMPLE SENDER ' || i || '</Nm>
                <PstlAdr>
                    <AdrLine>ANKARA/TURKEY</AdrLine>
                </PstlAdr>
            </Dbtr>
            <DbtrAcct>
                <Id>
                    <Othr>
                        <Id>TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '</Id>
                    </Othr>
                </Id>
            </DbtrAcct>
            <DbtrAgt>
                <FinInstnId>
                    <BICFI>TGBATRIXXX</BICFI>
                </FinInstnId>
            </DbtrAgt>
            <CdtrAgt>
                <FinInstnId>
                    <BICFI>AKBKTRISKXXX</BICFI>
                </FinInstnId>
            </CdtrAgt>
            <Cdtr>
                <Nm>SAMPLE RECEIVER ' || i || '</Nm>
                <PstlAdr>
                    <AdrLine>ISTANBUL/TURKEY</AdrLine>
                </PstlAdr>
            </Cdtr>
            <CdtrAcct>
                <Id>
                    <Othr>
                        <Id>TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '</Id>
                    </Othr>
                </Id>
            </CdtrAcct>
            <RmtInf>
                <Ustrd>MX CONVERTED MESSAGE ' || i || '</Ustrd>
            </RmtInf>
        </CdtTrfTxInf>
    </FIToFICstmrCdtTrf>
</Document>',
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- =============================================================================
-- RESET SEQUENCE TO START FROM MAX ID + 1
-- =============================================================================

-- Update the sequence to start from the maximum ID + 1
SELECT setval('swift_messages_id_seq', (SELECT COALESCE(MAX(id), 0) + 1 FROM swift_messages), false);

-- Final summary with corrected counts
SELECT 
    message_type,
    COUNT(*) AS count,
    SUM(amount) AS total_amount,
    STRING_AGG(DISTINCT currency, ', ' ORDER BY currency) AS currencies,
    MIN(created_at) AS first_message,
    MAX(created_at) AS last_message
FROM swift_messages 
GROUP BY message_type
ORDER BY message_type;

-- Overall summary
SELECT 
    COUNT(*) AS total_messages,
    COUNT(DISTINCT message_type) AS message_types,
    COUNT(DISTINCT currency) AS currencies,
    SUM(amount) AS total_amount,
    AVG(amount) AS avg_amount,
    MIN(amount) AS min_amount,
    MAX(amount) AS max_amount
FROM swift_messages;

-- Sequence status
SELECT 
    'swift_messages_id_seq' AS sequence_name,
    last_value AS current_value,
    is_called
FROM swift_messages_id_seq; 