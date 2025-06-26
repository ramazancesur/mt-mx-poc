-- Validated MT-MX Messages Insert Script
-- Her mesaj türü için doğru SWIFT format'ında mesajlar
-- Clean existing data first

-- =============================================================================
-- MT103 - Single Customer Credit Transfer (15 examples)
-- =============================================================================

-- MT103 Example 1 - Standard EUR Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT103', 'TGBATRIXXX', 'DEUTDEFFXXX', 25000.00, 'EUR', CURRENT_DATE,
    '{1:F01TGBATRIXXX0000000000}{2:I103DEUTDEFFXXXXN}{4:
:20:MT103REF001
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE, 'YYMMDD') || 'EUR25000,00
:50K:/TR330006200119000006672315
AHMET YILMAZ
ATATURK CAD. NO:123
KADIKOY/ISTANBUL
:52A:TGBATRIXXX
:57A:DEUTDEFFXXX
:59:/DE89370400440532013000
MUELLER GMBH
HAUPTSTRASSE 45
FRANKFURT/GERMANY
:70:INVOICE PAYMENT INV-2024-001
:71A:SHA
-}',
    NULL,
    NOW(), NOW()
);

-- MT103 Example 2 - USD Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT103', 'AKBKTRISKXXX', 'CHASUS33XXX', 50000.00, 'USD', CURRENT_DATE + 1,
    '{1:F01AKBKTRISKXXX0000000000}{2:I103CHASUS33XXXXN}{4:
:20:MT103REF002
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 1, 'YYMMDD') || 'USD50000,00
:50K:/TR640001300000000001234567
SELIN AKSOY
BAGLICA MAH. EMEK KONUTLARI
ETIMESGUT/ANKARA
:52A:AKBKTRISKXXX
:57A:CHASUS33XXX
:59:/US64NFCU0000000001234567
JOHN SMITH
123 MAIN STREET
NEW YORK NY 10001/USA
:70:UNIVERSITY TUITION PAYMENT 2024
:71A:SHA
-}',
    NULL,
    NOW(), NOW()
);

-- MT103 Example 3 - GBP Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT103', 'ISBKTRISAHXXX', 'BARCGB22XXX', 15000.00, 'GBP', CURRENT_DATE + 2,
    '{1:F01ISBKTRISAHXXX0000000000}{2:I103BARCGB22XXXXN}{4:
:20:MT103REF003
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 2, 'YYMMDD') || 'GBP15000,00
:50K:/TR250001500000000009876543
MEHMET CAN OZTURK
KONAK MAHALLESI IZMIR CADDESI
KONAK/IZMIR
:52A:ISBKTRISAHXXX
:57A:BARCGB22XXX
:59:/GB29NWBK60161331926819
WILLIAM JONES
45 HIGH STREET
LONDON EC2V 6DN/UK
:70:PROPERTY PURCHASE DEPOSIT
:71A:SHA
-}',
    NULL,
    NOW(), NOW()
);

-- MT103 Example 4 - TRY Internal Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT103', 'YAPITRISXXX', 'GARBTRISKXXX', 100000.00, 'TRY', CURRENT_DATE + 3,
    '{1:F01YAPITRISXXX0000000000}{2:I103GARBTRISKXXXXN}{4:
:20:MT103REF004
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 3, 'YYMMDD') || 'TRY100000,00
:50K:/TR980001400000000001122334
FATMA KAYA
CANKAYA MAHALLESI
CANKAYA/ANKARA
:52A:YAPITRISXXX
:57A:GARBTRISKXXX
:59:/TR330003200000000009988776
AHMET DEMIR INSAAT LTD STI
OSTIM SANAYI SITESI
YENIMAHALLE/ANKARA
:70:CONSTRUCTION MATERIALS PAYMENT
:71A:SHA
-}',
    NULL,
    NOW(), NOW()
);

