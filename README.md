<div align="center">

# 💸 SpendWise AI

**AI destekli kişisel finans takip uygulaması**

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android&logoColor=white)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Gemini](https://img.shields.io/badge/AI-Gemini%202.5%20Flash-8E75B2?logo=google&logoColor=white)](https://deepmind.google/gemini)

*Harcamalarını doğal dille yaz, AI otomatik analiz etsin.*

</div>

---

## 📱 Ekran Görüntüleri

<div align="center">
<table>
  <tr>
    <td align="center">
      <img src="screenshots/Screenshot_20260322_194040.png" width="160"/><br/>
      <sub><b>Ana Sayfa</b></sub>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_20260322_194101.png" width="160"/><br/>
      <sub><b>Gider Takibi</b></sub>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_20260322_194119.png" width="160"/><br/>
      <sub><b>Gelir Takibi</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="screenshots/Screenshot_20260322_194135.png" width="160"/><br/>
      <sub><b>AI Finans Koçu</b></sub>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_20260322_194222.png" width="160"/><br/>
      <sub><b>Bütçe Takibi</b></sub>
    </td>
    <td align="center">
      <img src="screenshots/Screenshot_20260322_194239.png" width="160"/><br/>
      <sub><b>Ayarlar</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="screenshots/login.png" width="160"/><br/>
      <sub><b>Giriş Ekranı</b></sub>
    </td>
    <td align="center">
      <img src="screenshots/add_transaction.png" width="160"/><br/>
      <sub><b>İşlem Ekle (AI)</b></sub>
    </td>
    <td align="center">
      <img src="screenshots/chart.png" width="160"/><br/>
      <sub><b>Harcama Grafiği</b></sub>
    </td>
  </tr>
</table>
</div>

---

## ✨ Özellikler

### 🤖 Yapay Zeka ile Harcama Girişi
Sadece ne harcadığını yaz, AI gerisini halleder:
```
"Migros'ta 340 TL market yaptım"  →  Market / 340 TL / Gider
"Maaş 18000 TL geldi"             →  Maaş / 18000 TL / Gelir
"Starbucks kahve 95 TL"           →  Yemek & İçecek / 95 TL / Gider
```
- **Google Gemini 2.5 Flash** — hızlı ve akıllı kategori tespiti
- **Regex fallback** — internet kesilse de çalışmaya devam eder
- 17 farklı kategori otomatik tespit edilir

### 📊 Finansal Takip
- Gelir & gider kaydı, **anlık bakiye** hesaplama
- **Donut grafik** — kategorilere göre harcama dağılımı
- **Net durum** — gelir vs gider karşılaştırması
- Günlük harcama özeti

### 🎯 Bütçe Yönetimi
- Aylık bütçe limiti belirle
- Gerçek zamanlı harcama takibi (% olarak)
- Limite yaklaşınca ⚡ uyarı, aşınca ⚠️ uyarı
- Kategori bazlı bütçe dağılımı

### 🤖 AI Finans Koçu
- Harcama verilerini analiz eder
- Kişiselleştirilmiş tasarruf önerileri sunar
- Somut rakamlarla hedef belirler
- Gemini ile Türkçe rapor üretir

### 🔄 Bulut Senkronizasyonu
- **Firebase Firestore** — telefon değişse bile veriler kaybolmaz
- Her işlem anında buluta yedeklenir
- **Kullanıcıya özel veri izolasyonu** — farklı hesaplar birbirini görmez
- Offline çalışma desteği (Room)

### 🔐 Kimlik Doğrulama
- **Google ile tek tuşla giriş**
- E-posta / şifre ile kayıt ve giriş
- Firebase Authentication altyapısı

### 🌍 Çoklu Dil & Para Birimi
- 🇹🇷 Türkçe / 🇬🇧 İngilizce
- TL · USD · EUR · GBP
- Anlık dil değiştirme, uygulama yeniden başlar

---

## 🏗️ Mimari

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│                                                         │
│   LoginScreen   HomeScreen   AddExpenseScreen           │
│   BudgetScreen  InsightsScreen  TransactionsScreen      │
│        │              │                │                │
│   LoginVM    DashboardVM    AddExpenseVM   BudgetVM     │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                   Domain Layer                          │
│                                                         │
│         AddExpenseUseCase   ExpenseTextParser           │
└──────────┬──────────────────────────────┬───────────────┘
           │                              │
┌──────────▼───────────┐    ┌─────────────▼──────────────┐
│    Local (Room)      │    │      Remote (Firebase)     │
│                      │    │                            │
│  TransactionDao      │    │   FirestoreRepository      │
│  CategoryDao    ◄────┼────┤   AuthRepository           │
│  InsightDao          │    │   Firebase Auth            │
│                      │    │   Cloud Firestore          │
└──────────────────────┘    └────────────────────────────┘
                                          │
                             ┌────────────▼──────────────┐
                             │        AI Layer           │
                             │                           │
                             │   GeminiRestClient        │
                             │   GeminiExpenseParser     │
                             │   GeminiInsightsGen       │
                             │   RegexFallbackParser     │
                             └───────────────────────────┘
```

**Veri akışı — İşlem ekleme:**
```
Kullanıcı yazar
    → Gemini API (NLP analiz)
    → ParsedTransaction (amount, currency, category, type)
    → Room'a yaz  ──→  UI anında güncellenir
    → Firestore'a sync (arka planda, hata UI'ı bloklamaz)
```

**Login / Logout veri izolasyonu:**
```
Giriş  →  Room'u temizle  →  Firestore'dan kullanıcı verisini yükle
Çıkış  →  Room'u temizle  →  Login ekranı
```

---

## 🛠️ Kullanılan Teknolojiler

| Kategori | Teknoloji | Versiyon |
|---|---|---|
| Dil | Kotlin | 1.9+ |
| UI Framework | Jetpack Compose | BOM 2024 |
| Design System | Material 3 | — |
| Navigasyon | Navigation Compose | 2.7+ |
| Yerel Veritabanı | Room (SQLite) | 2.6+ |
| Reactive | Kotlin Flow + Coroutines | 1.7+ |
| Bulut Veritabanı | Firebase Firestore | — |
| Kimlik Doğrulama | Firebase Auth | — |
| Yapay Zeka | Google Gemini 2.5 Flash | REST API |
| Dependency Injection | Manuel (AppContainer) | — |
| Min SDK | Android 8.0 (API 26) | — |
| Target SDK | Android 14 (API 34) | — |



</div>
