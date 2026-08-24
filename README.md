# Teknisio

Teknisio adalah sistem pemesanan jasa servis elektronik dengan backend Spring Boot dan aplikasi Android. Project ini berawal dari project akhir mata kuliah dan dikembangkan melalui kolaborasi; riwayat Git menjadi rujukan kontribusi masing-masing, sehingga repository ini tidak mengklaim seluruh source sebagai karya satu orang.

Fokus teknis project adalah pemodelan lifecycle permintaan layanan, pemisahan akses pelanggan dan teknisi, komunikasi client–server, serta penerapan kontrol keamanan dasar pada API dan aplikasi mobile. Project ini bukan layanan production dan masih membutuhkan pengujian end-to-end yang lebih luas.

## Fitur yang Tersedia

- Registrasi dan login pelanggan/teknisi.
- JWT authentication dan role-based authorization.
- Pengelolaan kategori perangkat serta keahlian teknisi.
- Pencarian teknisi dan pembuatan permintaan layanan.
- Alur menerima, menolak, memulai, membatalkan, dan menyelesaikan permintaan.
- Riwayat status dan ulasan layanan.
- Chat dan pembaruan lokasi melalui REST/WebSocket.
- Aplikasi Android dengan Retrofit, OkHttp, dan penyimpanan sesi lokal.

## Arsitektur

```text
Android client
      |
      | HTTPS / WSS
      v
Spring Boot REST + WebSocket API
      |
      v
PostgreSQL + Flyway migrations
```

Backend menggunakan struktur controller–service–repository. Identitas user diambil dari JWT yang sudah diverifikasi; endpoint domain menerapkan pembatasan role dan ownership sesuai layanan yang diakses.

## Teknologi

| Bagian | Teknologi |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5, Gradle Kotlin DSL |
| Database | PostgreSQL 16, Spring Data JPA, Flyway |
| Security | Spring Security, BCrypt, JWT, Bucket4j |
| Mobile | Android Java, XML Layout, Retrofit, OkHttp |
| Realtime | Spring WebSocket/STOMP |
| Development | Docker Compose |

## Struktur Repository

```text
teknisio_backend/   REST API, WebSocket, migration, dan test backend
teknisio_mobile/    aplikasi Android
teknisio_document/  rancangan dan catatan pengembangan
nixpacks.toml       konfigurasi build deployment yang pernah digunakan
```

Build output, APK, crash log, state IDE, dan `local.properties` tidak disimpan sebagai source. Artifact rilis sebaiknya dibuat dari commit yang sudah direview.

## Menjalankan Backend

Persyaratan:

- JDK 17
- Docker dan Docker Compose untuk database development

Siapkan konfigurasi lokal:

```bash
cd teknisio_backend
cp .env.example .env
```

Isi minimal `POSTGRES_PASSWORD` dan `JWT_SECRET` pada `.env`. JWT secret harus berupa nilai acak minimal 32 byte dan tidak boleh dimasukkan ke Git.

Jalankan PostgreSQL dan backend:

```bash
docker compose up -d postgres
./gradlew bootRun
```

Default port backend adalah `8080`. Flyway menjalankan migration saat aplikasi mulai.

### Rate Limit Authentication

Endpoint login dibatasi secara default menjadi 5 request per menit per alamat client. Kedua endpoint registrasi berbagi batas 10 request per jam per alamat client. Nilai dapat disesuaikan melalui variabel `AUTH_*` yang tercantum di `.env.example`.

Limiter ini bekerja per instance aplikasi dan memiliki cache client yang dibatasi ukurannya. Untuk deployment dengan beberapa instance, tambahkan rate limit bersama pada reverse proxy atau API gateway yang tepercaya.

## Menjalankan Aplikasi Android

Persyaratan: Android SDK dengan compile SDK 36 dan JDK yang kompatibel dengan Android Gradle Plugin project.

Debug emulator menggunakan `http://10.0.2.2:8080/` secara default. Pengecualian cleartext hanya tersedia pada source set `debug` untuk emulator/localhost:

```bash
cd teknisio_mobile
./gradlew assembleDebug
```

Base URL debug dapat diubah dengan Gradle property `TEKNISIO_DEBUG_API_BASE_URL`. Untuk perangkat fisik, gunakan endpoint development HTTPS bila host bukan localhost/emulator.

Release hanya menerima HTTPS. Jika property tidak diberikan, build memakai domain placeholder yang tidak melayani traffic:

```bash
./gradlew assembleRelease \
  -PTEKNISIO_API_BASE_URL=https://api.example.com/
```

Build akan ditolak jika `TEKNISIO_API_BASE_URL` release tidak diawali `https://`.

## Pengujian

Backend:

```bash
cd teknisio_backend
./gradlew test
```

Mobile unit test:

```bash
cd teknisio_mobile
./gradlew testDebugUnitTest
```

Smoke test WebSocket membuat fixture sementara dan mewajibkan `TEKNISIO_TEST_PASSWORD` dari environment. Script tidak mempunyai fallback password source-controlled. Jalankan hanya pada environment pengujian yang diotorisasi.

## Catatan Keamanan

- Backend gagal start bila password database atau JWT secret wajib tidak tersedia.
- Login menggunakan pesan error generik untuk mengurangi account enumeration.
- Login dan registrasi memiliki rate limiting per alamat client.
- Main/release Android menolak cleartext traffic; pengecualian lokal berada di `src/debug`.
- Token dan credential tidak boleh ditulis ke log atau disimpan di repository.
- Nilai deployment publik tidak di-hardcode pada source mobile.
- Kontrol rate limit in-memory bukan pengganti pembatasan terdistribusi pada edge.

## Status Project

MVP utama tersedia dan cukup menunjukkan integrasi backend, database, realtime, dan Android. Prioritas pengembangan berikutnya adalah memperluas negative security tests, memperjelas pembagian kontribusi, dan memvalidasi deployment dari konfigurasi bersih.