-- MT103 Example 5 - CHF Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT103', 'TGBATRIXXX', 'UBSWCHZHXXX', 35000.00, 'CHF', CURRENT_DATE + 4,
    '{1:F01TGBATRIXXX0000000000}{2:I103UBSWCHZHXXXXN}{4:
:20:MT103REF005
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + 4, 'YYMMDD') || 'CHF35000,00
:50K:/TR330006200119000006672315
TURKISH PHARMACEUTICAL CO
ORGANIZE SANAYI BOLGESI
BURSA/TURKEY
:52A:TGBATRIXXX
:57A:UBSWCHZHXXX
:59:/CH9300762011623852957
NOVARTIS AG
LIGHTSTRASSE 35
BASEL/SWITZERLAND
:70:PHARMACEUTICAL SUPPLIES PAYMENT
:71A:SHA
-}',
    NULL,
    NOW(), NOW()
);

-- =============================================================================
-- MT102 - Multiple Customer Credit Transfer (10 examples)
-- =============================================================================

-- MT102 Example 1 - Salary Payments
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT102', 'TGBATRIXXX', 'AKBKTRISKXXX', 45000.00, 'TRY', CURRENT_DATE,
    '{1:F01TGBATRIXXX0000000000}{2:I102AKBKTRISKXXXXN}{4:
:20:MT102BATCH001
:21:SAL202412001
:32A:' || TO_CHAR(CURRENT_DATE, 'YYMMDD') || 'TRY45000,00
:50A:TGBATRIXXX
:52A:TGBATRIXXX
:57A:AKBKTRISKXXX
:72:/ACC/SALARY PAYMENTS DECEMBER 2024
:21:EMP001
:32B:TRY15000,00
:50K:/TR330006200000000001234567
AHMET YILMAZ
PERSONEL DEPARTMANI
:59:/TR640001300000000001111111
AHMET YILMAZ
KADIKOY ISTANBUL
:70:DECEMBER 2024 SALARY
:21:EMP002
:32B:TRY15000,00
:50K:/TR330006200000000001234567
AHMET YILMAZ
PERSONEL DEPARTMANI  
:59:/TR640001300000000002222222
SELIN AKSOY
ETIMESGUT ANKARA
:70:DECEMBER 2024 SALARY
:21:EMP003
:32B:TRY15000,00
:50K:/TR330006200000000001234567
AHMET YILMAZ
PERSONEL DEPARTMANI
:59:/TR640001300000000003333333
MEHMET CAN
KONAK IZMIR
:70:DECEMBER 2024 SALARY
-}',
    NULL,
    NOW(), NOW()
);

-- MT102 Example 2 - Supplier Payments  
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT102', 'YAPITRISXXX', 'TGBATRIXXX', 75000.00, 'EUR', CURRENT_DATE + 1,
    '{1:F01YAPITRISXXX0000000000}{2:I102TGBATRIXXXXN}{4:
:20:MT102BATCH002
:21:SUP202412001
:32A:' || TO_CHAR(CURRENT_DATE + 1, 'YYMMDD') || 'EUR75000,00
:50A:YAPITRISXXX
:52A:YAPITRISXXX
:57A:TGBATRIXXX
:72:/ACC/SUPPLIER PAYMENTS
:21:SUP001
:32B:EUR25000,00
:50K:/TR980001400000000001122334
YAPI KREDI CORPORATE
TREASURY DEPARTMENT
:59:/TR330003200000000001111111
ABC INSAAT LTD STI
ANKARA
:70:CONSTRUCTION MATERIALS Q4 2024
:21:SUP002
:32B:EUR25000,00
:50K:/TR980001400000000001122334
YAPI KREDI CORPORATE
TREASURY DEPARTMENT
:59:/TR330003200000000002222222
XYZ ELEKTRIK LTD STI
ISTANBUL
:70:ELECTRICAL MATERIALS Q4 2024
:21:SUP003
:32B:EUR25000,00
:50K:/TR980001400000000001122334
YAPI KREDI CORPORATE
TREASURY DEPARTMENT
:59:/TR330003200000000003333333
DEF MAKINE SAN TIC LTD STI
IZMIR
:70:MACHINERY PURCHASE Q4 2024
-}',
    NULL,
    NOW(), NOW()
);

