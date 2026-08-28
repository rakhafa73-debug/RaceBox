# RaceBox — Pengukur Kecepatan & Jarak Motor (Android, Offline-First)

Aplikasi Android untuk tim racing: merekam kecepatan, jarak, dan lap secara real-time dari sensor GPS, menyimpan semua data secara offline (Room/SQLite), dengan data GPS terenkripsi AES-256 di penyimpanan lokal, lalu menyinkronkannya ke backend batch via REST.

## Fitur

- **Autentikasi offline** — login/registrasi lokal, kata sandi di-hash `bcrypt`, sesi disimpan terenkripsi AES-256 (Android Keystore).
- **GPS tracking kontinu** — `FusedLocationProviderClient`, update setiap 1–2 detik lewat *foreground service*.
- **Lap recorder** — tombol "Mulai Lap" / "Selesai Lap", setiap titik GPS ditulis ke SQLite.
- **Ringkasan race** — statistik (rata-rata, maksimum, jarak, durasi), grafik kecepatan, heat-map jalur (custom View, offline).
- **Export & Share** — laporan CSV dan JSON dibagikan via share sheet (FileProvider).
- **Sync** — `WorkManager` periodik + tombol "Sinkronkan Sekarang" ke `POST /sync`.
- **Notifikasi** — pada race tersimpan dan sinkronisasi selesai.

## Arsitektur

```
app/src/main/java/com/racebox/app/
├── RaceBoxApp.kt / MainActivity.kt      # Entry point, container manual DI
├── di/AppContainer.kt                    # Manual DI (no Hilt)
├── data/
│   ├── db/                               # Room: User, Race, Lap, GpsPoint
│   ├── security/                         # CryptoUtils (AES-GCM 256) + PasswordHasher (bcrypt)
│   ├── prefs/                            # SecurePrefs + UserSession (terenkripsi)
│   ├── sync/                             # RaceBoxApi/Retrofit, SyncClient, SyncRepository, SyncWorker
│   └── export/                           # RaceExporter (CSV/JSON) + share
├── domain/
│   ├── geo/GeoUtils.kt                   # Haversine
│   ├── track/GpsTracker.kt               # FusedLocationProviderClient (callback → Flow)
│   └── race/                             # RaceSession, LapSlice, LapStats
├── repository/                           # AuthRepository, RaceRepository
├── tracking/TrackingService.kt           # Foreground service GPS
├── ui/                                   # Login, Dashboard, Tracking, History, Summary
│   ├── notify/NotificationHelper.kt
│   └── views/                            # SpeedChartView, TrackMapView (heat-map)
```

## Keamanan data

- Kata sandi: hash bcrypt (`org.mindrot:jbcrypt`).
- Koordinat GPS & kecepatan: dienkripsi AES-256-GCM dengan kunci di **Android Keystore** sebelum ditulis ke Room (`latitudeEnc`, `longitudeEnc`, `speedKmhEnc`), didekripsi saat dibaca untuk chart/export/sync.
- Sesi login: disimpan terenkripsi di `SharedPreferences`.

## Cara build

1. Buka folder ini di **Android Studio** (Koala / 2023.1+) — IDE akan meng-generate `gradle wrapper` (Direktori `gradle/wrapper` sudah berisi `gradle-wrapper.properties`).
2. Sync Gradle, lalu jalankan ke emulator/device dengan API 21+.
3. Backend default (dari emulator): `http://10.0.2.2:3000/` → ubah `backend_base_url` di `res/values/strings.xml`.

## Catatan / batasan implementasi awal

- **Tanpa server backend** di repo ini — implementasikan REST `POST /sync` (lihat skema `SyncPayload` di `data/sync/SyncModels.kt`) agar sinkronisasi berhasil.
- **Garansi GPS**: minta izin lokasi saat pertama kali (Android 6+); pada Android 13+ izin notifikasi juga diminta.
- **Proses di-kill saat race berlangsung** segmen terakhir bisa hilang (lap aktif baru di-finalisasi saat tombol lap/stop); data yang sudah masuk ke lap tersimpan permanen.
- **Mulai Lap / Selesai Lap**: sesuai UC, lap hanya tercatat saat tombol dipicu; di antara lap, titik GPS ditampung sementara dan disambungkan ke lap berikutnya.

## Stack

Kotlin 1.9 · AGP 8.2 · minSdk 21 / targetSdk 34 · Room 2.6 (KSP) · Navigation · WorkManager · Retrofit/OkHttp · Play Services Location · Material 3. Versi lengkap di `gradle/libs.versions.toml`.