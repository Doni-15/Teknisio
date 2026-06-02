# Teknisio Mobile

Android client untuk **Teknisio: Solusi Servis Anda**.

Aplikasi mobile ini digunakan oleh dua role utama:

- **Customer**, untuk registrasi/login, memilih kategori alat elektronik, mencari technician, membuat service request, melihat riwayat request, melihat detail request, dan membatalkan request.
- **Technician**, untuk registrasi/login, mengatur keahlian alat elektronik, melihat request masuk, menerima/menolak request, memulai pengerjaan, dan menyelesaikan pekerjaan.

Mobile Teknisio berkomunikasi dengan backend melalui **HTTP REST API** dengan format **JSON**. Semua kontrak request dan response mengikuti backend Teknisio, terutama field berbahasa Inggris seperti `deviceCategoryId`, `technicianProfileId`, `serviceRequestId`, `issueDescription`, `address`, `addressDetail`, dan `status`.

---

## Status Singkat Project

```text
Status mobile saat ini: MVP core workflow integration
```

Yang sudah tersedia di mobile:

```text
✅ Onboarding dan routing awal aplikasi
✅ Login customer dan technician
✅ Register customer dan technician
✅ Penyimpanan access token dan data user lokal
✅ AuthInterceptor untuk mengirim Bearer token otomatis
✅ Customer home dengan device category
✅ Customer technician discovery
✅ Customer create service request
✅ Customer order history
✅ Customer order detail
✅ Customer cancel service request
✅ Technician home / request list
✅ Technician request detail
✅ Technician accept request
✅ Technician reject request
✅ Technician start work
✅ Technician complete work
✅ Technician skill/device category management
✅ Notification UI dan parser response fleksibel
```

Yang belum selesai / belum sinkron penuh dengan backend roadmap terbaru:

```text
🟡 Status history timeline belum ada di UI mobile
🟡 Customer create review setelah request COMPLETED belum ada
🟡 List review technician belum ada
🟡 Technician update availability belum ada
🟡 User/technician profile update belum lengkap
🟡 Notification UI sudah ada, tetapi backend notification masih deferred
🟡 Chat masih placeholder / belum tersedia
```

---

## Table of Contents