-- =============================================================================
-- MT202 - General Financial Institution Transfer (10 examples)
-- =============================================================================

-- MT202 Example 1 - Interbank Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT202', 'TGBATRIXXX', 'DEUTDEFFXXX', 500000.00, 'EUR', CURRENT_DATE,
    '{1:F01TGBATRIXXX0000000000}{2:I202DEUTDEFFXXXXN}{4:
:20:MT202REF001
:21:CORP2024001
:32A:' || TO_CHAR(CURRENT_DATE, 'YYMMDD') || 'EUR500000,00
:52A:TGBATRIXXX
:53A:TGBATRIXXX
:56A:DEUTDEFFXXX
:57A:DEUTDEFFXXX
:58A:/DE89370400440532013000
DEUTSCHE BANK AG
FRANKFURT/GERMANY
:72:/ACC/INTERBANK SETTLEMENT
-}',
    NULL,
    NOW(), NOW()
);

-- MT202 Example 2 - USD Interbank Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT202', 'AKBKTRISKXXX', 'CHASUS33XXX', 750000.00, 'USD', CURRENT_DATE + 1,
    '{1:F01AKBKTRISKXXX0000000000}{2:I202CHASUS33XXXXN}{4:
:20:MT202REF002
:21:CORP2024002
:32A:' || TO_CHAR(CURRENT_DATE + 1, 'YYMMDD') || 'USD750000,00
:52A:AKBKTRISKXXX
:53A:AKBKTRISKXXX
:56A:CHASUS33XXX
:57A:CHASUS33XXX
:58A:/US64NFCU0000000001234567
JPMORGAN CHASE BANK
NEW YORK/USA
:72:/ACC/INTERBANK SETTLEMENT
-}',
    NULL,
    NOW(), NOW()
);

-- =============================================================================
-- MT202COV - General Financial Institution Transfer for Cover (10 examples)
-- =============================================================================

-- MT202COV Example 1 - Cover Payment
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT202COV', 'TGBATRIXXX', 'DEUTDEFFXXX', 300000.00, 'EUR', CURRENT_DATE,
    '{1:F01TGBATRIXXX0000000000}{2:I202COVDEUTDEFFXXXXN}{4:
:20:MT202COVREF001
:21:COV2024001
:32A:' || TO_CHAR(CURRENT_DATE, 'YYMMDD') || 'EUR300000,00
:52A:TGBATRIXXX
:53A:TGBATRIXXX
:56A:DEUTDEFFXXX
:57A:DEUTDEFFXXX
:58A:/DE89370400440532013000
DEUTSCHE BANK AG
FRANKFURT/GERMANY
:72:/ACC/COVER PAYMENT FOR MT103
-}',
    NULL,
    NOW(), NOW()
);

-- MT202COV Example 2 - USD Cover Payment
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT202COV', 'AKBKTRISKXXX', 'CHASUS33XXX', 450000.00, 'USD', CURRENT_DATE + 1,
    '{1:F01AKBKTRISKXXX0000000000}{2:I202COVCHASUS33XXXXN}{4:
:20:MT202COVREF002
:21:COV2024002
:32A:' || TO_CHAR(CURRENT_DATE + 1, 'YYMMDD') || 'USD450000,00
:52A:AKBKTRISKXXX
:53A:AKBKTRISKXXX
:56A:CHASUS33XXX
:57A:CHASUS33XXX
:58A:/US64NFCU0000000001234567
JPMORGAN CHASE BANK
NEW YORK/USA
:72:/ACC/COVER PAYMENT FOR MT103
-}',
    NULL,
    NOW(), NOW()
);

-- =============================================================================
-- MT203 - Multiple General Financial Institution Transfer (10 examples)
-- =============================================================================

