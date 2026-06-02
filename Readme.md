# Teknisio — Full Project README

**Teknisio: Solusi Servis Anda** adalah sistem pemesanan jasa servis elektronik yang menghubungkan **customer** dengan **technician**. Project ini terdiri dari backend berbasis **Spring Boot REST API** dan frontend berbasis **Android Java**. Backend dirancang sebagai satu server utama yang dapat melayani banyak client, termasuk Android Mobile, JavaFX Desktop, atau client lain yang mengikuti kontrak REST API.

README ini menjadi dokumentasi utama project secara keseluruhan: status project, arsitektur, tech stack, cara setup backend, cara setup mobile, kontrak API, testing, troubleshooting, aturan Git, dan urutan pengembangan berikutnya.

---

## Status Singkat Project

```text
Status project saat ini: MVP core workflow stabilization
```

Backend sudah masuk fase stabilisasi core workflow. Android mobile sudah mulai menjadi client nyata yang terhubung ke backend melalui Retrofit, bukan lagi sekadar mockup UI.

### Backend yang sudah stabil

```text
✅ Auth dasar: register, login, profile
✅ Role-based access CUSTOMER / TECHNICIAN / ADMIN
✅ Device category public API
✅ Technician device category / skill API
✅ Customer technician discovery
✅ Customer service request create/list/detail/cancel
✅ Technician service request list/detail/accept/reject/start/complete
✅ Status transition divalidasi database trigger
✅ Status history dibuat otomatis database trigger
✅ Strict smoke test V5: 736 passed, 0 failed
```

### Backend yang sudah tersedia tetapi perlu dikunci lagi dengan test

```text
🟡 Service request status history read API
🟡 Customer create review API
🟡 Review schema, entity, repository, dan response
```

### Mobile yang sudah tersedia

```text
✅ Onboarding dan navigasi awal
✅ Login dan register customer/technician
✅ Penyimpanan token JWT lokal
✅ AuthInterceptor otomatis mengirim Bearer token
✅ Customer home load device categories
✅ Customer mencari technician berdasarkan device category
✅ Customer membuat service request
✅ Customer melihat history/detail request dan cancel request
✅ Technician melihat request masuk/detail request
✅ Technician accept/reject/start/complete request
✅ Technician mengelola device category / skill
✅ Notification screen client-side tersedia
```

### Fitur yang belum menjadi prioritas awal MVP

```text
⏳ Refresh token dan logout server-side
⏳ Chat REST/WebSocket
⏳ Notification backend
⏳ Admin management
⏳ Desktop JavaFX implementation detail
```

---

## Table of Contents

