# NFC Verihubs Reader - Android App

Aplikasi Android untuk membaca kartu NFC (e-KTP & e-Sertifikat) dan memverifikasinya menggunakan API Verihubs Indonesia.

---

## 📁 Struktur Proyek

```
nfc-verihubs/
├── app/
│   ├── src/main/
│   │   ├── java/com/verihubs/nfcreader/
│   │   │   ├── NFCApplication.kt           # Hilt application class
│   │   │   ├── MainActivity.kt             # Entry point + NFC foreground dispatch
│   │   │   │
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── VerihubsApiService.kt    # Retrofit interface (endpoints)
│   │   │   │   │   └── VerihubsApiClient.kt     # OkHttp + auth interceptor
│   │   │   │   ├── model/
│   │   │   │   │   └── Models.kt                # Request/response data classes
│   │   │   │   └── repository/
│   │   │   │       └── VerihubsRepository.kt    # API calls + Flow results
│   │   │   │
│   │   │   ├── nfc/
│   │   │   │   ├── NFCReader.kt            # Low-level NFC/ISO 7816 reader
│   │   │   │   └── NFCViewModel.kt         # State management + business logic
│   │   │   │
│   │   │   ├── ui/
│   │   │   │   ├── screens/
│   │   │   │   │   ├── MainScreen.kt       # Navigation host + bottom bar
│   │   │   │   │   ├── ScannerScreen.kt    # NFC scanning UI + animations
│   │   │   │   │   ├── ResultScreens.kt    # e-KTP & e-Certificate result views
│   │   │   │   │   ├── HistoryScreen.kt    # Scan history list
│   │   │   │   │   └── SettingsScreen.kt   # API config & app info
│   │   │   │   ├── components/
│   │   │   │   │   └── BottomNavBar.kt     # Navigation bar
│   │   │   │   └── theme/
│   │   │   │       └── Theme.kt            # Colors + MaterialTheme
│   │   │   │
│   │   │   └── utils/
│   │   │       └── AppModule.kt            # Hilt DI providers
│   │   │
│   │   ├── res/
│   │   │   ├── xml/nfc_tech_filter.xml     # NFC tech filters
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   │
│   │   └── AndroidManifest.xml             # NFC permissions + intent filters
│   │
│   ├── build.gradle                        # Dependencies
│   └── proguard-rules.pro
│
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## ⚙️ Setup & Konfigurasi

### 1. Tambahkan API Key Verihubs

Buka `app/build.gradle`, cari bagian `defaultConfig` dan isi API key Anda:

```groovy
buildConfigField "String", "VERIHUBS_API_KEY", "\"ISI_API_KEY_ANDA_DI_SINI\""
buildConfigField "String", "VERIHUBS_BASE_URL", "\"https://api.verihubs.com/v1/\""
```

> ⚠️ **Jangan** commit API key ke repository publik. Gunakan `local.properties` atau environment variable untuk produksi.

### 2. Minimum Requirements

| Item | Requirement |
|------|-------------|
| Android SDK | minSdk 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Kotlin | 1.9.10 |
| NFC Hardware | **Wajib** |
| Internet | **Wajib** (untuk API Verihubs) |

### 3. Build & Run

```bash
# Clone / ekstrak proyek
# Buka dengan Android Studio Hedgehog (2023.1.1) atau lebih baru

# Build debug APK
./gradlew assembleDebug

# Install ke device
./gradlew installDebug
```

---

## 🔌 Integrasi API Verihubs

### Endpoint yang Digunakan

| Endpoint | Method | Fungsi |
|----------|--------|--------|
| `/ecertificate/verify` | POST | Verifikasi e-Sertifikat dari NFC |
| `/ekyc/nfc` | POST | Verifikasi e-KTP dari NFC chip |
| `/ecertificate/{id}` | GET | Detail sertifikat by ID |
| `/document/liveness` | POST | Cek keaslian dokumen |

### Request Format

```json
// E-Certificate
{
  "chip_data": "HEX_DATA_DARI_NFC_CHIP",
  "certificate_id": "OPTIONAL_CERT_ID",
  "document_type": "ECERTIFICATE"
}