-- MT203 Example 1 - Valid Multiple Financial Institution Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT203', 'TGBATRIXXX', 'DEUTDEFFXXX', 800000.00, 'EUR', CURRENT_DATE,
    '{1:F01TGBATRIXXX0000000000}{2:I203DEUTDEFFXXXXN}{4:
:20:MT203REF001
:32A:' || TO_CHAR(CURRENT_DATE, 'YYMMDD') || 'EUR800000,00
:52A:TGBATRIXXX
:53A:TGBATRIXXX
:56A:DEUTDEFFXXX
:57A:DEUTDEFFXXX
:58A:/DE89370400440532013000
DEUTSCHE BANK AG
FRANKFURT/GERMANY
:72:/ACC/MULTIPLE INTERBANK SETTLEMENTS
-}',
    NULL,
    NOW(), NOW()
);

-- MT203 Example 2 - USD Multiple Interbank Transfers
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT203', 'AKBKTRISKXXX', 'CHASUS33XXX', 1200000.00, 'USD', CURRENT_DATE + 1,
    '{1:F01AKBKTRISKXXX0000000000}{2:I203CHASUS33XXXXN}{4:
:20:MT203REF002
:21:BATCH2024002
:32A:' || TO_CHAR(CURRENT_DATE + 1, 'YYMMDD') || 'USD1200000,00
:52A:AKBKTRISKXXX
:53A:AKBKTRISKXXX
:56A:CHASUS33XXX
:57A:CHASUS33XXX
:58A:/US64NFCU0000000001234567
JPMORGAN CHASE BANK
NEW YORK/USA
:72:/ACC/MULTIPLE INTERBANK SETTLEMENTS
:21:TRX004
:32B:USD400000,00
:58A:/US64NFCU0000000001234567
JPMORGAN CHASE BANK
NEW YORK/USA
:21:TRX005
:32B:USD400000,00
:58A:/US64NFCU0000000001234567
JPMORGAN CHASE BANK
NEW YORK/USA
:21:TRX006
:32B:USD400000,00
:58A:/US64NFCU0000000001234567
JPMORGAN CHASE BANK
NEW YORK/USA
-}',
    NULL,
    NOW(), NOW()
);

-- =============================================================================
-- Generate additional messages using loops for each type
-- =============================================================================

-- Generate 10 more MT103 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX', 'GARBTRISKXXX'];
    currencies TEXT[] := ARRAY['TRY', 'USD', 'EUR', 'GBP', 'CHF'];
    companies TEXT[] := ARRAY['ANKARA LTD STI', 'ISTANBUL AS', 'IZMIR KOOP', 'BURSA SAN TIC', 'ANTALYA LTD'];
BEGIN
    FOR i IN 1..10 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT103',
            bic_codes[((i-1) % 5) + 1],
            bic_codes[(i % 5) + 1],
            (RANDOM() * 90000 + 10000)::DECIMAL(10,2),
            currencies[((i-1) % 5) + 1],
            CURRENT_DATE + i + 5,
            '{1:F01' || bic_codes[((i-1) % 5) + 1] || '0000000000}{2:I103' || bic_codes[(i % 5) + 1] || 'N}{4:
:20:MT103AUTO' || LPAD(i::TEXT, 3, '0') || '
:23B:CRED
:32A:' || TO_CHAR(CURRENT_DATE + i + 5, 'YYMMDD') || currencies[((i-1) % 5) + 1] || TRUNC(RANDOM() * 90000 + 10000) || ',00
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

-- Generate 8 more MT102 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX'];
    currencies TEXT[] := ARRAY['TRY', 'EUR', 'USD'];
BEGIN
    FOR i IN 1..8 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT102',
            bic_codes[((i-1) % 4) + 1],
            bic_codes[(i % 4) + 1],
            (RANDOM() * 200000 + 50000)::DECIMAL(10,2),
            currencies[((i-1) % 3) + 1],
            CURRENT_DATE + i + 10,
            '{1:F01' || bic_codes[((i-1) % 4) + 1] || '0000000000}{2:I102' || bic_codes[(i % 4) + 1] || 'N}{4:
