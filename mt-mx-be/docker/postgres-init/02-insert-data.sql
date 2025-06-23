-- Insert sample SWIFT MT messages
-- Clean existing data first
TRUNCATE TABLE swift_messages RESTART IDENTITY CASCADE;

-- MT103 - Single Customer Credit Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT103', 'BANKBEBBAXXX', 'BANKDEFFXXXX', 5000.00, 'EUR', '2025-06-22',
    '{1:F01BANKBEBBAXXX0000000000}{2:I103BANKDEFFXXXXN}{4:
:20:REF103SINGLE123
:23B:CRED
:32A:250622EUR5000,00
:33B:EUR5000,00
:50K:/1122334455
AHMET YILDIZ
KADIKOY/ISTANBUL
:52A:BANKUS33XXX
:53A:BANKBEBBAXXX
:54A:BANKGB2LXXX
:56A:BANKDEFFXXXX
:57A:BANKFRPPXXX
:59:/9988776655
SELIN AKSOY
BAKIRKOY/ISTANBUL
:70:Ücret açıklaması veya referans
:71A:SHA
:72:/INS/Özel talimatlar
-}',
    NULL,
    NOW(), NOW()
);

-- MT102 - Multiple Customer Credit Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT102', 'BANKBEBBAXXX', 'BANKDEFFXXXX', 20000.00, 'EUR', '2025-06-22',
    '{1:F01BANKBEBBAXXX0000000000}{2:I102BANKDEFFXXXXN}{4:
:20:REF102BATCH12345
:21:RREF987654321
:32A:250622EUR20000,00
:50A:BANKBEBBAXXX
:52A:BANKUS33XXX
:57A:BANKDEFFXXXX
:58A:BANKFRPPXXX
:72:/INS/Toplu ödeme açıklaması
:70:Ödeme talimatı toplu referans
:15A:PAYM
:21A:TRANS1
:32B:EUR10000,00
:50K:/123456789
ALI YILMAZ
ISTANBUL
:59:/987654321
AYŞE DEMİR
ANKARA
:70:Birinci ödeme açıklaması
:21A:TRANS2
:32B:EUR10000,00
:50K:/223344556
MEHMET CAN
IZMIR
:59:/654321987
FATMA KAYA
BURSA
:70:İkinci ödeme açıklaması
-}',
    NULL,
    NOW(), NOW()
);

-- MT202 - General Financial Institution Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT202', 'BANKBEBBAXXX', 'BANKDEFFXXXX', 100000.00, 'USD', '2025-06-22',
    '{1:F01BANKBEBBAXXX0000000000}{2:I202BANKDEFFXXXXN}{4:
:20:REFERENCE12345678
:21:RELATEDREF98765432
:32A:250622USD100000,00
:52A:BANKUS33XXX
:53A:BANKBEBBAXXX
:56A:BANKGB2LXXX
:57A:BANKDEFFXXXX
:58A:BANKFRPPXXX
:72:/INS/INSTRUCTION INFO
-}',
    NULL,
    NOW(), NOW()
);

-- MT203 - Multiple General Financial Institution Transfer
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT203', 'BANKBEBBAXXX', 'BANKDEFFXXXX', 250000.00, 'USD', '2025-06-22',
    '{1:F01BANKBEBBAXXX0000000000}{2:I203BANKDEFFXXXXN}{4:
:20:REF203CORR123
:21:RELATEDREF203
:32A:250622USD250000,00
:52A:BANKUS33XXX
:53A:BANKBEBBAXXX
:54A:BANKNL2AXXX
:57A:BANKDEFFXXXX
:58A:BANKFRPPXXX
:72:/INS/Muhabir banka transfer talimatı
-}',
    NULL,
    NOW(), NOW()
);

-- MT202COV - General Financial Institution Transfer with Cover
INSERT INTO swift_messages (
    message_type, sender_bic, receiver_bic, amount, currency, value_date,
    raw_mt_message, generated_mx_message, created_at, updated_at
) VALUES (
    'MT202COV', 'BANKBEBBAXXX', 'BANKDEFFXXXX', 100000.00, 'USD', '2025-06-22',
    '{1:F01BANKBEBBAXXX0000000000}{2:I202COVBANKDEFFXXXXN}{4:
:20:REFERENCE87654321
:21:RELATEDREF12345678
:32A:250622USD100000,00
:52A:BANKUS33XXX
:53A:BANKBEBBAXXX
:56A:BANKGB2LXXX
:57A:BANKDEFFXXXX
:58A:BANKFRPPXXX
:72:/INS/INSTRUCTION INFO
:50A:/123456789
JOHN DOE
:59:/987654321
JANE SMITH
-}',
    NULL,
    NOW(), NOW()
);