1. [Tujuan Mobile](#1-tujuan-mobile)
2. [Tech Stack](#2-tech-stack)
3. [Arsitektur Mobile](#3-arsitektur-mobile)
4. [Struktur Folder](#4-struktur-folder)
5. [Aturan Kontrak dengan Backend](#5-aturan-kontrak-dengan-backend)
6. [Konfigurasi Backend URL](#6-konfigurasi-backend-url)
7. [Menjalankan Project dari Nol](#7-menjalankan-project-dari-nol)
8. [Menjalankan Project Harian](#8-menjalankan-project-harian)
9. [Flow Aplikasi](#9-flow-aplikasi)
10. [Endpoint yang Digunakan Mobile](#10-endpoint-yang-digunakan-mobile)
11. [Response Format](#11-response-format)
12. [Session dan Security](#12-session-dan-security)
13. [Testing](#13-testing)
14. [Gradle Commands](#14-gradle-commands)
15. [Git Rules](#15-git-rules)
16. [Troubleshooting](#16-troubleshooting)
17. [Catatan Developer](#17-catatan-developer)
18. [Urutan Pengerjaan Terdekat](#18-urutan-pengerjaan-terdekat)
19. [Commit Convention](#19-commit-convention)

---

# 1. Tujuan Mobile

Mobile Teknisio bertugas menjadi antarmuka utama untuk customer dan technician.

Fungsi utama mobile:

- Menampilkan onboarding dan halaman awal aplikasi.
- Mengarahkan user sesuai status login dan role.
- Menyediakan halaman login dan register.
- Menyimpan session user lokal setelah login/register berhasil.
- Mengirim request ke backend menggunakan Retrofit.
- Menampilkan data kategori alat elektronik dari backend.
- Menampilkan daftar technician berdasarkan kategori alat elektronik.
- Membuat service request dengan alamat manual.
- Menampilkan status dan riwayat service request customer.
- Menampilkan request masuk untuk technician.
- Mengizinkan technician mengubah status request: `ACCEPTED`, `REJECTED`, `ON_PROGRESS`, dan `COMPLETED`.
- Mengelola keahlian technician berdasarkan device category.

Untuk MVP saat ini, input lokasi menggunakan field manual:

```text
address
addressDetail
```

Mobile tidak wajib memakai GPS pada flow MVP stable saat ini.

---

# 2. Tech Stack

| Komponen | Teknologi / Status |
|---|---|
| Platform | Android |
| Bahasa utama | Java |
| Build Tool | Gradle Kotlin DSL |
| Namespace | `com.teknisio.mobile` |
| Application ID | `com.teknisio.mobile` |
| Min SDK | 23 |
| Target SDK | 36 |
| Compile SDK | 36 |
| Version Code | 1 |
| Version Name | 0.9.0 |
| UI | XML Layout + Activity |
| Theme | Material3 DayNight NoActionBar |
| Networking | Retrofit 2.11.0 |
| JSON Converter | Gson Converter 2.11.0 |
| HTTP Client | OkHttp |
| HTTP Logging | OkHttp Logging Interceptor 4.12.0 |
| Local Session | SharedPreferences via `TokenManager` |
| Security | JWT Bearer Token dari backend |
| Testing | JUnit dan Android Instrumented Test template |

Dependency penting di `app/build.gradle.kts`:

```kotlin
implementation(libs.activity.ktx)
implementation(libs.appcompat)
implementation(libs.constraintlayout)
implementation(libs.material)

implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

testImplementation(libs.junit)
androidTestImplementation(libs.espresso.core)
androidTestImplementation(libs.ext.junit)
```

---

# 3. Arsitektur Mobile

Mobile menggunakan pendekatan:

```text
Android Activity-based Client + REST API Layer
```

Alur data utama:

```text
Activity / UI
    ↓
Request Model
    ↓
ApiService Retrofit
    ↓
ApiClient + AuthInterceptor
    ↓
Backend REST API
    ↓
ApiResponse<T>
    ↓
Response Model
    ↓
Render UI
```

Tanggung jawab tiap bagian:

| Bagian | Tanggung Jawab |
|---|---|
| `view` | Activity dan tampilan aplikasi |
| `model.request` | Body request yang dikirim ke backend |
| `model.response` | Struktur response dari backend |
| `network` | Retrofit client, API interface, dan auth interceptor |
| `local` | Penyimpanan session/token lokal |
| `util` | Helper untuk toast, error parser, status, text, dan view |
| `base` | Base activity dan konfigurasi umum activity |

Catatan penting:

- Activity boleh mengatur UI dan memanggil API.
- Model request/response harus mengikuti kontrak backend.
- Jangan menyimpan password user di local storage.
- Jangan hardcode token di source code.
- Semua request protected harus mengandalkan `AuthInterceptor`.

---

# 4. Struktur Folder

Struktur utama project:

```text
.
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/teknisio/mobile/
│       │   └── res/
│       ├── test/
│       └── androidTest/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── gradlew.bat
```

Struktur package Java:

```text
app/src/main/java/com/teknisio/mobile/
├── MainActivity.java
├── base/
├── controller/
├── local/
│   └── TokenManager.java
├── model/
│   ├── request/
│   └── response/
├── network/
│   ├── ApiClient.java
│   ├── ApiService.java
│   └── AuthInterceptor.java
├── util/
└── view/
    ├── auth/
    ├── customer/
    ├── onboarding/
    └── technician/
```

Struktur resource penting:

```text
app/src/main/res/
├── drawable/
├── layout/
├── mipmap-*/
├── values/
├── values-night/
└── xml/
    ├── backup_rules.xml
    ├── data_extraction_rules.xml
    └── network_security_config.xml
```

---

# 5. Aturan Kontrak dengan Backend

Aturan utama integrasi mobile dengan backend:

- Mobile tidak membuat kontrak API sendiri.
- Mobile mengikuti endpoint resmi backend.
- Endpoint resmi memakai bahasa Inggris.
- Field request dan response memakai bahasa Inggris.
- Semua response backend dibaca sebagai `ApiResponse<T>`.
- Token dikirim dengan header `Authorization: Bearer {accessToken}`.
- Role menentukan halaman dan endpoint yang boleh diakses.
- Customer hanya mengakses endpoint `/api/customers/**`.
- Technician hanya mengakses endpoint `/api/technicians/**`.
- Endpoint publik seperti `/api/auth/login` dan `/api/device-categories` tidak membutuhkan token.

Mapping istilah penting:

| Konsep | Nama API / Mobile Model |
|---|---|
| User | `user` |
| Customer | `customer` |
| Technician | `technician` |
| Kategori alat elektronik | `deviceCategory` |
| ID kategori alat elektronik | `deviceCategoryId` |
| Keahlian technician | `technicianDeviceCategory` |
| Permintaan layanan | `serviceRequest` |
| ID permintaan layanan | `serviceRequestId` |
| Technician profile | `technicianProfileId` |
| Kategori terpilih | `selectedDeviceCategories` |
| Deskripsi masalah | `issueDescription` |
| Alamat | `address` |
| Detail alamat | `addressDetail` |
| Alasan batal | `cancelReason` |
| Alasan tolak | `rejectReason` |
| Catatan technician | `technicianNote` |
| Biaya akhir | `finalCost` |

---

# 6. Konfigurasi Backend URL

Mobile memakai Retrofit untuk mengakses backend. Konfigurasi URL backend biasanya berada di salah satu file berikut:

```text
app/src/main/java/com/teknisio/mobile/network/ApiClient.java
app/src/main/java/com/teknisio/mobile/util/Constants.java
```

Pastikan `BASE_URL` selalu diakhiri `/`.

Contoh untuk backend Railway:

```java
public static final String BASE_URL = "https://nama-backend.up.railway.app/";
```

Contoh untuk emulator Android Studio yang mengakses backend lokal laptop:

```java
public static final String BASE_URL = "http://10.0.2.2:8080/";
```

Contoh untuk HP fisik yang satu Wi-Fi dengan laptop:

```java
public static final String BASE_URL = "http://IP_LAPTOP:8080/";
```

Contoh:

```java
public static final String BASE_URL = "http://192.168.1.10:8080/";
```

Catatan penting:

- `localhost` di emulator/HP berarti perangkat Android itu sendiri, bukan laptop.
- Untuk emulator Android Studio gunakan `10.0.2.2`.
- Untuk HP fisik gunakan IP laptop.
- Jika memakai HTTP lokal, pastikan `network_security_config.xml` mengizinkan cleartext traffic saat development.
- Untuk release, sebaiknya gunakan HTTPS.

---

# 7. Menjalankan Project dari Nol

## 7.1 Install tools

Pastikan sudah terinstall:

- Android Studio
- Android SDK
- Git
- Emulator Android atau HP Android fisik
- Backend Teknisio sudah bisa dijalankan atau URL deploy sudah tersedia

Cek Gradle wrapper:

```bash
./gradlew --version
```

Windows:

```bash
gradlew.bat --version
```

## 7.2 Clone repository

```bash
git clone <url-repository-mobile>
cd teknisio_mobile
```

Sesuaikan nama folder jika repository berbeda.

## 7.3 Buka di Android Studio

Langkah:

```text
Android Studio
↓
Open
↓
Pilih folder root project mobile
↓
Tunggu Gradle Sync selesai
```

Pastikan module `app` terbaca.

## 7.4 Pastikan backend aktif

Jika memakai backend lokal:

```bash
cd teknisio_backend
docker compose up -d
./gradlew bootRun
```

Cek health backend:

```bash
curl http://localhost:8080/actuator/health
```

Expected:

```json
{
  "status": "UP"
}
```

## 7.5 Sesuaikan `BASE_URL`

Untuk emulator:

```text
http://10.0.2.2:8080/
```

Untuk HP fisik:

```text
http://IP_LAPTOP:8080/
```

Untuk deploy Railway:

```text
https://nama-backend.up.railway.app/
```

## 7.6 Jalankan aplikasi

Dari Android Studio:

```text
Pilih device/emulator
↓
Run app
```

Dari terminal:

```bash
./gradlew installDebug
```

Windows:

```bash
gradlew.bat installDebug
```

---

# 8. Menjalankan Project Harian

Untuk developer yang sudah setup:

```bash
# 1. Pastikan backend aktif atau deploy URL bisa diakses
curl http://localhost:8080/actuator/health

# 2. Build debug APK
./gradlew assembleDebug

# 3. Install ke emulator/device
./gradlew installDebug
```

Jika menjalankan dari Android Studio, cukup:

```text
Sync Gradle
↓
Run app
```

Clean build jika ada error aneh:

```bash
./gradlew clean assembleDebug
```

---

# 9. Flow Aplikasi

## 9.1 Flow awal aplikasi

```text
OnboardingActivity
↓
MainActivity
↓
Cek TokenManager
↓
Jika belum login → LoginActivity
Jika role CUSTOMER → CustomerHomeActivity
Jika role TECHNICIAN → TechnicianHomeActivity
Jika role tidak valid → clear session → LoginActivity
```

## 9.2 Flow customer

```text
Customer register / login
↓
CustomerHomeActivity
↓
Load device categories
↓
Customer memilih device category
↓
Mobile memanggil search technician
↓
Customer memilih technician
↓
OrderTechnicianActivity
↓
Customer memilih satu atau lebih supported device categories
↓
Customer mengisi issueDescription, address, addressDetail
↓
Mobile membuat service request
↓
Customer melihat order history / detail
↓
Customer bisa cancel selama status masih diizinkan backend
```

## 9.3 Flow technician

```text
Technician register / login
↓
TechnicianHomeActivity
↓
Technician melihat request masuk
↓
Technician membuka detail request
↓
Technician accept atau reject
↓
Jika accept, technician start work
↓
Technician complete request dengan finalCost dan technicianNote
```

## 9.4 Flow skill technician

```text
Technician login
↓
Buka halaman skill/device category
↓
Load device categories aktif
↓
Load skill milik technician
↓
Technician tambah skill
↓
Technician hapus/nonaktifkan skill
```

---

# 10. Endpoint yang Digunakan Mobile

Endpoint berikut sudah ada di `ApiService` mobile.

## 10.1 Public / Auth

```text
POST /api/auth/login
POST /api/auth/register/customer
POST /api/auth/register/technician
GET  /api/auth/profile
```

## 10.2 Device Category

```text
GET /api/device-categories
```

Catatan:

- Detail endpoint `GET /api/device-categories/{deviceCategoryId}` tersedia di backend, tetapi tidak wajib dipakai mobile jika list sudah cukup.

## 10.3 Customer Technician Discovery

```text
GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}&availabilityStatus={status}&sort={sort}
GET /api/customers/technicians/{technicianProfileId}
```

Optional query:

```text
availabilityStatus=ONLINE|OFFLINE|BUSY|ON_LEAVE
sort=rating|totalJobs|name
```

## 10.4 Customer Service Request

```text
POST  /api/customers/service-requests
GET   /api/customers/service-requests?status={status}
GET   /api/customers/service-requests/{serviceRequestId}
PATCH /api/customers/service-requests/{serviceRequestId}/cancel
```

Request create service request:

```json
{
  "technicianProfileId": "uuid",
  "deviceCategoryIds": ["uuid"],
  "issueDescription": "AC tidak dingin",
  "address": "Jl. Contoh No. 123",
  "addressDetail": "Rumah pagar hitam"
}
```

## 10.5 Technician Device Category / Skill

```text
GET    /api/technicians/device-categories
POST   /api/technicians/device-categories
DELETE /api/technicians/device-categories/{deviceCategoryId}
```

Request tambah skill:

```json
{
  "deviceCategoryId": "uuid"
}
```

## 10.6 Technician Service Request

```text
GET   /api/technicians/service-requests?status={status}&sort={sort}
GET   /api/technicians/service-requests/{serviceRequestId}
PATCH /api/technicians/service-requests/{serviceRequestId}/accept
PATCH /api/technicians/service-requests/{serviceRequestId}/reject
PATCH /api/technicians/service-requests/{serviceRequestId}/start
PATCH /api/technicians/service-requests/{serviceRequestId}/complete
```

Request reject:

```json
{
  "rejectReason": "Jadwal tidak tersedia"
}
```

Request complete:

```json
{
  "finalCost": 150000,
  "technicianNote": "AC sudah dibersihkan dan dicek ulang"
}
```

## 10.7 Notification UI

Mobile sudah menyiapkan endpoint:

```text
GET   /api/notifications
PATCH /api/notifications/{notificationId}/read
```

Catatan:

- UI notification sudah ada di mobile.
- Backend notification masih deferred, jadi fitur ini belum dianggap final MVP.
- Jika backend belum menyediakan endpoint, UI harus menampilkan empty state atau error yang ramah.

## 10.8 Endpoint backend yang belum dipakai mobile

Endpoint berikut perlu ditambahkan ke mobile berikutnya:

```text
GET  /api/customers/service-requests/{serviceRequestId}/status-history
GET  /api/technicians/service-requests/{serviceRequestId}/status-history
POST /api/customers/service-requests/{serviceRequestId}/review
GET  /api/customers/technicians/{technicianProfileId}/reviews
PUT  /api/users/me
PATCH /api/technicians/availability
PUT  /api/technicians/profile
```

Deferred:

```text
POST /api/service-requests/{serviceRequestId}/messages
GET  /api/service-requests/{serviceRequestId}/messages
WebSocket chat
```

---

# 11. Response Format

Semua response utama backend memakai wrapper:

```java
ApiResponse<T>
```

Model mobile:

```java
public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
    public Object errors;
}
```

## 11.1 Success response

```json
{
  "success": true,
  "message": "Success message",
  "data": {},
  "errors": {}
}
```

## 11.2 Error response

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "errors": {}
}
```

## 11.3 Validation error

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

Aturan di mobile:

- Jika `response.isSuccessful()` dan `body.success == true`, render `data`.
- Jika HTTP 4xx/5xx, parse error body dengan `ErrorParser`.
- Jangan tampilkan raw stack trace ke user.
- Tampilkan pesan pendek melalui `AppToast` atau empty state.

---

# 12. Session dan Security

## 12.1 TokenManager

`TokenManager` bertugas menyimpan session lokal, seperti:

```text
accessToken
userId
technicianProfileId
name
email
phoneNumber
address
role
accountStatus
```

Fungsi penting:

```text
isLoggedIn()
isCustomer()
isTechnician()
clearSession()
```

## 12.2 AuthInterceptor

`AuthInterceptor` membaca access token dari `TokenManager`, lalu menambahkan header:

```http
Authorization: Bearer {accessToken}
Accept: application/json
```

Aturan:

- Request publik tetap boleh berjalan tanpa token.
- Request protected otomatis memakai token jika token tersedia.
- Saat logout, panggil `tokenManager.clearSession()` dan `ApiClient.reset()`.

## 12.3 Routing berdasarkan role

`MainActivity` mengarahkan user berdasarkan role:

```text
Belum login → LoginActivity
CUSTOMER → CustomerHomeActivity
TECHNICIAN → TechnicianHomeActivity
Role tidak valid → clear session → LoginActivity
```

## 12.4 Logout MVP

Untuk MVP saat ini, logout dilakukan dari sisi client:

```text
clear local token
reset ApiClient
kembali ke LoginActivity
```

Catatan:

- Backend refresh token/logout server-side belum prioritas utama di roadmap terbaru.
- Jika backend nanti menambahkan logout server-side, mobile perlu menambahkan endpoint logout.

---

# 13. Testing

## 13.1 Build validation

```bash
./gradlew clean assembleDebug
```

Windows:

```bash
gradlew.bat clean assembleDebug
```

## 13.2 Unit test

```bash
./gradlew test
```

## 13.3 Instrumented test

Butuh emulator/device aktif:

```bash
./gradlew connectedAndroidTest
```

## 13.4 Manual smoke test customer

Checklist customer:

```text
[ ] App terbuka dari onboarding
[ ] Login customer berhasil
[ ] Register customer berhasil
[ ] Customer diarahkan ke CustomerHomeActivity
[ ] Device categories tampil
[ ] Customer memilih device category
[ ] Daftar technician tampil
[ ] Detail technician tampil
[ ] Customer membuat service request
[ ] Order muncul di history
[ ] Detail order bisa dibuka
[ ] Customer bisa cancel request dengan alasan
[ ] Logout menghapus session dan kembali ke login
```

## 13.5 Manual smoke test technician

Checklist technician:

```text
[ ] Login technician berhasil
[ ] Register technician berhasil
[ ] Technician diarahkan ke TechnicianHomeActivity
[ ] Technician bisa melihat request masuk
[ ] Technician bisa membuka detail request
[ ] Technician bisa accept request
[ ] Technician bisa reject request dengan alasan
[ ] Technician bisa start request
[ ] Technician bisa complete request dengan finalCost dan technicianNote
[ ] Technician bisa melihat skill/device category miliknya
[ ] Technician bisa tambah skill
[ ] Technician bisa hapus/nonaktifkan skill
[ ] Logout menghapus session dan kembali ke login
```

## 13.6 Manual test error state

Setiap fitur API minimal dites:

```text
[ ] Backend mati / tidak reachable
[ ] Token kosong / session habis
[ ] Role salah
[ ] Form kosong
[ ] Invalid input
[ ] Data kosong dari backend
[ ] Error 400 tampil informatif
[ ] Error 401 mengarah ke login atau memberi pesan session
[ ] Error 403 memberi pesan akses ditolak
[ ] Error 404 memberi pesan data tidak ditemukan
[ ] Loading state tidak stuck
```

---

# 14. Gradle Commands

Melihat versi Gradle:

```bash
./gradlew --version
```

Build debug APK:

```bash
./gradlew assembleDebug
```

Install debug APK ke device/emulator:

```bash
./gradlew installDebug
```

Clean project:

```bash
./gradlew clean
```

Clean build:

```bash
./gradlew clean assembleDebug
```

Unit test:

```bash
./gradlew test
```

Instrumented test:

```bash
./gradlew connectedAndroidTest
```

Windows:

```bash
gradlew.bat assembleDebug
gradlew.bat installDebug
gradlew.bat clean assembleDebug
```

---

# 15. Git Rules

## 15.1 Jangan commit

```text
local.properties
.gradle/
build/
app/build/
.idea/workspace.xml
.idea/caches/
.idea/libraries/
.idea/modules.xml
.idea/navEditor.xml
.idea/assetWizardSettings.xml
*.iml
.DS_Store
/captures
.externalNativeBuild/
.cxx/
*.apk
*.aab
```

## 15.2 Boleh dan perlu commit

```text
build.gradle.kts
settings.gradle.kts
gradle/libs.versions.toml
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
app/build.gradle.kts
app/proguard-rules.pro
app/src/main/AndroidManifest.xml
app/src/main/java/**
app/src/main/res/**
app/src/test/**
app/src/androidTest/**
Readme.md
Roadmap.md
```

Catatan:

- `local.properties` berisi path Android SDK lokal, jangan commit.
- Jangan commit file build output.
- Jangan commit APK debug kecuali memang diminta untuk distribusi manual.
- File resource icon, layout, drawable, dan XML wajib di-commit.

---

# 16. Troubleshooting

## 16.1 Gradle sync gagal

Coba:

```bash
./gradlew clean
```

Lalu di Android Studio:

```text
File → Sync Project with Gradle Files
```

Jika masih gagal:

```text
File → Invalidate Caches → Restart
```

## 16.2 SDK tidak ditemukan

Error biasanya terkait `local.properties`.

Solusi:

- Buka project di Android Studio.
- Pastikan Android SDK terinstall.
- Android Studio akan membuat `local.properties` otomatis.

Contoh isi lokal:

```properties
sdk.dir=/home/user/Android/Sdk
```

Jangan commit file ini.

## 16.3 Aplikasi tidak bisa akses backend lokal dari emulator

Jangan pakai:

```text
http://localhost:8080/
```

Gunakan:

```text
http://10.0.2.2:8080/
```

Untuk emulator Android Studio, `10.0.2.2` mengarah ke localhost laptop.

## 16.4 Aplikasi tidak bisa akses backend lokal dari HP fisik

Gunakan IP laptop:

```text
http://192.168.x.x:8080/
```

Cek IP laptop:

```bash
ip addr
```

atau:

```bash
hostname -I
```

Pastikan:

- HP dan laptop berada di Wi-Fi yang sama.
- Firewall laptop tidak memblokir port `8080`.
- Backend bind ke port `8080`.

## 16.5 Cleartext HTTP blocked

Untuk development lokal HTTP, cek:

```text
app/src/main/res/xml/network_security_config.xml
```

Pastikan cleartext diizinkan untuk debug.

Contoh:

```xml
<network-security-config>
    <base-config cleartextTrafficPermitted="true" />
</network-security-config>
```

Untuk release, lebih aman gunakan HTTPS.

## 16.6 Token tidak terkirim

Cek:

```text
TokenManager menyimpan accessToken
AuthInterceptor terdaftar di OkHttpClient
ApiClient sudah di-reset setelah login/register/logout
```

Setelah login/register berhasil, panggil:

```java
ApiClient.reset();
```

## 16.7 Login berhasil tapi masuk halaman salah

Cek nilai role dari backend:

```text
CUSTOMER
TECHNICIAN
```

Cek juga method:

```java
tokenManager.isCustomer()
tokenManager.isTechnician()
```

Jika role tidak valid, `MainActivity` akan clear session dan kembali ke login.

## 16.8 Response error tidak terbaca

Cek `ErrorParser`.

Backend error normalnya berbentuk:

```json
{
  "success": false,
  "message": "Error message",
  "data": null,
  "errors": {}
}
```

Mobile sebaiknya mengambil `message`, bukan menampilkan raw error body.

## 16.9 Notification kosong / error

Kemungkinan:

- Backend notification belum aktif.
- Endpoint `/api/notifications` belum tersedia.
- Response notification belum sesuai parser.

Untuk MVP, tampilkan empty state yang aman:

```text
Belum ada notifikasi.
```

atau:

```text
Fitur notifikasi belum tersedia.
```

---

# 17. Catatan Developer

- Jangan ubah endpoint mobile tanpa mengecek kontrak backend.
- Jangan expose token di log.
- Jangan hardcode data user test di source code.
- Semua field API harus English.
- UI text boleh berbahasa Indonesia.
- Gunakan `AppToast` untuk pesan sukses/error/warning/info.
- Gunakan `ErrorParser` untuk membaca pesan error backend.
- Gunakan `OrderStatusHelper` untuk normalisasi/tampilan status order.
- Gunakan `TextHelper` untuk fallback text dan validasi string kosong.
- Gunakan `ViewHelper` untuk helper UI yang berulang.
- Jangan memaksa fitur notification/chat sebelum backend siap.
- Jangan rombak struktur besar jika flow MVP sudah jalan.
- Setiap fitur baru harus dites manual customer dan technician.

---

# 18. Urutan Pengerjaan Terdekat

Urutan paling aman dari kondisi mobile sekarang:

```text
1. MOB-09 Tambah status history timeline di detail order customer
2. MOB-09b Tambah status history timeline di detail order technician
3. MOB-10 Tambah create review setelah request COMPLETED
4. MOB-10b Tambah list review technician jika backend endpoint tersedia
5. MOB-11 Tambah technician availability update setelah backend endpoint siap
6. MOB-12 Tambah profile update setelah backend endpoint siap
7. MOB-13 Rapikan BASE_URL debug/release
8. MOB-14 Stabilkan notification setelah backend notification aktif
9. MOB-15 Tambah REST chat setelah backend chat REST aktif
10. MOB-16 WebSocket chat setelah REST chat stabil
```

Prioritas immediate:

```text
🔥 Status history timeline
🔥 Review after completed request
🔥 Availability technician
```

Yang tidak perlu diprioritaskan sekarang:

```text
⏳ Admin mobile
⏳ Payment
⏳ GPS wajib
⏳ Kamera/media request
⏳ WebSocket chat langsung
```

---

# 19. Commit Convention

Contoh commit fitur mobile:

```bash
git add .
git commit -m "feat add customer status history timeline"
```

Contoh commit perbaikan integrasi API:

```bash
git add .
git commit -m "fix align service request dto with backend contract"
```

Contoh commit UI:

```bash
git add .
git commit -m "ui improve technician request detail screen"
```

Contoh commit dokumentasi:

```bash
git add Readme.md Roadmap.md
git commit -m "docs update mobile readme and roadmap"
```

---

# 20. Ringkasan Singkat

```text
Mobile Teknisio sudah siap untuk core MVP customer dan technician.
Auth, device category, technician discovery, service request customer, service request technician, dan skill technician sudah tersedia.
Fokus berikutnya adalah status history timeline, review setelah completed, dan availability technician.
Notification dan chat tetap deferred sampai backend stabil.
```