:20:MT102BATCH' || LPAD(i::TEXT, 3, '0') || '
:21:BATCH' || i || '2024
:32A:' || TO_CHAR(CURRENT_DATE + i + 10, 'YYMMDD') || currencies[((i-1) % 3) + 1] || TRUNC(RANDOM() * 200000 + 50000) || ',00
:50A:' || bic_codes[((i-1) % 4) + 1] || '
:52A:' || bic_codes[((i-1) % 4) + 1] || '
:57A:' || bic_codes[(i % 4) + 1] || '
:72:/ACC/BATCH PAYMENT ' || i || '
:21:TRX' || LPAD(i::TEXT, 3, '0') || 'A
:32B:' || currencies[((i-1) % 3) + 1] || TRUNC(RANDOM() * 50000 + 10000) || ',00
:50K:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
SENDER COMPANY
ANKARA/TURKEY
:59:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
RECEIVER COMPANY
ISTANBUL/TURKEY
:70:BATCH PAYMENT ' || i || ' PART A
:21:TRX' || LPAD(i::TEXT, 3, '0') || 'B
:32B:' || currencies[((i-1) % 3) + 1] || TRUNC(RANDOM() * 50000 + 10000) || ',00
:50K:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
SENDER COMPANY
ANKARA/TURKEY
:59:/TR' || LPAD((RANDOM() * 999999999999999999)::BIGINT::TEXT, 18, '0') || '
RECEIVER COMPANY
ISTANBUL/TURKEY
:70:BATCH PAYMENT ' || i || ' PART B
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- Generate 8 more MT202 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX'];
    intl_bics TEXT[] := ARRAY['DEUTDEFFXXX', 'CHASUS33XXX', 'BARCGB22XXX', 'UBSWCHZHXXX'];
    currencies TEXT[] := ARRAY['EUR', 'USD', 'GBP', 'CHF'];
BEGIN
    FOR i IN 1..8 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT202',
            bic_codes[((i-1) % 4) + 1],
            intl_bics[((i-1) % 4) + 1],
            (RANDOM() * 1000000 + 200000)::DECIMAL(10,2),
            currencies[((i-1) % 4) + 1],
            CURRENT_DATE + i + 15,
            '{1:F01' || bic_codes[((i-1) % 4) + 1] || '0000000000}{2:I202' || intl_bics[((i-1) % 4) + 1] || 'N}{4:
:20:MT202REF' || LPAD(i::TEXT, 3, '0') || '
:21:CORP2024' || LPAD(i::TEXT, 3, '0') || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 15, 'YYMMDD') || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 1000000 + 200000) || ',00
:52A:' || bic_codes[((i-1) % 4) + 1] || '
:53A:' || bic_codes[((i-1) % 4) + 1] || '
:56A:' || intl_bics[((i-1) % 4) + 1] || '
:57A:' || intl_bics[((i-1) % 4) + 1] || '
:58A:/ACCOUNTNUMBER
INTERNATIONAL BANK
COUNTRY
:72:/ACC/INTERBANK SETTLEMENT ' || i || '
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- Generate 8 more MT202COV messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX'];
    intl_bics TEXT[] := ARRAY['DEUTDEFFXXX', 'CHASUS33XXX', 'BARCGB22XXX', 'UBSWCHZHXXX'];
    currencies TEXT[] := ARRAY['EUR', 'USD', 'GBP', 'CHF'];
BEGIN
    FOR i IN 1..8 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT202COV',
            bic_codes[((i-1) % 4) + 1],
            intl_bics[((i-1) % 4) + 1],
            (RANDOM() * 800000 + 150000)::DECIMAL(10,2),
            currencies[((i-1) % 4) + 1],
            CURRENT_DATE + i + 20,
            '{1:F01' || bic_codes[((i-1) % 4) + 1] || '0000000000}{2:I202COV' || intl_bics[((i-1) % 4) + 1] || 'N}{4:
