# Roadmap Backend Teknisio

Roadmap pengerjaan backend Teknisio untuk aplikasi desktop dan mobile.

README ini menjadi panduan backend. Detail kontrak API yang lebih lengkap akan dibuat terpisah di `API_CONTRACT.md`.

---

## Prinsip Utama

- Backend hanya satu untuk semua client.
- Client boleh berbeda: JavaFX Desktop, Android Java, atau client lain.
- Backend tetap melayani semuanya melalui REST API + JSON.
- Data yang keluar/masuk API wajib menggunakan English.
- Nama package, entity, tabel, dan function internal boleh tetap menggunakan bahasa Indonesia jika sudah ada.
- Jangan membuat endpoint khusus Android atau khusus Desktop.
- Jangan expose entity database langsung sebagai response API.
- DTO adalah kontrak antara backend dan frontend.
- Endpoint resmi menggunakan English.
- Endpoint Indonesia seperti `/api/kategori`, `/api/permintaan`, atau `/api/teknisi` tidak dipakai sebagai kontrak resmi.

---

## Flow Final MVP

```text
Customer login
↓
Customer masuk ke halaman home
↓
Customer melihat daftar alat elektronik:
Air Conditioner, Refrigerator, Washing Machine, Television, Fan, Rice Cooker, dll.
↓
Customer memilih salah satu alat elektronik, misalnya Air Conditioner
↓
Backend mencari technician yang punya keahlian Air Conditioner
↓
Frontend menampilkan daftar technician yang bisa menangani Air Conditioner
↓
Customer memilih salah satu technician
↓
Sistem menampilkan semua keahlian technician tersebut
Contoh: Air Conditioner + Refrigerator
↓
Customer boleh memilih Air Conditioner saja, atau Air Conditioner + Refrigerator
↓
Customer mengisi deskripsi masalah secara bebas
↓
Customer membuat service request
```

Untuk MVP, customer tidak memilih detail jenis servis seperti:

```text
AC Cleaning
AC Repair
Refrigerator Freon Refill
Washing Machine Cleaning
```

Customer hanya memilih kategori alat elektronik dan mengisi `issueDescription`.

---

## Istilah Resmi API

| Konsep | Nama API | Nama internal yang boleh tetap dipakai |
|---|---|---|
| Alat elektronik | `deviceCategory` | `KategoriLayanan` |
| Keahlian technician | `technicianDeviceCategory` | `TeknisiKategoriLayanan` |
| Permintaan layanan | `serviceRequest` | `PermintaanLayanan` |
| Kategori yang dipilih dalam order | `selectedDeviceCategories` | `PermintaanLayananKategori` |
| Deskripsi masalah | `issueDescription` | `deskripsiMasalah` |
| Technician | `technician` | `Teknisi` |

Catatan:
- `JenisLayanan` dan `TeknisiLayanan` boleh tetap ada sebagai legacy.
- Untuk MVP baru, flow tidak memakai `jenis_layanan`.
- Jika nanti fitur berkembang, `jenis_layanan` bisa dipakai lagi untuk detail layanan spesifik.

---

## 0. Fondasi Backend

Target: backend punya struktur rapi, response seragam, error handling, repository, dan siap dikembangkan per modul.

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-00 [MVP] Rapikan struktur package**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `config`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `model`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `repositories`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `dto`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `services`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `controllers`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `security`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `common`
    - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `response`
    - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat package `exception`
  - ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Package `websocket` ditunda sampai fitur realtime/chat/notifikasi
  - ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Package `common.util` ditunda sampai ada utility yang benar-benar dipakai

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-01 [MVP] Buat global response format**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat `ApiResponse<T>`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response sukses punya `success`, `message`, `data`, `errors`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response error punya `success`, `message`, `data`, `errors`

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-02 [MVP] Buat global exception handler**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat `GlobalExceptionHandler`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Handle validation error
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Handle not found
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Handle bad request
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Handle unauthorized
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Handle forbidden
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Handle internal server error

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-03 [MVP] Siapkan DTO validation**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambahkan dependency validation jika belum ada
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Gunakan `@NotBlank`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Gunakan `@NotNull`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Gunakan `@Email`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Gunakan `@Size`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Gunakan `@Pattern` jika perlu

- ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) **BE-04 [MVP] Buat repository untuk entity inti**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `UserRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `UserSessionRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `TeknisiProfileRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `KategoriLayananRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `PermintaanLayananRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `MediaPermintaanRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `PesanRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `NotifikasiRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `RiwayatStatusRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `ReviewRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `LogAktivitasRepository`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `TeknisiKategoriLayananRepository`
  - ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) `PermintaanLayananKategoriRepository`
  - ![finished](https://img.shields.io/badge/%5Blegacy%5D-lightgrey?style=flat-square) `JenisLayananRepository` tetap legacy
  - ![finished](https://img.shields.io/badge/%5Blegacy%5D-lightgrey?style=flat-square) `TeknisiLayananRepository` tetap legacy

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-05 [MVP] Validasi koneksi database dan Flyway**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `./gradlew clean build` sukses
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `./gradlew bootRun` sukses
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tabel terbentuk otomatis
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `/actuator/health` status `UP`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tidak ada error migration

---

## 1. Master Data Layanan / Alat Elektronik

Target: customer bisa melihat daftar alat elektronik dan backend bisa mencari technician berdasarkan keahlian alat elektronik.

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-20 [MVP] Seed data kategori alat elektronik**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori Air Conditioner
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori Refrigerator
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori Washing Machine
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori Television
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori Fan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori Rice Cooker
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tambah kategori lain jika UI membutuhkan, misalnya Oven atau Mixer
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Pastikan seed data tidak duplikat saat migration dijalankan ulang
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Pastikan semua data default `aktif = true`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tidak perlu seed jenis layanan/detail servis untuk MVP

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-21 [MVP] List kategori alat elektronik aktif**
  - Endpoint: `GET /api/device-categories`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Hanya tampilkan data `aktif = true`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Jangan tampilkan data yang sudah soft delete
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response menggunakan field English
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response berisi `deviceCategoryId`, `name`, `icon`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint boleh diakses tanpa login

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-22 [MVP] Detail kategori alat elektronik**
  - Endpoint: `GET /api/device-categories/{deviceCategoryId}`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi kategori ditemukan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi kategori aktif
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi kategori belum soft delete
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response menggunakan field English
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response berisi `deviceCategoryId`, `name`, `icon`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Jika kategori tidak ditemukan, return error rapi

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-23 [MVP] Buat relasi keahlian technician berdasarkan kategori alat elektronik**
  - Tabel: `teknisi_kategori_layanan`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Satu technician bisa memiliki banyak keahlian alat elektronik
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Satu kategori alat elektronik bisa dimiliki banyak technician
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Relasi menggunakan `id_teknisi_profile`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Relasi menggunakan `id_kategori`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Pastikan relasi tidak duplikat
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Pastikan data default `aktif = true`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Relasi ini digunakan untuk mencari technician berdasarkan alat elektronik yang dipilih customer

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-24 [MVP] Search technician berdasarkan kategori alat elektronik**
  - Endpoint: `GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint hanya untuk customer yang sudah login
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi `deviceCategoryId` ditemukan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi kategori aktif
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi kategori belum soft delete
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan technician yang memiliki keahlian pada kategori tersebut
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Hanya tampilkan technician dengan relasi keahlian aktif
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `technicianProfileId`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `name`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `profilePhoto`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `availabilityStatus`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `averageRating`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `ratingCount`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `totalJobs`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sertakan `supportedDeviceCategories`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response menggunakan field English

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-25 [MVP] Detail technician untuk customer**
  - Endpoint: `GET /api/customers/technicians/{technicianProfileId}`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint hanya untuk customer yang sudah login
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tanpa token return `Unauthorized`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Token technician return `Forbidden`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi technician ditemukan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi akun technician aktif
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `technicianProfileId`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `name`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `profilePhoto`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `availabilityStatus`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `averageRating`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `ratingCount`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `totalJobs`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `description`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `supportedDeviceCategories`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `supportedDeviceCategories` hanya berisi relasi keahlian aktif
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Jangan tampilkan kategori yang tidak aktif atau sudah soft delete
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response menggunakan field English
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Jika technician tidak ditemukan, return `Technician not found`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint digunakan setelah customer memilih technician

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-26 [NEXT] Filter technician**
  - Endpoint: `GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}&availabilityStatus=ONLINE&sort=rating`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Filter berdasarkan status ketersediaan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sort berdasarkan rating
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sort berdasarkan total pekerjaan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Sort berdasarkan nama
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi invalid `availabilityStatus`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi invalid `sort`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tanpa token return `Unauthorized`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Token technician return `Forbidden`
  - ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Sort berdasarkan harga jika harga technician sudah tersedia
  - ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Sort berdasarkan jarak jika latitude dan longitude technician sudah tersedia
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Untuk MVP awal, filter jarak boleh ditunda

---

## 2. Auth dan Session

Target: user bisa register, login, logout, refresh token, dan akses endpoint sesuai role.

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-10 [MVP] Register customer**
  - Endpoint: `POST /api/auth/register/customer`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi `name` wajib
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi `email` wajib dan unik
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi `phoneNumber` wajib dan unik
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi `password` minimal
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan password dalam bentuk hash
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Role otomatis `CUSTOMER`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Status akun otomatis `ACTIVE`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response field menggunakan English

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-11 [MVP] Register technician**
  - Endpoint: `POST /api/auth/register/technician`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan data user dengan role `TECHNICIAN`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat otomatis data `teknisi_profile`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Status ketersediaan default `OFFLINE`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Rating default 0
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Total pekerjaan default 0
  - ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) Keahlian technician dapat diatur setelah register melalui endpoint skill technician

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-12 [MVP] Login**
  - Endpoint: `POST /api/auth/login`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Login pakai email dan password
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Cek password dengan BCrypt
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Cek status akun `ACTIVE`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Generate JWT access token
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return token dan data user

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-13 [MVP] JWT authentication**
  - Endpoint: `GET /api/auth/profile`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat JWT validation
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Buat JWT filter
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Ambil user dari token
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint selain public/register/login wajib pakai token

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-14 [NEXT] Refresh token**
  - Endpoint: `POST /api/auth/refresh`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi refresh token
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi session belum expired
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi session belum revoked
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Generate access token baru

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-15 [MVP] Logout**
  - Endpoint: `POST /api/auth/logout`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Revoke session aktif
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Isi `revoked_at`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Token lama tidak bisa dipakai refresh

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-16 [MVP] Password hashing**
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Gunakan BCrypt
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Jangan pernah simpan password asli
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Jangan tampilkan `password_hash` di response

- ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) **BE-17 [MVP] Role-based access**
  - ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) Endpoint `/api/customers/**` hanya bisa diakses customer
  - ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) Endpoint `/api/technicians/**` hanya bisa diakses technician
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint umum bisa diakses tanpa login jika diperlukan
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint profil sendiri wajib login
  - ![Status](https://img.shields.io/badge/status-ongoing-blue?style=flat-square) Pastikan endpoint customer search technician tidak bentrok dengan endpoint self-service technician

---

## 3. Profil User dan Technician

Target: customer dan technician bisa melihat serta mengubah profil dasar. Technician juga bisa mengatur keahlian alat elektronik yang dikuasai.

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-30 [MVP] Lihat profil sendiri**
  - Endpoint: `GET /api/auth/profile`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `name`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `email`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `phoneNumber`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `profilePhoto`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `address`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `role`

- [ ] **BE-31 [MVP] Update profil sendiri**
  - Endpoint: `PUT /api/users/me`
  - [ ] Bisa update `name`
  - [ ] Bisa update `phoneNumber`
  - [ ] Bisa update `profilePhoto`
  - [ ] Bisa update `address`
  - [ ] Validasi `phoneNumber` jika berubah
  - [ ] Response menggunakan field English

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-32 [MVP] Lihat profil technician**
  - Endpoint untuk customer: `GET /api/customers/technicians/{technicianProfileId}`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `technicianProfileId`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `name`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `profilePhoto`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `availabilityStatus`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `averageRating`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `ratingCount`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `totalJobs`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `description`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Return `supportedDeviceCategories`
  - Catatan: sudah diimplementasikan lebih awal pada flow technician discovery / BE-25

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) **BE-33 [MVP] Technician update availability status**
  - Endpoint: `PATCH /api/technicians/me/status`
  - Request body: `availabilityStatus`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Status bisa `ONLINE`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Status bisa `OFFLINE`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Status bisa `BUSY`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Status bisa `ON_LEAVE`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Hanya technician yang boleh akses
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Customer token return `Forbidden`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tanpa token return `Unauthorized`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi invalid status
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Update `status_ketersediaan` di `teknisi_profile`
  - ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response menggunakan field English

- [ ] **BE-34 [NEXT] Technician update deskripsi profil**
  - Endpoint: `PUT /api/technicians/me/profile`
  - [ ] Bisa update `description`
  - [ ] Bisa update `profilePhoto` via user profile
  - [ ] Bisa update `address`

- [ ] **BE-35 [MVP] Technician tambah keahlian alat elektronik**
  - Endpoint: `POST /api/technicians/me/device-categories`
  - [ ] Request berisi `deviceCategoryId`
  - [ ] Validasi kategori ditemukan
  - [ ] Validasi kategori aktif
  - [ ] Validasi kategori belum soft delete
  - [ ] Cegah duplikasi keahlian
  - [ ] Simpan ke `teknisi_kategori_layanan`
  - [ ] Hanya technician yang boleh akses

- [ ] **BE-36 [MVP] Technician hapus/nonaktifkan keahlian alat elektronik**
  - Endpoint: `DELETE /api/technicians/me/device-categories/{deviceCategoryId}`
  - [ ] Validasi relasi ditemukan
  - [ ] Hapus atau set `aktif = false`
  - [ ] Hanya pemilik profil technician yang boleh hapus

- [ ] **BE-37 [MVP] Technician lihat keahlian sendiri**
  - Endpoint: `GET /api/technicians/me/device-categories`
  - [ ] Return semua keahlian aktif milik technician login
  - [ ] Response menggunakan field English
  - [ ] Jangan tampilkan kategori yang nonaktif atau soft delete

---

## 4. Jadwal Technician

Target: technician bisa mengatur jam kerja.

- [ ] **BE-40 [MVP] Tambah jadwal kerja**
  - Endpoint: `POST /api/technicians/me/schedules`
  - [ ] Input `day`
  - [ ] Input `startTime`
  - [ ] Input `endTime`
  - [ ] Validasi `endTime` lebih besar dari `startTime`
  - [ ] Cegah jadwal duplikat

- [ ] **BE-41 [MVP] Lihat jadwal sendiri**
  - Endpoint: `GET /api/technicians/me/schedules`
  - [ ] Return semua jadwal aktif
  - [ ] Urutkan berdasarkan `day` dan `startTime`

- [ ] **BE-42 [NEXT] Update jadwal kerja**
  - Endpoint: `PUT /api/technicians/me/schedules/{scheduleId}`
  - [ ] Validasi jadwal milik technician tersebut
  - [ ] Bisa ubah `day`
  - [ ] Bisa ubah `startTime`
  - [ ] Bisa ubah `endTime`
  - [ ] Bisa ubah active/nonactive

- [ ] **BE-43 [MVP] Hapus/nonaktifkan jadwal kerja**
  - Endpoint: `DELETE /api/technicians/me/schedules/{scheduleId}`
  - [ ] Validasi jadwal milik technician tersebut
  - [ ] Soft delete/nonaktifkan jadwal

- [ ] **BE-44 [MVP] Validasi jadwal**
  - [ ] `endTime` harus lebih besar dari `startTime`
  - [ ] `day` harus sesuai enum
  - [ ] Tidak boleh bentrok dengan jadwal yang sama

- [ ] **BE-45 [NEXT] Service cek technician tersedia**
  - [ ] Cek status technician `ONLINE`
  - [ ] Cek keahlian device category
  - [ ] Cek jadwal kerja aktif
  - [ ] Dipakai saat menampilkan daftar technician dan request masuk

---

## 5. Service Request — Customer

Target: customer bisa membuat, melihat, dan membatalkan service request. Customer memilih technician terlebih dahulu, lalu memilih satu atau lebih alat elektronik yang dikuasai technician tersebut.

- [ ] **BE-50 [MVP] Customer membuat service request**
  - Endpoint: `POST /api/service-requests`
  - [ ] Validasi user role `CUSTOMER`
  - [ ] Validasi `technicianProfileId` ditemukan
  - [ ] Validasi akun technician aktif
  - [ ] Validasi `deviceCategoryIds` minimal 1
  - [ ] Validasi semua device category ditemukan
  - [ ] Validasi semua device category aktif dan belum soft delete
  - [ ] Validasi technician memiliki semua keahlian sesuai `deviceCategoryIds`
  - [ ] Simpan `technicianProfileId`
  - [ ] Simpan `deviceCategoryIds` ke tabel `permintaan_layanan_kategori`
  - [ ] Simpan `latitude` jika tersedia
  - [ ] Simpan `longitude` jika tersedia
  - [ ] Simpan `address`
  - [ ] Simpan `addressDetail`
  - [ ] Simpan `issueDescription`
  - [ ] Generate `requestCode`, contoh `REQ-178...`
  - [ ] Status awal `WAITING`

- [ ] **BE-51 [MVP] Simpan detail lokasi dan masalah**
  - [ ] `address` wajib
  - [ ] `issueDescription` wajib
  - [ ] `addressDetail` opsional
  - [ ] `latitude` opsional untuk MVP
  - [ ] `longitude` opsional untuk MVP
  - [ ] Jika latitude dikirim, validasi range -90 sampai 90
  - [ ] Jika longitude dikirim, validasi range -180 sampai 180

- [ ] **BE-52 [MVP] Tabel item alat elektronik pada service request**
  - Tabel: `permintaan_layanan_kategori`
  - [ ] Satu service request bisa punya banyak device category
  - [ ] Satu service request tetap hanya punya satu technician
  - [ ] Device category yang dipilih harus termasuk keahlian technician
  - [ ] Tidak boleh ada duplikasi category dalam satu request
  - [ ] Relasi menggunakan `id_permintaan`
  - [ ] Relasi menggunakan `id_kategori`

- [ ] **BE-53 [MVP] Customer lihat riwayat service request**
  - Endpoint: `GET /api/customers/service-requests`
  - [ ] Hanya tampilkan request milik user login
  - [ ] Bisa pagination
  - [ ] Bisa filter status opsional
  - [ ] Urutkan dari terbaru
  - [ ] Sertakan technician
  - [ ] Sertakan `selectedDeviceCategories`

- [ ] **BE-54 [MVP] Customer lihat detail service request**
  - Endpoint: `GET /api/service-requests/{serviceRequestId}`
  - [ ] Validasi request milik customer atau technician terkait
  - [ ] Return data customer
  - [ ] Return data technician
  - [ ] Return `selectedDeviceCategories`
  - [ ] Return address dan issueDescription
  - [ ] Return status
  - [ ] Return estimasi dan biaya akhir jika ada

- [ ] **BE-55 [MVP] Customer batalkan service request**
  - Endpoint: `PATCH /api/service-requests/{serviceRequestId}/cancel`
  - [ ] Hanya customer pemilik request
  - [ ] Hanya boleh jika status masih `WAITING` atau `ACCEPTED`
  - [ ] Simpan `cancelReason`
  - [ ] Update status `CANCELLED`
  - [ ] Isi `cancelledAt`
  - [ ] Catat riwayat status

- [ ] **BE-56 [NEXT] Simpan media request**
  - Endpoint: `POST /api/service-requests/{serviceRequestId}/media`
  - [ ] Validasi request milik customer
  - [ ] Simpan `fileUrl`
  - [ ] Simpan `fileType`
  - [ ] Simpan `mimeType`
  - [ ] Simpan `fileSize`

---

## 6. Service Request — Technician

Target: technician bisa melihat request masuk yang memang ditujukan kepadanya, lalu menerima, menolak, memulai, dan menyelesaikan layanan.

Catatan:
- Karena customer sudah memilih technician sebelum membuat request, technician tidak lagi mengambil request bebas dari kategori.
- Request masuk adalah request `WAITING` yang `technicianProfileId`-nya sama dengan technician login.

- [ ] **BE-60 [MVP] Technician lihat request masuk**
  - Endpoint: `GET /api/technicians/service-requests/incoming`
  - [ ] Hanya technician login
  - [ ] Tampilkan request `WAITING` yang ditujukan ke technician login
  - [ ] Sertakan data customer
  - [ ] Sertakan `selectedDeviceCategories`
  - [ ] Sertakan `issueDescription`
  - [ ] Bisa pagination
  - [ ] Urutkan dari terbaru

- [ ] **BE-61 [MVP] Technician lihat detail request**
  - Endpoint: `GET /api/technicians/service-requests/{serviceRequestId}`
  - [ ] Validasi technician adalah technician yang dipilih pada request
  - [ ] Return data customer
  - [ ] Return lokasi
  - [ ] Return `selectedDeviceCategories`
  - [ ] Return `issueDescription`
  - [ ] Return status

- [ ] **BE-62 [MVP] Technician accept request**
  - Endpoint: `PATCH /api/technicians/service-requests/{serviceRequestId}/accept`
  - [ ] Hanya technician login
  - [ ] Request harus `WAITING`
  - [ ] Request harus ditujukan ke technician login
  - [ ] Validasi technician masih memiliki semua keahlian untuk selectedDeviceCategories
  - [ ] Set status `ACCEPTED`
  - [ ] Isi `acceptedAt`
  - [ ] Set status technician `BUSY` jika perlu
  - [ ] Catat riwayat status
  - [ ] Buat notifikasi ke customer

- [ ] **BE-63 [MVP] Technician reject request**
  - Endpoint: `PATCH /api/technicians/service-requests/{serviceRequestId}/reject`
  - [ ] Request harus `WAITING`
  - [ ] Request harus ditujukan ke technician login
  - [ ] Simpan `rejectReason` jika ada
  - [ ] Status menjadi `REJECTED`
  - [ ] Tidak mengubah technician menjadi busy
  - [ ] Catat riwayat status
  - [ ] Buat notifikasi ke customer

- [ ] **BE-64 [MVP] Technician mulai pengerjaan**
  - Endpoint: `PATCH /api/technicians/service-requests/{serviceRequestId}/start`
  - [ ] Request harus `ACCEPTED`
  - [ ] Hanya technician yang dipilih pada request
  - [ ] Status menjadi `ON_PROGRESS`
  - [ ] Isi `processedAt`
  - [ ] Catat riwayat status
  - [ ] Buat notifikasi ke customer

- [ ] **BE-65 [MVP] Technician selesaikan pengerjaan**
  - Endpoint: `PATCH /api/technicians/service-requests/{serviceRequestId}/complete`
  - [ ] Request harus `ON_PROGRESS`
  - [ ] Hanya technician yang dipilih pada request
  - [ ] Status menjadi `COMPLETED`
  - [ ] Isi `completedAt`
  - [ ] Tambah `totalJobs`
  - [ ] Status technician kembali `ONLINE` jika perlu
  - [ ] Catat riwayat status
  - [ ] Buat notifikasi ke customer

- [ ] **BE-66 [NEXT] Technician isi estimasi biaya**
  - Endpoint: `PATCH /api/technicians/service-requests/{serviceRequestId}/estimate`
  - [ ] Hanya technician terkait
  - [ ] Estimasi biaya tidak boleh negatif
  - [ ] Simpan `estimatedCost`

- [ ] **BE-67 [MVP] Technician isi biaya akhir dan catatan**
  - Endpoint: `PATCH /api/technicians/service-requests/{serviceRequestId}/finalize`
  - [ ] Hanya technician terkait
  - [ ] Final cost tidak boleh negatif
  - [ ] Simpan `finalCost`
  - [ ] Simpan `technicianNote`

---

## 7. Status dan Riwayat Status

Target: semua perubahan status service request tercatat dan bisa dilihat sebagai timeline.

- [ ] **BE-70 [MVP] Lihat timeline status request**
  - Endpoint: `GET /api/service-requests/{serviceRequestId}/status-history`
  - [ ] Validasi user berhak melihat request
  - [ ] Return `previousStatus`
  - [ ] Return `newStatus`
  - [ ] Return `note`
  - [ ] Return `changedAt`
  - [ ] Return `changedBy`

- [ ] **BE-71 [MVP] Catat siapa pengubah status**
  - [ ] Simpan `lastChangedBy`
  - [ ] Simpan `changedBy` di riwayat status
  - [ ] Customer tercatat saat cancel
  - [ ] Technician tercatat saat accept/reject/start/complete

- [ ] **BE-72 [MVP] Validasi transisi status**
  - [ ] `WAITING` boleh ke `ACCEPTED`
  - [ ] `WAITING` boleh ke `REJECTED`
  - [ ] `WAITING` boleh ke `CANCELLED`
  - [ ] `ACCEPTED` boleh ke `ON_PROGRESS`
  - [ ] `ACCEPTED` boleh ke `CANCELLED`
  - [ ] `ON_PROGRESS` boleh ke `COMPLETED`
  - [ ] Status final tidak bisa diubah lagi

- [ ] **BE-73 [MVP] Simpan waktu status**
  - [ ] `acceptedAt` saat accepted
  - [ ] `processedAt` saat on progress
  - [ ] `completedAt` saat completed
  - [ ] `cancelledAt` saat cancelled

- [ ] **BE-74 [MVP] Blok perubahan status final**
  - [ ] `COMPLETED` tidak bisa diubah
  - [ ] `CANCELLED` tidak bisa diubah
  - [ ] `REJECTED` tidak bisa diubah

---

## 8. Chat REST

Target: customer dan technician terkait service request bisa berkomunikasi lewat REST API dulu sebelum WebSocket.

- [ ] **BE-80 [MVP] Kirim pesan text**
  - Endpoint: `POST /api/service-requests/{serviceRequestId}/messages`
  - [ ] Validasi user bagian dari request
  - [ ] Pesan text wajib punya `message`
  - [ ] Message type `TEXT`
  - [ ] Simpan sender
  - [ ] Simpan `sentAt`

- [ ] **BE-81 [MVP] Ambil riwayat chat**
  - Endpoint: `GET /api/service-requests/{serviceRequestId}/messages`
  - [ ] Validasi user bagian dari request
  - [ ] Urutkan dari pesan lama ke baru
  - [ ] Bisa pagination
  - [ ] Jangan tampilkan pesan yang soft deleted

- [ ] **BE-82 [NEXT] Kirim pesan gambar/file URL**
  - Endpoint: `POST /api/service-requests/{serviceRequestId}/messages/media`
  - [ ] Validasi `fileUrl`
  - [ ] Message type `IMAGE`
  - [ ] Simpan sender
  - [ ] Simpan `sentAt`

- [ ] **BE-83 [NEXT] Tandai pesan sudah dibaca**
  - Endpoint: `PATCH /api/messages/{messageId}/read`
  - [ ] Validasi penerima pesan
  - [ ] Isi `readAt`
  - [ ] Tidak perlu ubah kalau sudah dibaca

- [ ] **BE-84 [MVP] Validasi akses chat**
  - [ ] Customer hanya bisa chat pada request miliknya
  - [ ] Technician hanya bisa chat pada request yang ditujukan kepadanya
  - [ ] User lain tidak boleh akses chat request tersebut

---

## 9. Notifikasi

Target: user bisa melihat notifikasi perubahan status dan pesan baru.

- [ ] **BE-90 [MVP] List notifikasi user**
  - Endpoint: `GET /api/notifications`
  - [ ] Hanya notifikasi milik user login
  - [ ] Urutkan terbaru
  - [ ] Bisa pagination

- [ ] **BE-91 [MVP] Hitung notifikasi belum dibaca**
  - Endpoint: `GET /api/notifications/unread-count`
  - [ ] Hitung `readAt IS NULL`
  - [ ] Hanya milik user login

- [ ] **BE-92 [MVP] Tandai satu notifikasi dibaca**
  - Endpoint: `PATCH /api/notifications/{notificationId}/read`
  - [ ] Validasi notifikasi milik user
  - [ ] Isi `readAt`

- [ ] **BE-93 [NEXT] Tandai semua notifikasi dibaca**
  - Endpoint: `PATCH /api/notifications/read-all`
  - [ ] Update semua notifikasi user login
  - [ ] Hanya yang belum dibaca

- [ ] **BE-94 [MVP] Buat notifikasi saat status berubah**
  - [ ] Saat customer membuat request, technician dapat notifikasi
  - [ ] Saat technician menerima request, customer dapat notifikasi
  - [ ] Saat technician menolak request, customer dapat notifikasi
  - [ ] Saat technician mulai kerja, customer dapat notifikasi
  - [ ] Saat technician menyelesaikan request, customer dapat notifikasi
  - [ ] Saat customer membatalkan request, technician dapat notifikasi

- [ ] **BE-95 [NEXT] Buat notifikasi saat pesan baru**
  - [ ] Saat customer kirim pesan, technician dapat notifikasi
  - [ ] Saat technician kirim pesan, customer dapat notifikasi
  - [ ] Reference type `CHAT`

---

## 10. Review

Target: customer bisa memberi rating setelah service request selesai.

- [ ] **BE-100 [MVP] Customer membuat review**
  - Endpoint: `POST /api/service-requests/{serviceRequestId}/review`
  - [ ] Request harus milik customer
  - [ ] Request harus `COMPLETED`
  - [ ] Rating wajib 1 sampai 5
  - [ ] Comment opsional
  - [ ] Satu request hanya boleh satu review

- [ ] **BE-101 [MVP] Validasi request completed**
  - [ ] Kalau belum selesai, return error
  - [ ] Kalau cancelled/rejected, tidak boleh review

- [ ] **BE-102 [MVP] Validasi satu request satu review**
  - [ ] Cek review berdasarkan `serviceRequestId`
  - [ ] Jika sudah ada, return error

- [ ] **BE-103 [NEXT] Lihat review technician**
  - Endpoint: `GET /api/customers/technicians/{technicianProfileId}/reviews`
  - [ ] Return daftar review
  - [ ] Return nama customer jika boleh
  - [ ] Return rating
  - [ ] Return comment
  - [ ] Bisa pagination

- [ ] **BE-104 [MVP] Update rating rata-rata technician**
  - [ ] Hitung ulang `averageRating`
  - [ ] Update `averageRating`
  - [ ] Update `ratingCount`

- [ ] **BE-105 [MVP] Tampilkan rating di profil technician**
  - [ ] Include averageRating di endpoint detail technician
  - [ ] Include ratingCount
  - [ ] Include totalJobs

---

## 11. Log Aktivitas

Target: aktivitas penting tersimpan untuk audit.

- [ ] **BE-110 [PLUS] Log saat login**
  - [ ] Simpan `userId`
  - [ ] Simpan activity `LOGIN`
  - [ ] Simpan IP address jika tersedia
  - [ ] Simpan user agent jika tersedia

- [ ] **BE-111 [PLUS] Log saat buat request**
  - [ ] Simpan activity `CREATE_SERVICE_REQUEST`
  - [ ] Simpan metadata `serviceRequestId`
  - [ ] Simpan `selectedDeviceCategories`

- [ ] **BE-112 [PLUS] Log saat update status**
  - [ ] Simpan activity `UPDATE_SERVICE_REQUEST_STATUS`
  - [ ] Simpan `previousStatus`
  - [ ] Simpan `newStatus`

- [ ] **BE-113 [PLUS] Log saat logout**
  - [ ] Simpan activity `LOGOUT`
  - [ ] Simpan sessionId jika perlu

- [ ] **BE-114 [LATER] Endpoint lihat log untuk admin**
  - Endpoint: `GET /api/admin/activity-logs`
  - [ ] Hanya admin
  - [ ] Bisa filter tanggal
  - [ ] Bisa filter user
  - [ ] Bisa filter activity

---

## 12. WebSocket

Target: fitur real-time dikerjakan setelah REST API stabil.

- [ ] **BE-85 [LATER] WebSocket chat real-time**
  - Endpoint WS: `/ws/chat`
  - [ ] Setup WebSocket config
  - [ ] Setup STOMP jika digunakan
  - [ ] Buat room berdasarkan `serviceRequestId`
  - [ ] Broadcast pesan baru ke room request

- [ ] **BE-86 [LATER] Broadcast pesan baru**
  - [ ] Saat pesan tersimpan, kirim event ke WebSocket
  - [ ] Customer menerima pesan technician
  - [ ] Technician menerima pesan customer

- [ ] **BE-96 [LATER] WebSocket notifikasi**
  - Endpoint WS: `/ws/notification`
  - [ ] Kirim notifikasi status request
  - [ ] Kirim notifikasi pesan baru
  - [ ] Kirim unread count update

---

## 13. Admin Opsional

Catatan: role `ADMIN` ada di backend, tapi fokus utama masih customer dan technician. Kerjakan admin hanya kalau MVP sudah aman.

- [ ] **BE-120 [LATER] Admin CRUD kategori alat elektronik**
  - Endpoint: `/api/admin/device-categories`
  - [ ] Create device category
  - [ ] Update device category
  - [ ] Nonaktifkan device category
  - [ ] List semua device category termasuk nonaktif

- [ ] **BE-121 [LATER] Admin kelola keahlian technician**
  - Endpoint: `/api/admin/technicians/{technicianProfileId}/device-categories`
  - [ ] Tambah keahlian technician
  - [ ] Hapus/nonaktifkan keahlian technician
  - [ ] List keahlian technician
  - [ ] Validasi category aktif

- [ ] **BE-122 [LATER] Admin lihat semua user**
  - Endpoint: `GET /api/admin/users`
  - [ ] Filter role
  - [ ] Filter accountStatus
  - [ ] Search name/email

- [ ] **BE-123 [LATER] Admin nonaktifkan user**
  - Endpoint: `PATCH /api/admin/users/{userId}/disable`
  - [ ] Set accountStatus
  - [ ] Cegah user login jika inactive/banned

- [ ] **BE-124 [LATER] Admin lihat semua service request**
  - Endpoint: `GET /api/admin/service-requests`
  - [ ] Filter status
  - [ ] Filter tanggal
  - [ ] Filter technician
  - [ ] Filter customer
  - [ ] Filter device category

- [ ] **BE-125 [LATER] Admin dashboard sederhana**
  - Endpoint: `GET /api/admin/dashboard`
  - [ ] Total user
  - [ ] Total technician
  - [ ] Total service request
  - [ ] Total completed request
  - [ ] Total waiting request
  - [ ] Rating rata-rata technician

---

## Checklist Definisi Selesai per Fitur

Satu fitur dianggap selesai kalau:

- [ ] Endpoint bisa dipanggil via Postman/curl
- [ ] Request DTO sudah divalidasi
- [ ] Response sukses rapi
- [ ] Response error rapi
- [ ] Data tersimpan/terambil dari database dengan benar
- [ ] Role access sudah sesuai
- [ ] Data keluar/masuk API menggunakan English
- [ ] Tidak ada data sensitif bocor di response
- [ ] Minimal ada 1 test manual yang berhasil
- [ ] Commit Git sudah dibuat

---

## Catatan untuk API_CONTRACT.md

Dokumen API detail dibuat terpisah supaya README tetap fokus sebagai roadmap.

`API_CONTRACT.md` nanti berisi:
- daftar endpoint resmi
- HTTP method
- auth requirement
- request body
- response body
- contoh response sukses
- contoh response error
- aturan validasi
- status code
