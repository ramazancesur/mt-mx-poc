# 🏦 SWIFT Mesaj Tipleri Dokümantasyonu

Bu dokümantasyon, MT-MX sisteminde desteklenen SWIFT mesaj tiplerinin detaylı açıklamalarını içerir.

## 📋 İçindekiler

- [Desteklenen Mesaj Tipleri](#desteklenen-mesaj-tipleri)
- [Ortak Alanlar](#ortak-alanlar)
- [Detaylı Mesaj Dokümantasyonları](#detaylı-mesaj-dokümantasyonları)
- [Validasyon Kuralları](#validasyon-kuralları)
- [Örnek Kullanım](#örnek-kullanım)

## 📊 Desteklenen Mesaj Tipleri

| Mesaj Tipi | Açıklama | Dokümantasyon |
|------------|----------|---------------|
| **MT102** | Multiple Customer Credit Transfer | [📄 MT102 Detayları](./MT102.md) |
| **MT103** | Single Customer Credit Transfer | [📄 MT103 Detayları](./MT103.md) |
| **MT202** | General Financial Institution Transfer | [📄 MT202 Detayları](./MT202.md) |
| **MT202COV** | Cover Payment | [📄 MT202COV Detayları](./MT202COV.md) |
| **MT203** | Multiple General Financial Institution Transfer | [📄 MT203 Detayları](./MT203.md) |

## 🔧 Ortak Alanlar

Tüm SWIFT mesajlarında bulunan ortak alanlar:

### 📋 Temel Alanlar

| Alan | Açıklama | Zorunlu | Format | Örnek |
|------|----------|---------|--------|--------|
| `messageType` | Mesaj tipi | ✅ | String | MT103 |
| `senderBic` | Gönderen BIC kodu | ✅ | 8-11 karakter | ISBKTRISAXXX |
| `receiverBic` | Alıcı BIC kodu | ✅ | 8-11 karakter | BARCGB22XXX |
| `amount` | Transfer tutarı | ✅ | Decimal | 15000.00 |
| `currency` | Para birimi | ✅ | 3 karakter ISO | EUR, USD, GBP |
| `valueDate` | Valör tarihi | ✅ | YYYY-MM-DD | 2025-06-24 |

### 📄 Mesaj İçeriği

| Alan | Açıklama | Zorunlu | Format |
|------|----------|---------|--------|
| `rawMtMessage` | Ham MT mesaj içeriği | ✅ | SWIFT MT format |
| `generatedMxMessage` | Dönüştürülmüş MX XML | 🔄 | ISO 20022 XML |

### 📅 Sistem Alanları

| Alan | Açıklama | Zorunlu | Format |
|------|----------|---------|--------|
| `id` | Benzersiz kimlik | 🔄 | Long |
| `createdAt` | Oluşturulma tarihi | 🔄 | Timestamp |
| `updatedAt` | Güncellenme tarihi | 🔄 | Timestamp |

## 🔍 Detaylı Mesaj Dokümantasyonları

Her mesaj tipi için ayrıntılı dokümantasyon:

### 💳 Customer Credit Transfers
- **[MT102](./MT102.md)** - Multiple Customer Credit Transfer
- **[MT103](./MT103.md)** - Single Customer Credit Transfer

### 🏛️ Financial Institution Transfers
- **[MT202](./MT202.md)** - General Financial Institution Transfer
- **[MT202COV](./MT202COV.md)** - Cover Payment
- **[MT203](./MT203.md)** - Multiple General Financial Institution Transfer

## ✅ Validasyon Kuralları

### 🔒 BIC Kodu Validasyonu
```
Format: [A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?
Uzunluk: 8 veya 11 karakter
Örnek: ISBKTRISAXXX, BARCGB22
```

### 💰 Tutar Validasyonu
```
Minimum: 0.01
Maksimum: 999,999,999.99
Ondalık: En fazla 2 basamak
```

### 📅 Tarih Validasyonu
```
Format: YYYY-MM-DD
Minimum: Bugün
Maksimum: Bugün + 1 yıl
```

### 💱 Para Birimi Validasyonu
```
Format: ISO 4217 kodu
Uzunluk: 3 karakter
Desteklenen: EUR, USD, GBP, TRY
```

## 🚀 Örnek Kullanım

### API ile Mesaj Oluşturma

```bash
POST /api/swift-messages
Content-Type: application/json

{
  "messageType": "MT103",
  "senderBic": "ISBKTRISAXXX",
  "receiverBic": "BARCGB22XXX",
  "amount": 15000.00,
  "currency": "GBP",
  "valueDate": "2025-06-26",
  "rawMtMessage": "{1:F01ISBKTRISAXXX...}"
}
```

### Mesaj Dönüştürme

```bash
POST /api/swift-messages/{id}/convert
```

### XML Güncelleme

```bash
PUT /api/swift-messages/{id}/update-xml
Content-Type: application/json

"<?xml version='1.0' encoding='UTF-8'?>
<Document xmlns='urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08'>
  ...
</Document>"
```

## 🔄 MT ↔ MX Dönüştürme

### MT → MX Dönüştürme Süreci
1. **MT Parsing**: SWIFT MT mesajını ayrıştırma
2. **Field Extraction**: Anahtar alanları çıkarma
3. **XML Generation**: ISO 20022 formatında XML oluşturma
4. **Validation**: XSD şema doğrulaması

### MX → MT Dönüştürme Süreci
1. **XML Parsing**: MX XML'ini ayrıştırma
2. **Data Mapping**: Alanları MT formatına eşleme
3. **MT Generation**: SWIFT MT formatında mesaj oluşturma
4. **Validation**: SWIFT standartlarına uygunluk kontrolü

## 📚 İlgili Dokümanlar

- [API Dokümantasyonu](../README.md#api-dokümantasyonu)
- [Geliştirici Rehberi](../README.md#geliştirme)
- [Test Dokümantasyonu](../README.md#test-coverage)

---

> **💡 Not**: Detaylı alan açıklamaları ve örnekler için ilgili mesaj tipinin dokümantasyon dosyasını inceleyiniz. 