1. [Tujuan Project](#1-tujuan-project)
2. [Ruang Lingkup MVP](#2-ruang-lingkup-mvp)
3. [Arsitektur Sistem](#3-arsitektur-sistem)
4. [Tech Stack](#4-tech-stack)
5. [Struktur Repository](#5-struktur-repository)
6. [Aturan Kontrak API](#6-aturan-kontrak-api)
7. [Flow Utama Aplikasi](#7-flow-utama-aplikasi)
8. [Backend Setup](#8-backend-setup)
9. [Mobile Setup](#9-mobile-setup)
10. [Environment dan Base URL](#10-environment-dan-base-url)
11. [Database dan Flyway](#11-database-dan-flyway)
12. [Endpoint Summary](#12-endpoint-summary)
13. [Response Format](#13-response-format)
14. [Security Contract](#14-security-contract)
15. [Testing](#15-testing)
16. [Manual Test Flow](#16-manual-test-flow)
17. [Roadmap Terdekat](#17-roadmap-terdekat)
18. [Git Rules](#18-git-rules)
19. [Troubleshooting](#19-troubleshooting)
20. [Catatan Developer](#20-catatan-developer)
21. [Commit Convention](#21-commit-convention)

---

# 1. Tujuan Project

Teknisio dibuat untuk membantu customer memesan layanan servis elektronik secara online dan membantu technician mengelola permintaan layanan yang masuk.

Tujuan utama sistem:

- Customer dapat register dan login.
- Technician dapat register dan login.
- Customer dapat memilih kategori alat elektronik.
- Customer dapat melihat daftar technician yang mendukung kategori tersebut.
- Customer dapat membuat service request kepada technician tertentu.
- Technician dapat menerima, menolak, memulai, dan menyelesaikan service request.
- Sistem menyimpan status request dan riwayat status.
- Customer dapat melihat riwayat layanan.
- Customer dapat memberi review setelah layanan selesai.
- Sistem siap dikembangkan ke chat, notification, dan admin management.

---

# 2. Ruang Lingkup MVP

## 2.1 Yang masuk MVP stabil

```text
Auth
Device category
Technician skill
Technician discovery
Customer service request
Technician workflow
Status transition
Status history database trigger
Review dasar
```

## 2.2 Yang tidak dipaksakan di MVP awal

```text
GPS wajib
Upload foto kerusakan
Payment gateway
Chat real-time
Notification real-time
Admin dashboard lengkap
Refresh token server-side
Multi-client production deployment
```

## 2.3 Keputusan penting MVP

Untuk MVP saat ini, customer **tidak dipaksa memakai GPS**. Customer cukup mengisi alamat manual melalui field:

```text
address
addressDetail
```

Customer juga tidak memilih detail layanan seperti `AC Cleaning`, `AC Repair`, atau `Refrigerator Freon Refill`. Customer memilih satu atau lebih kategori alat elektronik melalui:

```text
deviceCategoryIds
```

Lalu menjelaskan masalah melalui:

```text
issueDescription
```

---

# 3. Arsitektur Sistem

Teknisio memakai arsitektur **client-server**.

```text
Android Mobile / JavaFX Desktop / Client lain
                    ↓
              REST API JSON
                    ↓
          Spring Boot Backend
                    ↓
            PostgreSQL Database
```

## 3.1 Backend architecture

Backend memakai pendekatan:

```text
Spring MVC + Layered Architecture
```

Alur request backend:

```text
Client
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Entity
  ↓
PostgreSQL
```

Tanggung jawab layer:

| Layer | Tanggung Jawab |
|---|---|
| Controller | Menerima request, menjalankan validasi DTO, mengembalikan response |
| Service | Business logic, validasi ownership, validasi status, mapping response |
| Repository | Query database menggunakan Spring Data JPA |
| Entity | Mapping tabel database |
| DTO Request | Kontrak input dari frontend |
| DTO Response | Kontrak output ke frontend |
| Security | JWT, authentication, authorization, current user |
| Common | Response wrapper, exception, utility |

## 3.2 Mobile architecture

Mobile Android memakai struktur berbasis layer sederhana:

```text
Activity / View
  ↓
Controller / helper logic
  ↓
Retrofit ApiService
  ↓
Backend REST API
```

Komponen penting mobile:

| Komponen | Fungsi |
|---|---|
| Activity | UI dan flow layar |
| ApiService | Daftar endpoint Retrofit |
| ApiClient | Konfigurasi Retrofit, OkHttp, Gson |
| AuthInterceptor | Menambahkan Authorization Bearer token |
| TokenManager | Menyimpan token dan data user lokal |
| Request model | Body yang dikirim ke backend |
| Response model | Data response dari backend |
| ErrorParser | Membaca error dari `ApiResponse` backend |
| AppToast | Menampilkan pesan sukses/error di UI |

---

# 4. Tech Stack

## 4.1 Backend

| Komponen | Teknologi |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.14 |
| Build Tool | Gradle Kotlin DSL |
| Database | PostgreSQL 16 |
| Migration | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Token | JWT / JJWT 0.12.6 |
| Validation | Spring Validation / Jakarta Validation |
| Monitoring | Spring Boot Actuator |
| Realtime planned | Spring WebSocket |
| Local database | Docker Compose |
| Boilerplate helper | Lombok |

## 4.2 Android Mobile

| Komponen | Teknologi |
|---|---|
| Language | Java |
| Platform | Android |
| Build Tool | Gradle Kotlin DSL |
| Namespace | `com.teknisio.mobile` |
| Application ID | `com.teknisio.mobile` |
| Compile SDK | 36 |
| Target SDK | 36 |
| Min SDK | 23 |
| App Version | `0.9.0` |
| UI | XML Layout + Android Activity |
| Networking | Retrofit 2.11.0 |
| JSON Converter | Gson Converter 2.11.0 |
| HTTP Client | OkHttp |
| HTTP Logging | OkHttp Logging Interceptor 4.12.0 |
| UI Library | AppCompat, Material, ConstraintLayout |
| Local Session | SharedPreferences via TokenManager |

## 4.3 Planned / optional frontend

| Client | Status |
|---|---|
| Android Mobile | Current implemented frontend |
| JavaFX Desktop | Planned / optional client using same REST API |
| Web Admin | Deferred |

---

# 5. Struktur Repository

Jika backend dan mobile berada dalam satu repository, struktur yang disarankan:

```text
teknisio/
├── README.md
├── docs/
│   ├── README_BACKEND.md
│   ├── README_MOBILE.md
│   ├── ROADMAP_BACKEND.md
│   └── ROADMAP_MOBILE.md
├── backend/
│   ├── build.gradle.kts
│   ├── docker-compose.yml
│   ├── .env.example
│   ├── develop/
│   │   └── api-smoke-test.sh
│   └── src/
└── mobile/
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── app/
```

Jika repository masih terpisah, README ini tetap berlaku sebagai dokumentasi gabungan. Jalankan perintah backend di folder backend dan perintah mobile di folder mobile.

## 5.1 Struktur backend saat ini

```text
src/main/java/com/teknisio/
├── TeknisioBackendApplication.java
├── common/
│   ├── exception/
│   ├── response/
│   └── util/
├── config/
├── controllers/
├── dto/
│   ├── requests/
│   └── responses/
├── model/
│   ├── entities/
│   ├── entities/base/
│   └── enums/
├── repositories/
├── security/
├── services/
└── websocket/
```

Resource backend:

```text
src/main/resources/
├── application.yml
├── db/
│   ├── migration/
│   └── develop/
├── static/
└── templates/
```

## 5.2 Struktur mobile saat ini

```text
app/src/main/java/com/teknisio/mobile/
├── base/
├── controller/
├── local/
├── model/
│   ├── request/
│   └── response/
├── network/
├── util/
└── view/
    ├── auth/
    ├── customer/
    ├── onboarding/
    └── technician/
```

---

# 6. Aturan Kontrak API

Aturan kontrak API Teknisio:

- Backend hanya satu untuk semua client.
- Jangan membuat endpoint khusus Android atau khusus Desktop.
- Endpoint resmi memakai bahasa Inggris.
- Field request dan response API memakai bahasa Inggris.
- Nama internal Java/database boleh tetap bahasa Indonesia jika sudah ada.
- Jangan expose entity JPA langsung ke response.
- Gunakan DTO request dan DTO response.
- Semua response memakai `ApiResponse<T>`.
- Semua error melewati global exception handler atau security handler.
- Mobile wajib mengikuti response wrapper backend.
- Mobile tidak membuat format response sendiri di luar kontrak backend.
- Untuk MVP, customer memilih `deviceCategoryIds`, bukan `jenisLayananId`.
- Untuk MVP, customer mengisi `issueDescription`, `address`, dan optional `addressDetail`.

Mapping istilah:

| Konsep | Nama API | Nama Internal Backend |
|---|---|---|
| User | `user` | `User`, `users` |
| Customer | `customer` | `User` role `CUSTOMER` |
| Technician | `technician` | `User` role `TECHNICIAN`, `TeknisiProfile` |
| Kategori alat elektronik | `deviceCategory` | `KategoriLayanan` |
| Keahlian technician | `technicianDeviceCategory` | `TeknisiKategoriLayanan` |
| Permintaan layanan | `serviceRequest` | `PermintaanLayanan` |
| Kategori terpilih | `selectedDeviceCategories` | `PermintaanLayananKategori` |
| Deskripsi masalah | `issueDescription` | `deskripsiMasalah` |
| Alamat | `address` | `alamat` |
| Detail alamat | `addressDetail` | `detailAlamat` |
| Riwayat status | `statusHistory` | `RiwayatStatus` |
| Catatan technician | `technicianNote` | `catatanTeknisi` |
| Alasan batal | `cancelReason` | `alasanBatal` |
| Alasan tolak | `rejectReason` | `alasanTolak` |

---

# 7. Flow Utama Aplikasi

## 7.1 Flow customer

```text
Customer buka aplikasi
↓
Register / login
↓
Masuk customer home
↓
Melihat daftar device category
↓
Memilih device category, misalnya Air Conditioner
↓
Melihat daftar technician yang mendukung kategori tersebut
↓
Filter/sort technician jika diperlukan
↓
Membuka detail technician
↓
Memilih satu atau lebih supported device categories
↓
Mengisi issueDescription, address, addressDetail
↓
Membuat service request
↓
Status awal WAITING
↓
Melihat riwayat/detail request
↓
Dapat cancel request selama status masih dapat dibatalkan
↓
Setelah COMPLETED, customer dapat memberi review
```

## 7.2 Flow technician

```text
Technician buka aplikasi
↓
Register / login
↓
Masuk technician home
↓
Mengatur skill / device categories yang dikuasai
↓
Melihat daftar service request masuk
↓
Membuka detail request
↓
Accept atau reject request
↓
Jika accepted, start work
↓
Setelah selesai, complete work
↓
Status request berubah dan status history tercatat
```

## 7.3 Flow status request

```text
WAITING
  ├── ACCEPTED
  │     ├── ON_PROGRESS
  │     │     ├── COMPLETED
  │     │     └── CANCELLED
  │     └── CANCELLED
  ├── REJECTED
  └── CANCELLED
```

Status final:

```text
COMPLETED
CANCELLED
REJECTED
```

Status final tidak boleh diubah lagi.

---

# 8. Backend Setup

## 8.1 Tools yang dibutuhkan

Pastikan sudah terinstall:

- Java 17
- Docker
- Docker Compose
- Git
- IDE seperti IntelliJ IDEA atau VS Code

Cek Java:

```bash
java -version
```

Cek Docker:

```bash
docker --version
docker compose version
```

## 8.2 Clone repository

Jika repository backend terpisah:

```bash
git clone <url-backend-repository>
cd teknisio-backend
```

Jika monorepo:

```bash
git clone <url-project-repository>
cd teknisio/backend
```

## 8.3 Buat file `.env`

```bash
cp .env.example .env
```

Contoh `.env.example`:

```env
APP_NAME=teknisio-backend

POSTGRES_HOST=localhost
POSTGRES_PORT=5433
POSTGRES_DB=teknisio_db
POSTGRES_USER=teknisio_user
POSTGRES_PASSWORD=change_this_password

SERVER_PORT=8080

JWT_SECRET=change_this_secret_minimum_32_characters
JWT_EXPIRATION_MS=86400000

CORS_ALLOWED_ORIGINS=http://localhost:3000
```

Catatan:

- Jangan commit `.env`.
- Commit hanya `.env.example`.
- `JWT_SECRET` boleh dibuat random production-like, tetapi generate sekali saja dan simpan di `.env`.
- Jangan generate JWT secret baru setiap aplikasi start.
- `JWT_EXPIRATION_MS=86400000` berarti token berlaku 24 jam.

Generate JWT secret:

```bash
openssl rand -base64 64
```

## 8.4 Jalankan PostgreSQL

```bash
docker compose up -d
```

Cek container:

```bash
docker ps
```

Container yang diharapkan:

```text
teknisio-postgres
```

## 8.5 Jalankan backend

Linux/macOS:

```bash
./gradlew bootRun
```

Windows:

```bash
gradlew.bat bootRun
```

Backend berjalan di:

```text
http://localhost:8080
```

## 8.6 Cek health backend

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

## 8.7 Build backend sebelum commit

```bash
./gradlew clean build
```

## 8.8 Jalankan strict regression test

```bash
bash develop/api-smoke-test.sh
```

Target terakhir yang pernah dicapai:

```text
ALL STRICT API SMOKE TESTS V5 PASSED
Passed: 736
Failed: 0
```

---

# 9. Mobile Setup

## 9.1 Tools yang dibutuhkan

Pastikan sudah terinstall:

- Android Studio
- JDK yang kompatibel dengan Android Gradle Plugin
- Android SDK
- Emulator Android atau HP fisik
- Git

## 9.2 Clone repository mobile

Jika repository mobile terpisah:

```bash
git clone <url-mobile-repository>
cd teknisio-mobile
```

Jika monorepo:

```bash
git clone <url-project-repository>
cd teknisio/mobile
```

## 9.3 Buka project di Android Studio

Langkah:

1. Buka Android Studio.
2. Pilih `Open`.
3. Pilih folder project mobile.
4. Tunggu Gradle sync selesai.
5. Pastikan module `app` terbaca.

## 9.4 Build mobile

Melalui Android Studio:

```text
Build > Make Project
```

Atau lewat terminal:

```bash
./gradlew assembleDebug
```

Windows:

```bash
gradlew.bat assembleDebug
```

## 9.5 Jalankan mobile

Pilih salah satu:

```text
Emulator Android
HP fisik via USB debugging
```

Lalu klik tombol Run di Android Studio.

## 9.6 Internet permission

Mobile sudah membutuhkan internet karena berkomunikasi dengan backend melalui REST API.

Pastikan `AndroidManifest.xml` punya:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

# 10. Environment dan Base URL

## 10.1 Backend local URL

Backend lokal default:

```text
http://localhost:8080
```

## 10.2 Mobile ke backend Railway

Mobile saat ini dapat diarahkan ke deployment Railway:

```java
public static final String BASE_URL = "https://steadfast-liberation-production-e36b.up.railway.app/";
```

## 10.3 Mobile emulator ke backend lokal

Jika backend berjalan di laptop dan mobile dijalankan di Android Emulator, gunakan:

```java
public static final String BASE_URL = "http://10.0.2.2:8080/";
```

`10.0.2.2` adalah alamat khusus emulator Android untuk mengakses `localhost` laptop.

## 10.4 Mobile HP fisik ke backend lokal

Jika mobile dijalankan di HP fisik, gunakan IP laptop yang satu jaringan Wi-Fi dengan HP.

Contoh:

```java
public static final String BASE_URL = "http://192.168.1.10:8080/";
```

Cek IP laptop:

Linux:

```bash
ip addr
```

Windows:

```bash
ipconfig
```

Pastikan firewall tidak memblokir port `8080`.

## 10.5 Rekomendasi technical debt Base URL

Saat ini Base URL masih hardcoded di `Constants.BASE_URL`. Untuk jangka panjang, lebih rapi memakai build config:

```text
debug local emulator -> http://10.0.2.2:8080/
debug physical phone -> http://IP-LAPTOP:8080/
release              -> Railway / production URL
```

---

# 11. Database dan Flyway

Backend memakai Flyway untuk database migration.

Lokasi migration:

```text
src/main/resources/db/migration
```

Migration aktif:

```text
V1__create_enums.sql
V2__create_core_tables.sql
V3__create_indexes.sql
V4__create_triggers.sql
V5__seed_device_categories.sql
V6__create_reviews.sql
```

Fungsi migration:

| File | Fungsi |
|---|---|
| `V1__create_enums.sql` | Membuat extension dan enum PostgreSQL |
| `V2__create_core_tables.sql` | Membuat tabel utama dan relasi FK |
| `V3__create_indexes.sql` | Membuat index untuk query utama |
| `V4__create_triggers.sql` | Membuat trigger updated_at, validasi status flow, dan status history |
| `V5__seed_device_categories.sql` | Seed kategori alat elektronik default |
| `V6__create_reviews.sql` | Membuat tabel review |

Aturan penting:

- Jangan edit migration lama yang sudah pernah dijalankan di database tim.
- Jika perlu perubahan schema, buat migration baru.
- Jangan masukkan `db/develop` ke lokasi Flyway normal.
- Script di `db/develop` hanya untuk cleanup/reset development lokal.

Cek history Flyway:

```bash
docker exec -it teknisio-postgres psql -U teknisio_user -d teknisio_db
```

Lalu:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Cek tabel:

```sql
\dt
```

Reset database lokal development:

```bash
docker compose down -v
docker compose up -d
./gradlew bootRun
```

---

# 12. Endpoint Summary

## 12.1 Public endpoints

```text
POST /api/auth/register/customer
POST /api/auth/register/technician
POST /api/auth/login
GET  /api/device-categories
GET  /api/device-categories/{deviceCategoryId}
GET  /actuator/health
GET  /actuator/info
```

## 12.2 Authenticated endpoint

```text
GET /api/auth/profile
```

## 12.3 Technician device category endpoints

```text
POST   /api/technicians/device-categories
GET    /api/technicians/device-categories
DELETE /api/technicians/device-categories/{deviceCategoryId}
```

## 12.4 Customer technician discovery endpoints

```text
GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}
GET /api/customers/technicians/{technicianProfileId}
```

Optional query:

```text
availabilityStatus=ONLINE|OFFLINE|BUSY|ON_LEAVE
sort=rating|totalJobs|name
```

## 12.5 Customer service request endpoints

```text
POST  /api/customers/service-requests
GET   /api/customers/service-requests
GET   /api/customers/service-requests/{serviceRequestId}
GET   /api/customers/service-requests/{serviceRequestId}/status-history
PATCH /api/customers/service-requests/{serviceRequestId}/cancel
POST  /api/customers/service-requests/{serviceRequestId}/review
```

Catatan:

- `status-history` sudah tersedia di backend, tetapi perlu strict smoke test lanjutan.
- `review` sudah tersedia di backend, tetapi perlu strict smoke test lanjutan.
- Mobile belum sepenuhnya memakai status-history dan review.

## 12.6 Technician service request endpoints

```text
GET   /api/technicians/service-requests
GET   /api/technicians/service-requests/{serviceRequestId}
GET   /api/technicians/service-requests/{serviceRequestId}/status-history
PATCH /api/technicians/service-requests/{serviceRequestId}/accept
PATCH /api/technicians/service-requests/{serviceRequestId}/reject
PATCH /api/technicians/service-requests/{serviceRequestId}/start
PATCH /api/technicians/service-requests/{serviceRequestId}/complete
```

Optional query:

```text
status=WAITING|ACCEPTED|ON_PROGRESS|COMPLETED|CANCELLED|REJECTED
sort=latest|oldest
```

## 12.7 Next endpoints

```text
PUT   /api/users/me
PATCH /api/technicians/availability
PUT   /api/technicians/profile
GET   /api/customers/technicians/{technicianProfileId}/reviews
```

## 12.8 Deferred endpoints

```text
POST  /api/auth/refresh
POST  /api/auth/logout
POST  /api/service-requests/{serviceRequestId}/messages
GET   /api/service-requests/{serviceRequestId}/messages
GET   /api/notifications
PATCH /api/notifications/{notificationId}/read
GET   /api/admin/users
GET   /api/admin/service-requests
```

---

# 13. Response Format

Semua response backend memakai wrapper:

```java
ApiResponse<T>
```

Mobile juga memiliki model response yang mengikuti format ini.

## 13.1 Success response

```json
{
  "success": true,
  "message": "Success message",
  "data": {},
  "errors": {}
}
```

## 13.2 Error response

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "errors": {}
}
```

## 13.3 Validation error

```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": {
    "email": "Email is required",
    "password": "Password is required"
  }
}
```

Catatan:

- `success=true` hanya untuk HTTP 2xx.
- `success=false` untuk HTTP 4xx dan 5xx.
- `errors` digunakan terutama untuk validation error.
- Mobile harus memakai `message` dari backend jika error terjadi.

---

# 14. Security Contract

## 14.1 Public

Endpoint berikut tidak butuh token:

```text
POST /api/auth/register/customer
POST /api/auth/register/technician
POST /api/auth/login
GET  /api/device-categories
GET  /api/device-categories/{deviceCategoryId}
GET  /actuator/health
GET  /actuator/info
```

## 14.2 Authenticated

Endpoint berikut butuh token valid:

```text
GET /api/auth/profile
```

## 14.3 Role based

```text
/api/customers/**    -> role CUSTOMER
/api/technicians/**  -> role TECHNICIAN
/api/admin/**        -> role ADMIN
```

Expected error:

| Kondisi | HTTP | Message |
|---|---:|---|
| Tanpa token | 401 | `Unauthorized` |
| Token role salah | 403 | `Forbidden` |
| Token invalid | 401 | `Unauthorized` |

## 14.4 Mobile session flow

```text
Login/Register success
↓
Backend return accessToken dan user
↓
Mobile simpan token dan user info ke TokenManager
↓
ApiClient reset
↓
Request berikutnya lewat AuthInterceptor
↓
AuthInterceptor menambahkan Authorization: Bearer {token}
```

Catatan:

- Untuk MVP saat ini, logout client cukup menghapus token lokal.
- Refresh token server-side ditunda.
- Jika backend mengaktifkan refresh/logout server-side nanti, mobile perlu update `ApiService`, DTO, dan flow session.

---

# 15. Testing

## 15.1 Backend unit/integration test

```bash
./gradlew test
```

## 15.2 Backend build validation

```bash
./gradlew clean build
```

## 15.3 Backend health check

```bash
curl http://localhost:8080/actuator/health
```

## 15.4 Backend strict smoke test

```bash
bash develop/api-smoke-test.sh
```

Target terakhir:

```text
ALL STRICT API SMOKE TESTS V5 PASSED
Passed: 736
Failed: 0
```

Setiap endpoint backend baru wajib dites minimal:

- Success case.
- Tanpa token jika protected.
- Wrong role jika role-based.
- Invalid UUID jika memakai UUID.
- Not found jika data tidak ada.
- Ownership protection jika data bukan milik user login.
- Validation error jika body tidak valid.
- Invalid enum jika memakai enum.
- Invalid status transition jika update status.
- Response sukses memakai `ApiResponse`.
- Response error memakai `ApiResponse`.

## 15.5 Mobile build test

```bash
./gradlew assembleDebug
```

## 15.6 Mobile manual API integration test

Minimal test mobile:

- Login customer.
- Login technician.
- Register customer.
- Register technician.
- Customer load device categories.
- Customer cari technician berdasarkan kategori.
- Customer buat order.
- Technician melihat order masuk.
- Technician accept order.
- Technician start order.
- Technician complete order.
- Customer melihat detail order completed.
- Customer cancel order saat status masih valid.
- Wrong role tidak bisa masuk layar role lain.

---

# 16. Manual Test Flow

## 16.1 Test backend cepat

```bash
docker compose up -d
./gradlew bootRun
curl http://localhost:8080/actuator/health
bash develop/api-smoke-test.sh
```

## 16.2 Test customer mobile

```text
1. Buka aplikasi mobile.
2. Register customer baru.
3. Login customer.
4. Pastikan masuk ke CustomerHomeActivity.
5. Pastikan device categories tampil.
6. Pilih salah satu device category.
7. Pastikan daftar technician tampil.
8. Buka detail technician.
9. Pilih supported device categories.
10. Isi issueDescription.
11. Isi address dan addressDetail.
12. Submit service request.
13. Pastikan service request berhasil dibuat dengan status WAITING.
14. Buka riwayat order.
15. Buka detail order.
16. Coba cancel jika masih WAITING / ACCEPTED / ON_PROGRESS.
```

## 16.3 Test technician mobile

```text
1. Register technician baru.
2. Login technician.
3. Pastikan masuk ke TechnicianHomeActivity.
4. Tambahkan skill device category.
5. Pastikan skill muncul di list.
6. Tunggu customer membuat request ke technician tersebut.
7. Buka request list.
8. Buka detail request.
9. Accept request.
10. Start work.
11. Complete work.
12. Pastikan status berubah sesuai flow.
```

## 16.4 Test integrasi dua device

```text
1. Device A login sebagai customer.
2. Device B login sebagai technician.
3. Customer membuat request ke technician.
4. Technician refresh request list.
5. Technician accept/start/complete.
6. Customer refresh detail/history.
7. Pastikan status sinkron.
```

---

# 17. Roadmap Terdekat

## 17.1 Backend next priority

```text
1. Tambah strict smoke test untuk status-history
2. Tambah strict smoke test untuk create review
3. Buat/list review technician
4. Technician availability update
5. Technician profile update
6. Chat REST send message
7. Chat REST history
8. WebSocket chat
9. Notification backend
10. Admin management
```

## 17.2 Mobile next priority

```text
1. Tambah status history timeline di detail order
2. Tambah review setelah order COMPLETED
3. Tambah list review di detail technician
4. Tambah technician availability update
5. Sinkronisasi update profile
6. Rapikan Base URL debug/release
7. Notification setelah backend endpoint stabil
8. Chat REST setelah backend siap
9. WebSocket chat setelah REST chat stabil
```

## 17.3 Technical debt penting

```text
- Jangan hardcode Base URL untuk semua environment.
- Jangan paksa fitur notification sebelum backend notification siap.
- Jangan mulai WebSocket sebelum REST chat dan status-history stabil.
- Pastikan semua endpoint baru masuk strict smoke test backend.
- Pastikan semua perubahan kontrak API langsung disesuaikan di model mobile.
```

---

# 18. Git Rules

## 18.1 Jangan commit

```text
.env
.env.*
!.env.example
build/
.gradle/
.idea/
.vscode/
*.log
*.session.sql
application-local.yml
application-local.properties
local.properties
captures/
.externalNativeBuild/
.cxx/
```

## 18.2 Boleh dan perlu commit backend

```text
.env.example
docker-compose.yml
build.gradle.kts
settings.gradle.kts
src/main/resources/application.yml
src/main/resources/db/migration/*.sql
src/main/java/**
src/test/java/**
develop/api-smoke-test.sh
README.md
ROADMAP.md
```

## 18.3 Boleh dan perlu commit mobile

```text
settings.gradle.kts
build.gradle.kts
app/build.gradle.kts
app/src/main/**
app/src/test/**
app/src/androidTest/**
proguard-rules.pro
README.md
ROADMAP.md
```

Catatan penting:

- Jangan ignore semua `*.sh`, karena script test backend perlu di-commit.
- Jangan ignore semua `*.txt`, karena beberapa dokumentasi atau sample bisa saja perlu di-commit.
- Jangan commit `.env` atau `local.properties`.
- Jangan commit file build output.

---

# 19. Troubleshooting

## 19.1 Backend: Docker permission denied

Error:

```text
permission denied while trying to connect to the Docker daemon socket
```

Solusi:

```bash
sudo usermod -aG docker $USER
```

Lalu logout dan login ulang.

## 19.2 Backend: Port PostgreSQL bentrok

Cek port:

```bash
sudo lsof -i :5432
sudo lsof -i :5433
```

Solusi cepat: ubah `POSTGRES_PORT` di `.env`.

```env
POSTGRES_PORT=5433
```

Restart container:

```bash
docker compose down
docker compose up -d
```

## 19.3 Backend: Credential database salah

Error:

```text
password authentication failed for user "teknisio_user"
```

Solusi:

- Pastikan `.env` sama dengan `application.yml`.
- Jika volume lama masih memakai password lama, reset volume lokal.

```bash
docker compose down -v
docker compose up -d
./gradlew bootRun
```

## 19.4 Backend: Flyway checksum mismatch

Error:

```text
Validate failed: Migration checksum mismatch
```

Penyebab:

- Migration lama yang sudah pernah dijalankan berubah.

Solusi development lokal:

```bash
docker compose down -v
docker compose up -d
./gradlew bootRun
```

Solusi tim:

- Jangan edit migration lama.
- Buat migration baru.

## 19.5 Backend: Hibernate validate error

Error:

```text
Schema-validation: wrong column type encountered
```

Cek:

- Nama tabel.
- Nama kolom.
- Tipe data.
- Enum PostgreSQL.
- `columnDefinition` pada entity enum.
- Migration sudah jalan atau belum.

## 19.6 Backend: protected API return 403 padahal sudah login

Cek role token:

```text
CUSTOMER hanya boleh /api/customers/**
TECHNICIAN hanya boleh /api/technicians/**
ADMIN hanya boleh /api/admin/**
```

## 19.7 Mobile: tidak bisa konek ke backend lokal dari emulator

Jangan pakai:

```text
http://localhost:8080/
```

Untuk emulator Android, pakai:

```text
http://10.0.2.2:8080/
```

## 19.8 Mobile: tidak bisa konek dari HP fisik

Cek:

- HP dan laptop satu Wi-Fi.
- Backend berjalan di `0.0.0.0` atau bisa diakses dari jaringan lokal.
- Firewall laptop tidak memblokir port `8080`.
- `BASE_URL` memakai IP laptop, bukan `localhost`.

Contoh:

```java
public static final String BASE_URL = "http://192.168.1.10:8080/";
```

## 19.9 Mobile: request protected selalu Unauthorized

Cek:

- Login benar-benar sukses.
- Token tersimpan di `TokenManager`.
- `AuthInterceptor` aktif di `ApiClient`.
- `ApiClient.reset()` dipanggil setelah login/register/logout.
- Header dikirim sebagai:

```text
Authorization: Bearer {token}
```

## 19.10 Mobile: role salah masuk halaman

Cek data user yang disimpan:

```text
role = CUSTOMER atau TECHNICIAN
```

Customer harus diarahkan ke customer screen. Technician harus diarahkan ke technician screen.

---

# 20. Catatan Developer

- Backend harus dijalankan sebelum mobile menggunakan backend lokal.
- Jangan hardcode credential database di source code.
- Jangan commit `.env`.
- Jangan commit `local.properties` Android.
- Semua field API harus English.
- Entity dan tabel internal boleh tetap memakai bahasa Indonesia.
- Jangan expose entity langsung ke response.
- Mobile harus mengikuti DTO backend.
- Jika response backend berubah, update model response mobile.
- Jika request body backend berubah, update model request mobile.
- Status history jangan di-insert manual dari Java service; database trigger sudah menangani.
- Jangan pakai `JenisLayanan` untuk flow MVP stable.
- Customer memilih `deviceCategoryIds`, bukan detail servis.
- Setiap fitur backend baru harus masuk strict smoke test.
- Setiap fitur mobile baru harus dites minimal dengan customer dan technician.
- Commit per phase supaya mudah rollback.

---

# 21. Commit Convention

## 21.1 Backend feature

```bash
git add .
git commit -m "phase backend add technician availability endpoint"
```

## 21.2 Backend test

```bash
git add develop/api-smoke-test.sh
git commit -m "test add status history smoke test"
```

## 21.3 Mobile feature

```bash
git add .
git commit -m "phase mobile add status history timeline"
```

## 21.4 Documentation

```bash
git add README.md ROADMAP.md
git commit -m "docs update full project readme"
```

---

# 22. Quick Command Reference

## Backend

```bash
# Start database
docker compose up -d

# Run backend
./gradlew bootRun

# Build backend
./gradlew clean build

# Health check
curl http://localhost:8080/actuator/health

# Strict smoke test
bash develop/api-smoke-test.sh
```

## Mobile

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Android Studio
Open project -> Sync Gradle -> Run app
```

---

# 23. Definisi Done

Sebuah fitur dianggap selesai jika memenuhi syarat berikut:

## Backend Definition of Done

```text
✅ Endpoint tersedia
✅ DTO request/response tersedia
✅ Validation tersedia
✅ Role-based access benar
✅ Ownership validation benar
✅ Error handling konsisten
✅ Response memakai ApiResponse
✅ Entity tidak diexpose langsung
✅ Build sukses
✅ Strict smoke test ditambahkan
✅ Dokumentasi endpoint diperbarui
```

## Mobile Definition of Done

```text
✅ UI flow tersedia
✅ Request model sesuai backend
✅ Response model sesuai backend
✅ ErrorParser dipakai untuk error backend
✅ Loading state ditampilkan
✅ Empty state ditampilkan jika data kosong
✅ Token otomatis dikirim jika endpoint protected
✅ Wrong role diarahkan dengan benar
✅ Manual test customer berhasil
✅ Manual test technician berhasil jika terkait technician
✅ Dokumentasi mobile diperbarui
```

---

# 24. Ringkasan Status Akhir

```text
Backend : core MVP service request workflow sudah kuat
Mobile  : core customer/technician workflow sudah terintegrasi
Next    : status history timeline + review + availability
Later   : notification + chat + admin
```

Project sudah berada pada tahap **integrasi dan stabilisasi MVP**, bukan lagi tahap inisialisasi project.
