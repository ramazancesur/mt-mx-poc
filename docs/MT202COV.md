# 🔄 MT202COV - Cover Payment

**MT202COV** mesajı, cover payment (teminat ödemesi) için kullanılan özel SWIFT mesaj tipidir. MT103 işlemlerinin karşılığını sağlamak için bankalar arası kullanılır.

## 🎯 Genel Bilgiler

| Özellik | Değer |
|---------|--------|
| **Mesaj Tipi** | MT202COV |
| **Kategori** | Cover Payment |
| **Kullanım Alanı** | MT103 cover işlemleri |
| **ISO 20022 Karşılığı** | pacs.009.001.08 |
| **Ana Fark** | :50A: ve :59A: alanları içerir |

## ✅ Zorunlu Alanlar

MT202'ye ek olarak:

| Alan | SWIFT Kodu | Açıklama | Format |
|------|------------|----------|--------|
| **Underlying Customer** | :50A:/:50F:/:50K: | Asıl müşteri | BIC veya detay |
| **Underlying Beneficiary** | :59A:/:59F: | Asıl yararlanıcı | BIC veya detay |

## 📄 Örnek Mesaj

```swift
{1:F01ISBKTRISAXXX0000000000}
{2:I202CHASDEFXXXXN}
{3:{108:MT202COV}}
{4:
:20:COV350042637234
:21:FT21350042637234
:32A:250626GBP15000,00
:50A:ISBKTRISAXXX
:52A:ISBKTRISAXXX
:58A:BARCGB22XXX
:59A:BARCGB22XXX
:72:/COVER/FOR MT103
/ORIG/FT21350042637234
-}
{5:{CHK:C9F8E6D2A1B4}}
```

## 🔗 İlgili Dokümanlar

- [MT202 Dokümantasyonu](./MT202.md)
- [MT103 Dokümantasyonu](./MT103.md) 