// E-KTP
{
  "chip_data": "HEX_DATA_DARI_NFC_CHIP",
  "nik": "OPTIONAL_NIK_UNTUK_CROSSCHECK",
  "dg1": "DATA_GROUP_1_HEX",
  "dg2": "DATA_GROUP_2_HEX",
  "sod": "SECURITY_OBJECT_DOC_HEX"
}
```

### Headers

```
Authorization: Bearer {API_KEY}
Content-Type: application/json
X-Client-Source: android-nfc-reader
```

---

## 📱 Fitur Aplikasi

### Scanner (Tab 1)
- Deteksi otomatis jenis dokumen (e-KTP / e-Sertifikat)
- Mode manual: pilih jenis dokumen sebelum scan
- Animasi pulse saat menunggu kartu
- Status real-time: Menunggu → Membaca → Memverifikasi → Hasil

### Hasil Scan
**E-KTP:** NIK, nama, tempat/tanggal lahir, jenis kelamin, agama, status perkawinan, pekerjaan, alamat lengkap, status chip, chip authentication

**E-Sertifikat:** Nama pemegang, nomor sertifikat, jenis sertifikat, penerbit, tanggal terbit/expired, skor verifikasi, data tambahan

### Riwayat (Tab 2)
- Daftar semua scan dalam sesi ini
- Badge valid/invalid per item
- Tipe dokumen dan timestamp

### Pengaturan (Tab 3)
- Informasi API key (masked)
- Base URL Verihubs
- Informasi versi aplikasi

---

## 🔧 Cara Kerja NFC

### Flow Pembacaan e-KTP (ISO 7816)

```
Tag Detected
     ↓
SELECT MF (Master File)
     ↓
SELECT Application (AID: A0000000184349 4E00000163)
     ↓
READ DG1 (Personal Data)
     ↓
READ DG2 (Photo/Biometric)
     ↓
READ SOD (Security Object)
     ↓
Send hex data → Verihubs API
     ↓
Display result
```

### Tech yang Didukung
- **IsoDep** → e-KTP (ISO 14443-4)
- **NfcA / NfcB** → Berbagai e-Certificate
- **Ndef** → Certificate dengan NDEF records
- **MifareClassic** → Kartu legacy

---

## 🛡️ Keamanan

- API key disimpan di `BuildConfig` (bukan plaintext di kode)
- HTTPS only (tidak ada cleartext traffic)
- ProGuard aktif di release build
- Data chip tidak disimpan ke disk (hanya in-memory)

---

## 📦 Dependencies Utama

| Library | Versi | Fungsi |
|---------|-------|--------|
| Jetpack Compose BOM | 2024.02.00 | UI Framework |
| Hilt | 2.50 | Dependency Injection |
| Retrofit2 | 2.9.0 | HTTP Client |
| OkHttp3 | 4.12.0 | Network + Logging |
| Navigation Compose | 2.7.6 | In-app navigation |
| Coroutines | 1.7.3 | Async operations |
| Room | 2.6.1 | Local database (history) |

---

## 🐛 Troubleshooting

| Masalah | Solusi |
|---------|--------|
| NFC tidak terdeteksi | Pastikan NFC aktif di Settings > Connections > NFC |
| API 401 | Periksa API key di `build.gradle` |
| Gagal baca chip | Tempelkan kartu rata, jangan goyang. Coba beberapa posisi |
| `TAG_LOST` exception | Kartu terlalu cepat diangkat, tempel lebih lama |
| Build error Hilt | Pastikan `@HiltAndroidApp` ada di `NFCApplication.kt` |

---

## 📞 Support Verihubs

- Dokumentasi: https://docs.verihubs.com
- Dashboard: https://dashboard.verihubs.com  
- Email: support@verihubs.com