:20:MT202COVREF' || LPAD(i::TEXT, 3, '0') || '
:21:COV2024' || LPAD(i::TEXT, 3, '0') || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 20, 'YYMMDD') || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 800000 + 150000) || ',00
:52A:' || bic_codes[((i-1) % 4) + 1] || '
:53A:' || bic_codes[((i-1) % 4) + 1] || '
:56A:' || intl_bics[((i-1) % 4) + 1] || '
:57A:' || intl_bics[((i-1) % 4) + 1] || '
:58A:/ACCOUNTNUMBER
INTERNATIONAL BANK
COUNTRY
:72:/ACC/COVER PAYMENT FOR MT103 ' || i || '
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- Generate 8 more MT203 messages
DO $$
DECLARE 
    i INTEGER;
    bic_codes TEXT[] := ARRAY['TGBATRIXXX', 'AKBKTRISKXXX', 'ISBKTRISAHXXX', 'YAPITRISXXX'];
    intl_bics TEXT[] := ARRAY['DEUTDEFFXXX', 'CHASUS33XXX', 'BARCGB22XXX', 'UBSWCHZHXXX'];
    currencies TEXT[] := ARRAY['EUR', 'USD', 'GBP', 'CHF'];
BEGIN
    FOR i IN 1..8 LOOP
        INSERT INTO swift_messages (
            message_type, sender_bic, receiver_bic, amount, currency, value_date,
            raw_mt_message, generated_mx_message, created_at, updated_at
        ) VALUES (
            'MT203',
            bic_codes[((i-1) % 4) + 1],
            intl_bics[((i-1) % 4) + 1],
            (RANDOM() * 1500000 + 300000)::DECIMAL(10,2),
            currencies[((i-1) % 4) + 1],
            CURRENT_DATE + i + 25,
            '{1:F01' || bic_codes[((i-1) % 4) + 1] || '0000000000}{2:I203' || intl_bics[((i-1) % 4) + 1] || 'N}{4:
:20:MT203REF' || LPAD(i::TEXT, 3, '0') || '
:21:BATCH2024' || LPAD(i::TEXT, 3, '0') || '
:32A:' || TO_CHAR(CURRENT_DATE + i + 25, 'YYMMDD') || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 1500000 + 300000) || ',00
:52A:' || bic_codes[((i-1) % 4) + 1] || '
:53A:' || bic_codes[((i-1) % 4) + 1] || '
:56A:' || intl_bics[((i-1) % 4) + 1] || '
:57A:' || intl_bics[((i-1) % 4) + 1] || '
:58A:/ACCOUNTNUMBER
INTERNATIONAL BANK
COUNTRY
:72:/ACC/MULTIPLE INTERBANK SETTLEMENTS ' || i || '
:21:TRX' || LPAD(i::TEXT, 3, '0') || 'A
:32B:' || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 500000 + 100000) || ',00
:58A:/ACCOUNTNUMBER
INTERNATIONAL BANK
COUNTRY
:21:TRX' || LPAD(i::TEXT, 3, '0') || 'B
:32B:' || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 500000 + 100000) || ',00
:58A:/ACCOUNTNUMBER
INTERNATIONAL BANK
COUNTRY
:21:TRX' || LPAD(i::TEXT, 3, '0') || 'C
:32B:' || currencies[((i-1) % 4) + 1] || TRUNC(RANDOM() * 500000 + 100000) || ',00
:58A:/ACCOUNTNUMBER
INTERNATIONAL BANK
COUNTRY
-}',
            NULL,
            NOW(),
            NOW()
        );
    END LOOP;
END $$;

-- Summary
SELECT 
    message_type,
    COUNT(*) as message_count,
    SUM(amount) as total_amount,
    STRING_AGG(DISTINCT currency, ', ') as currencies
FROM swift_messages 
GROUP BY message_type 
ORDER BY message_type; 