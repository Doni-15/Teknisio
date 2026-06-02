# Roadmap Mobile Teknisio

Roadmap pengerjaan aplikasi mobile **Teknisio: Solusi Servis Anda** berbasis Android Java.

Dokumen ini menjadi panduan utama pengembangan mobile: berisi prinsip pengembangan, flow MVP, status pengerjaan, roadmap per modul, kontrak integrasi backend, prioritas pekerjaan, testing checklist, dan catatan teknis agar aplikasi mobile tetap sinkron dengan backend Teknisio.

---

## Prinsip Utama

- Mobile hanya bertindak sebagai client.
- Semua proses bisnis utama tetap divalidasi oleh backend.
- Mobile berkomunikasi dengan backend melalui REST API + JSON.
- Semua request/response API menggunakan field **English** agar konsisten dengan backend.
- Jangan membuat endpoint khusus Android.
- Jangan hardcode logika bisnis yang seharusnya milik backend.
- Jangan menyimpan password di local storage.
- Token JWT disimpan lokal hanya untuk kebutuhan session user.
- Semua request protected wajib memakai header `Authorization: Bearer {token}`.
- Semua response API mengikuti wrapper backend `ApiResponse<T>`.
- Error dari backend wajib ditampilkan dengan pesan yang informatif kepada user.
- Untuk MVP, customer memilih `deviceCategory`, memilih `technician`, mengisi `issueDescription`, `address`, dan `addressDetail`.
- Untuk MVP mobile saat ini, lokasi otomatis/GPS tidak dipaksakan karena backend sudah mendukung input alamat manual.
- Chat, notification, GPS, kamera, dan WebSocket ditunda sampai core service request stabil.

---

## Legend Status

| Badge | Arti |
|---|---|
| ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) | Sudah selesai dan sudah bisa dipakai di aplikasi |
| ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) | Sudah ada kode/UI/client, tetapi belum final atau belum diuji penuh end-to-end |
| ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) | Sedang dikerjakan / next immediate |
| ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) | Belum dikerjakan |
| ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) | Ditunda setelah MVP stable |
| ![blocked](https://img.shields.io/badge/%5Bblocked%5D-red?style=flat-square) | Menunggu endpoint backend atau keputusan desain |
| ![legacy](https://img.shields.io/badge/%5Blegacy%5D-lightgrey?style=flat-square) | Ada di konsep lama, tetapi tidak dipakai untuk MVP stable |

---

## Status Global Mobile

```text
✅ Android project structure tersedia
✅ Login customer/technician tersedia
✅ Register customer/technician tersedia
✅ Token JWT disimpan lokal lewat TokenManager
✅ AuthInterceptor otomatis memasang Bearer token
✅ Retrofit ApiService sudah mengikuti mayoritas endpoint backend MVP
✅ Device category list tersedia
✅ Customer technician discovery tersedia
✅ Customer create service request tersedia
✅ Customer order history/detail/cancel tersedia
✅ Technician request list/detail tersedia
✅ Technician accept/reject/start/complete tersedia
✅ Technician skill management tersedia
⚠️ Status history timeline belum tersedia di mobile
⚠️ Review belum tersedia di mobile
⚠️ Technician availability update belum tersedia di mobile
⚠️ Chat masih placeholder/deferred
⚠️ Notification client sudah ada, tetapi backend notification masih deferred
⚠️ Base URL masih hardcoded di Constants
```

---

## Flow Final MVP Mobile

```text
User membuka aplikasi
↓
Onboarding / splash menentukan session
↓
User login atau register sebagai CUSTOMER / TECHNICIAN
↓
Token JWT dan data user disimpan di TokenManager
↓
Jika role CUSTOMER:
  Customer masuk Customer Home
  ↓
  Customer melihat daftar device category
  ↓
  Customer memilih device category
  ↓
  Customer melihat daftar technician yang mendukung device category tersebut
  ↓
  Customer bisa filter availabilityStatus dan sort rating/totalJobs/name
  ↓
  Customer membuka detail technician
  ↓
  Customer memilih technician dan device category yang didukung technician
  ↓
  Customer mengisi issueDescription, address, addressDetail
  ↓
  Customer membuat service request
  ↓
  Customer melihat order history dan detail order
  ↓
  Customer bisa cancel order selama status masih diperbolehkan
  ↓
  Customer melihat status history timeline
  ↓
  Customer memberi review setelah status COMPLETED

Jika role TECHNICIAN:
  Technician masuk Technician Home
  ↓
  Technician mengatur skill device category
  ↓
  Technician melihat request masuk / request miliknya
  ↓
  Technician membuka detail request
  ↓
  Technician accept atau reject request
  ↓
  Jika accepted, technician start work
  ↓
  Technician complete work dengan technicianNote dan finalCost jika tersedia
  ↓
  Technician melihat history request dan status history timeline
```

---

## Struktur Mobile Saat Ini

Struktur utama project mobile:

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

Aktivitas penting yang sudah terdaftar di manifest:

```text
OnboardingActivity
LoginActivity
RegisterCustomerActivity
RegisterTechnicianActivity
CustomerHomeActivity
TechnicianListActivity
TechnicianDetailActivity
OrderTechnicianActivity
OrderHistoryActivity
ServiceRequestDetailActivity
NotificationActivity
AccountActivity
TechnicianHomeActivity
TechnicianRequestDetailActivity
TechnicianSkillActivity
TechnicianHistoryActivity
NewsActivity
```

---

## Istilah Resmi Mobile

| Konsep | Nama di Mobile/API | Catatan |
|---|---|---|
| User | `user` | Data user dari auth/profile |
| Customer | `customer` | Role `CUSTOMER` |
| Technician | `technician` | Role `TECHNICIAN` |
| Technician profile | `technicianProfileId` | ID profil teknisi dari backend |
| Alat elektronik | `deviceCategory` | Bukan `jenisLayanan` untuk MVP |
| Permintaan layanan | `serviceRequest` | Order/service request customer |
| Kategori yang dipilih | `selectedDeviceCategories` | List kategori yang masuk order |
| Deskripsi masalah | `issueDescription` | Diisi customer |
| Alamat | `address` | Diinput manual user |
| Detail alamat | `addressDetail` | Opsional |
| Status request | `status` | WAITING, ACCEPTED, ON_PROGRESS, COMPLETED, CANCELLED, REJECTED |
| Riwayat status | `statusHistory` | Next mobile feature |
| Rating | `review.rating` | Next mobile feature |
| Komentar review | `review.comment` | Next mobile feature |
| Token | `accessToken` | Disimpan di TokenManager |

---

# 0. Fondasi Mobile

Target: aplikasi Android punya struktur yang jelas, dependency networking tersedia, session lokal berjalan, dan semua API call memakai format backend terbaru.

---

## MOB-00 [MVP] Struktur project Android

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `base`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `controller`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `local`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `model.request`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `model.response`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `network`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `util`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `view.auth`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `view.customer`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Package `view.technician`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Package/flow onboarding tersedia

Catatan:

- Struktur sekarang masih berbasis Activity Java manual.
- Untuk MVP, tidak perlu migrasi besar ke MVVM/Repository pattern dulu.
- Refactor MVVM boleh dilakukan setelah core flow stabil.

---

## MOB-01 [MVP] Setup networking Retrofit

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `ApiClient` tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `ApiService` tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Retrofit memakai `GsonConverterFactory`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) OkHttp client tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Logging interceptor aktif hanya untuk debug build
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `ApiClient.reset()` tersedia setelah auth berubah
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Pisahkan `BASE_URL` debug/release
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan timeout eksplisit OkHttp

Current base URL:

```java
public static final String BASE_URL = "https://steadfast-liberation-production-e36b.up.railway.app/";
```

Catatan:

- Untuk emulator lokal, pakai `http://10.0.2.2:8080/`.
- Untuk HP fisik, pakai IP laptop, misalnya `http://192.168.1.10:8080/`.
- Untuk release/demo, pakai URL deploy backend.

---

## MOB-02 [MVP] Global API response model

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `ApiResponse<T>` tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Model response mengikuti `success`, `message`, `data`, `errors`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `ErrorParser` tersedia untuk membaca error body backend
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Fallback message tersedia jika response error kosong
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Standarkan semua Activity memakai `ErrorParser.parseError()`

Contoh wrapper:

```json
{
  "success": true,
  "message": "Success message",
  "data": {},
  "errors": {}
}
```

---

## MOB-03 [MVP] Local session dan auth header

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `TokenManager` tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `accessToken`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `tokenType`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `expiresInMs`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `userId`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `technicianProfileId`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `name`, `email`, `phoneNumber`, `profilePhoto`, `address`, `role`, `accountStatus`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `AuthInterceptor` otomatis memasang `Authorization: Bearer {token}`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambah helper `isLoggedIn()` dan `isTokenExpired()` jika belum ada
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Refresh token client ditunda karena backend terbaru belum menjadikan refresh token sebagai MVP utama

---

## MOB-04 [MVP] UI helper dan error UX

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `AppToast` tersedia untuk success/error/warning/info
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) `BackButtonHelper` tersedia
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) `ViewHelper`, `TextHelper`, `OrderStatusHelper` tersedia untuk formatting UI
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Standarkan loading state di semua Activity
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Standarkan empty state di list screen
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Standarkan retry button saat network error

---

# 1. Auth Mobile

Target: customer dan technician bisa register, login, menyimpan session, dan diarahkan ke halaman sesuai role.

---

## MOB-10 [MVP] Login

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `LoginActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `LoginRequest`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `POST /api/auth/login`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan auth response ke `TokenManager`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Reset `ApiClient` setelah token berubah
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Redirect berdasarkan role `CUSTOMER` atau `TECHNICIAN`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan loading state saat login
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Disable button saat request berjalan agar tidak double submit

Contract:

```http
POST /api/auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "customer.demo@mail.com",
  "password": "password123"
}
```

---

## MOB-11 [MVP] Register customer

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `RegisterCustomerActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `RegisterCustomerRequest`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `POST /api/auth/register/customer`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `name`, `email`, `phoneNumber`, `password`, `address`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan token setelah register berhasil
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Validasi input sisi mobile agar tidak selalu menunggu error backend
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Disable submit saat loading

Contract:

```http
POST /api/auth/register/customer
Content-Type: application/json
```

Request:

```json
{
  "name": "Customer Demo",
  "email": "customer.demo@mail.com",
  "phoneNumber": "+6281234567890",
  "password": "password123",
  "address": "Jl. Contoh No. 123"
}
```

---

## MOB-12 [MVP] Register technician

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `RegisterTechnicianActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `RegisterTechnicianRequest`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `POST /api/auth/register/technician`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `name`, `email`, `phoneNumber`, `password`, `address`, `description`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Simpan `technicianProfileId` dari response user
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Validasi input sisi mobile agar lebih ramah user
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Setelah register technician, arahkan ke setup skill jika skill masih kosong

Contract:

```http
POST /api/auth/register/technician
Content-Type: application/json
```

Request:

```json
{
  "name": "Technician Demo",
  "email": "technician.demo@mail.com",
  "phoneNumber": "+6281234567891",
  "password": "password123",
  "address": "Jl. Teknisi No. 1",
  "description": "Teknisi elektronik rumah tangga"
}
```

---

## MOB-13 [MVP] Profile/session check

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/auth/profile` tersedia di `ApiService`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Account screen memakai data session lokal
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Refresh data profile dari backend saat membuka AccountActivity
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Jika profile return 401, clear session dan redirect login

Contract:

```http
GET /api/auth/profile
Authorization: Bearer {token}
```

---

## MOB-14 [MVP] Logout client-side

- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Logout dilakukan dengan menghapus session lokal
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Pastikan semua screen logout memanggil `TokenManager.clear()` dan `ApiClient.reset()`
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Server-side logout ditunda sampai backend refresh/logout diaktifkan lagi

---

# 2. Master Data Device Category

Target: customer dapat melihat alat elektronik, dan technician dapat memilih skill berdasarkan device category.

---

## MOB-20 [MVP] List device category

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/device-categories`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response model `DeviceCategoryResponse`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Customer home memuat kategori perangkat
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Icon ditampilkan dari field `icon`/mapping UI
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan empty state jika kategori kosong
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan retry jika gagal load kategori

Contract:

```http
GET /api/device-categories
```

---

## MOB-21 [MVP] Detail device category

- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Endpoint backend `GET /api/device-categories/{deviceCategoryId}` tersedia
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Mobile belum perlu screen detail kategori khusus

Catatan:

- Untuk MVP, customer cukup memilih kategori dari list lalu lanjut ke technician list.

---

# 3. Customer Technician Discovery

Target: customer dapat mencari technician berdasarkan device category, filter/sort, dan membuka detail technician.

---

## MOB-30 [MVP] Customer search technician

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `TechnicianListActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/customers/technicians`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Query `deviceCategoryId`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Query `availabilityStatus`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Query `sort`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response model `CustomerTechnicianResponse`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan UI empty state jika tidak ada technician
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan retry network

Contract:

```http
GET /api/customers/technicians?deviceCategoryId={deviceCategoryId}&availabilityStatus=ONLINE&sort=rating
Authorization: Bearer {customerToken}
```

Allowed availability status:

```text
ONLINE
OFFLINE
BUSY
ON_LEAVE
```

Allowed sort:

```text
rating
totalJobs
name
```

---

## MOB-31 [MVP] Customer technician detail

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `TechnicianDetailActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/customers/technicians/{technicianProfileId}`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan `name`, `availabilityStatus`, `averageRating`, `ratingCount`, `totalJobs`, `description`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan `supportedDeviceCategories`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Navigasi ke order technician tersedia
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan list review technician setelah backend read review tersedia

Contract:

```http
GET /api/customers/technicians/{technicianProfileId}
Authorization: Bearer {customerToken}
```

---

# 4. Customer Service Request

Target: customer dapat membuat order, melihat riwayat order, melihat detail order, dan membatalkan order.

---

## MOB-40 [MVP] Customer create service request

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `OrderTechnicianActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `CreateServiceRequestRequest`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `POST /api/customers/service-requests`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `technicianProfileId`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `deviceCategoryIds`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `issueDescription`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `address`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Kirim `addressDetail`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi minimal 1 category di UI
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Validasi issue description dan address wajib di UI
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Disable submit saat request berjalan
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Setelah sukses, arahkan ke detail service request atau history

Contract:

```http
POST /api/customers/service-requests
Authorization: Bearer {customerToken}
Content-Type: application/json
```

Request:

```json
{
  "technicianProfileId": "uuid",
  "deviceCategoryIds": ["uuid"],
  "issueDescription": "AC tidak dingin dan mengeluarkan suara berisik",
  "address": "Jl. Contoh No. 123, Medan",
  "addressDetail": "Rumah warna putih pagar hitam"
}
```

---

## MOB-41 [MVP] Customer order history

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `OrderHistoryActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/customers/service-requests`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Query `status` tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Response model `ServiceRequestResponse`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tampilkan selected device categories
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tampilkan status dan waktu request
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan pull-to-refresh atau tombol refresh konsisten
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan filter status yang jelas di UI jika belum nyaman

Contract:

```http
GET /api/customers/service-requests?status=WAITING
Authorization: Bearer {customerToken}
```

---

## MOB-42 [MVP] Customer order detail

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `ServiceRequestDetailActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/customers/service-requests/{serviceRequestId}`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan service request detail
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan selected device categories
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tampilkan status timestamp jika tersedia di response
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan section status history timeline
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan tombol review jika status `COMPLETED` dan belum review

Contract:

```http
GET /api/customers/service-requests/{serviceRequestId}
Authorization: Bearer {customerToken}
```

---

## MOB-43 [MVP] Customer cancel service request

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `PATCH /api/customers/service-requests/{serviceRequestId}/cancel`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `CancelServiceRequestRequest`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tombol cancel tampil di detail order sesuai status
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Kirim `cancelReason`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Pastikan tombol cancel hanya muncul pada `WAITING`, `ACCEPTED`, `ON_PROGRESS`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Konfirmasi dialog sebelum cancel

Contract:

```http
PATCH /api/customers/service-requests/{serviceRequestId}/cancel
Authorization: Bearer {customerToken}
Content-Type: application/json
```

Request:

```json
{
  "cancelReason": "Saya ingin membatalkan permintaan"
}
```

---

# 5. Technician Service Request

Target: technician dapat melihat request, membuka detail, menerima/menolak, memulai, dan menyelesaikan pekerjaan.

---

## MOB-50 [MVP] Technician request list

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `TechnicianHomeActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `TechnicianHistoryActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/technicians/service-requests`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Query `status`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Query `sort`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan request milik technician login
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tampilkan customer summary jika response tersedia
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan filter status yang konsisten antara home dan history
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan empty state dan retry konsisten

Contract:

```http
GET /api/technicians/service-requests?status=WAITING&sort=latest
Authorization: Bearer {technicianToken}
```

---

## MOB-51 [MVP] Technician request detail

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `TechnicianRequestDetailActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/technicians/service-requests/{serviceRequestId}`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan customer name/phone jika tersedia
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan issue description, address, addressDetail
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan selected device categories
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tampilkan tombol aksi berdasarkan status
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan section status history timeline

Contract:

```http
GET /api/technicians/service-requests/{serviceRequestId}
Authorization: Bearer {technicianToken}
```

---

## MOB-52 [MVP] Technician accept request

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `PATCH /api/technicians/service-requests/{serviceRequestId}/accept`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tombol accept tersedia di detail request
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Konfirmasi dialog sebelum accept jika diperlukan
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Refresh detail setelah accept berhasil

Contract:

```http
PATCH /api/technicians/service-requests/{serviceRequestId}/accept
Authorization: Bearer {technicianToken}
```

---

## MOB-53 [MVP] Technician reject request

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `PATCH /api/technicians/service-requests/{serviceRequestId}/reject`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `RejectServiceRequestRequest`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Kirim `rejectReason`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Dialog input alasan tolak yang jelas
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Validasi alasan tolak tidak kosong di mobile

Contract:

```http
PATCH /api/technicians/service-requests/{serviceRequestId}/reject
Authorization: Bearer {technicianToken}
Content-Type: application/json
```

Request:

```json
{
  "rejectReason": "Jadwal penuh"
}
```

---

## MOB-54 [MVP] Technician start work

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `PATCH /api/technicians/service-requests/{serviceRequestId}/start`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tombol start tersedia setelah status `ACCEPTED`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Refresh detail setelah start berhasil
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan timestamp `startedAt` jika tersedia

Contract:

```http
PATCH /api/technicians/service-requests/{serviceRequestId}/start
Authorization: Bearer {technicianToken}
```

---

## MOB-55 [MVP] Technician complete work

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `PATCH /api/technicians/service-requests/{serviceRequestId}/complete`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `CompleteServiceRequestRequest`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Kirim `technicianNote` jika tersedia
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Kirim `finalCost` jika tersedia
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Dialog complete yang rapi untuk catatan dan biaya akhir
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Validasi `finalCost` angka valid di mobile
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Refresh detail setelah complete berhasil

Contract:

```http
PATCH /api/technicians/service-requests/{serviceRequestId}/complete
Authorization: Bearer {technicianToken}
Content-Type: application/json
```

Request:

```json
{
  "finalCost": 150000,
  "technicianNote": "AC sudah dibersihkan dan freon dicek."
}
```

---

# 6. Technician Skill Management

Target: technician dapat mengatur kategori perangkat yang dikuasai.

---

## MOB-60 [MVP] Technician lihat skill

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Screen `TechnicianSkillActivity`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `GET /api/technicians/device-categories`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Tampilkan skill aktif milik technician login
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan empty state jika technician belum punya skill

Contract:

```http
GET /api/technicians/device-categories
Authorization: Bearer {technicianToken}
```

---

## MOB-61 [MVP] Technician tambah skill

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `POST /api/technicians/device-categories`
- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Request model `AddTechnicianDeviceCategoryRequest`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Pilih category dari list device category
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Disable add jika category sudah aktif

Contract:

```http
POST /api/technicians/device-categories
Authorization: Bearer {technicianToken}
Content-Type: application/json
```

Request:

```json
{
  "deviceCategoryId": "uuid"
}
```

---

## MOB-62 [MVP] Technician hapus skill

- ![finished](https://img.shields.io/badge/%5Bfinished%5D-brightgreen?style=flat-square) Endpoint `DELETE /api/technicians/device-categories/{deviceCategoryId}`
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Remove skill dari UI
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Konfirmasi sebelum hapus skill
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Refresh list skill setelah hapus berhasil

Contract:

```http
DELETE /api/technicians/device-categories/{deviceCategoryId}
Authorization: Bearer {technicianToken}
```

---

# 7. Status History Timeline

Target: customer dan technician dapat melihat riwayat perubahan status request secara jelas.

---

## MOB-70 [NEXT] Model status history response

- ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) Buat `ServiceRequestStatusHistoryResponse`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field `statusHistoryId`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field `previousStatus`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field `newStatus`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field `note`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field `changedByUserId`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field `changedAt`

---

## MOB-71 [NEXT] Customer status history endpoint

- ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) Tambahkan endpoint di `ApiService`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Panggil endpoint dari `ServiceRequestDetailActivity`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan timeline status di detail order customer
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan empty state jika history kosong

Contract:

```http
GET /api/customers/service-requests/{serviceRequestId}/status-history
Authorization: Bearer {customerToken}
```

---

## MOB-72 [NEXT] Technician status history endpoint

- ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) Tambahkan endpoint di `ApiService`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Panggil endpoint dari `TechnicianRequestDetailActivity`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan timeline status di detail request technician

Contract:

```http
GET /api/technicians/service-requests/{serviceRequestId}/status-history
Authorization: Bearer {technicianToken}
```

---

# 8. Review

Target: customer bisa memberi rating setelah service request selesai, dan customer lain dapat melihat review technician.

---

## MOB-80 [NEXT] Model review request/response

- ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) Buat `CreateReviewRequest`
- ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) Buat `ReviewResponse`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field request `rating`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field request `comment`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Field response `reviewId`, `serviceRequestId`, `customerId`, `technicianProfileId`, `rating`, `comment`, `createdAt`, `updatedAt`

---

## MOB-81 [NEXT] Customer create review

- ![ongoing](https://img.shields.io/badge/%5Bongoing%5D-blue?style=flat-square) Tambahkan endpoint di `ApiService`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambah tombol review di `ServiceRequestDetailActivity` jika status `COMPLETED`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Buat dialog/screen input rating 1-5
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Validasi rating wajib 1-5 di mobile
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Comment opsional
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Sembunyikan tombol review setelah review berhasil/deteksi duplicate dari backend

Contract:

```http
POST /api/customers/service-requests/{serviceRequestId}/review
Authorization: Bearer {customerToken}
Content-Type: application/json
```

Request:

```json
{
  "rating": 5,
  "comment": "Teknisi datang tepat waktu dan servisnya rapi."
}
```

---

## MOB-82 [NEXT] List review technician

- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambahkan endpoint di `ApiService` setelah backend read review tersedia
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan review di `TechnicianDetailActivity`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tampilkan rating average/count yang sudah ada di response technician
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Pagination review jika data sudah banyak

Contract:

```http
GET /api/customers/technicians/{technicianProfileId}/reviews
Authorization: Bearer {customerToken}
```

---

# 9. Profile dan Availability

Target: user dapat memperbarui data profil, dan technician dapat mengatur status ketersediaan.

---

## MOB-90 [NEXT] Account/Profile screen

- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Screen `AccountActivity` tersedia
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Tampilkan data dari `TokenManager`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Sinkronkan ulang profile dari `GET /api/auth/profile`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Update profile jika endpoint backend `PUT /api/users/me` sudah final
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Upload foto profile ditunda

---

## MOB-91 [NEXT] Technician availability update

- ![blocked](https://img.shields.io/badge/%5Bblocked%5D-red?style=flat-square) Menunggu endpoint backend `PATCH /api/technicians/availability`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Tambah dropdown/switch status di technician account/home
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Allowed values: `ONLINE`, `OFFLINE`, `BUSY`, `ON_LEAVE`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Refresh customer discovery setelah technician update availability

Contract rencana:

```http
PATCH /api/technicians/availability
Authorization: Bearer {technicianToken}
Content-Type: application/json
```

Request rencana:

```json
{
  "availabilityStatus": "ONLINE"
}
```

---

# 10. Notification

Target: user dapat melihat notifikasi dan menandai notifikasi sebagai sudah dibaca.

---

## MOB-100 [DEFERRED] Notification list

- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Screen `NotificationActivity` tersedia
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Model/parser notification tersedia
- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Endpoint mobile `GET /api/notifications` sudah ada
- ![blocked](https://img.shields.io/badge/%5Bblocked%5D-red?style=flat-square) Backend notification belum jadi prioritas MVP stable
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Aktifkan setelah backend notification selesai

Contract rencana:

```http
GET /api/notifications
Authorization: Bearer {token}
```

---

## MOB-101 [DEFERRED] Mark notification as read

- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Endpoint mobile `PATCH /api/notifications/{notificationId}/read` sudah ada
- ![blocked](https://img.shields.io/badge/%5Bblocked%5D-red?style=flat-square) Backend notification belum final
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Aktifkan setelah backend endpoint tersedia

Contract rencana:

```http
PATCH /api/notifications/{notificationId}/read
Authorization: Bearer {token}
```

---

# 11. Chat

Target: customer dan technician dapat berkomunikasi terkait service request.

---

## MOB-110 [DEFERRED] Chat placeholder

- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) Navigasi chat sudah ada sebagai placeholder/toast
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Chat tidak dikerjakan sebelum status history dan review stabil

---

## MOB-111 [DEFERRED] REST chat history

- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Buat `MessageResponse`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Buat `SendMessageRequest`
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Endpoint send message
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Endpoint get message history
- ![blocked](https://img.shields.io/badge/%5Bblocked%5D-red?style=flat-square) Menunggu backend chat REST

Contract rencana:

```http
GET  /api/service-requests/{serviceRequestId}/messages
POST /api/service-requests/{serviceRequestId}/messages
```

---

## MOB-112 [DEFERRED] WebSocket chat real-time

- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) WebSocket dikerjakan setelah REST chat stabil
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Setup WebSocket/STOMP client jika backend sudah siap
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Auto reconnect
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Local optimistic message state

---

# 12. Media, GPS, dan Kamera

Target: customer dapat menambahkan foto kondisi perangkat dan lokasi otomatis jika fitur ini diaktifkan kembali.

---

## MOB-120 [DEFERRED] GPS location

- ![legacy](https://img.shields.io/badge/%5Blegacy%5D-lightgrey?style=flat-square) SRS awal meminta GPS, tetapi MVP terbaru memakai input alamat manual
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Ambil latitude/longitude ditunda
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Permission location ditunda

Catatan:

- Untuk MVP sekarang, jangan paksa GPS karena aplikasi juga disiapkan sinkron dengan desktop flow.

---

## MOB-121 [DEFERRED] Upload foto kondisi perangkat

- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Kamera/gallery upload ditunda
- ![blocked](https://img.shields.io/badge/%5Bblocked%5D-red?style=flat-square) Menunggu backend media upload final
- ![todo](https://img.shields.io/badge/%5Btodo%5D-lightgrey?style=flat-square) Multipart request jika backend sudah siap

---

# 13. News / Informasi

Target: halaman informasi/news tidak mengganggu flow utama MVP.

---

## MOB-130 [OPTIONAL] News screen

- ![implemented](https://img.shields.io/badge/%5Bimplemented%5D-yellowgreen?style=flat-square) `NewsActivity` terdaftar
- ![deferred](https://img.shields.io/badge/%5Bdeferred%5D-lightgrey?style=flat-square) Tidak masuk prioritas MVP core

---

# 14. Ringkasan Contract API Mobile

## 14.1 Endpoint yang sudah dipakai mobile

### Auth

```text
POST /api/auth/login
POST /api/auth/register/customer
POST /api/auth/register/technician
GET  /api/auth/profile
```

### Device Category

```text
GET /api/device-categories
```

### Customer Technician Discovery

```text
GET /api/customers/technicians
GET /api/customers/technicians/{technicianProfileId}
```

### Customer Service Request

```text
POST  /api/customers/service-requests
GET   /api/customers/service-requests
GET   /api/customers/service-requests/{serviceRequestId}
PATCH /api/customers/service-requests/{serviceRequestId}/cancel
```

### Technician Device Category

```text
GET    /api/technicians/device-categories
POST   /api/technicians/device-categories
DELETE /api/technicians/device-categories/{deviceCategoryId}
```

### Technician Service Request

```text
GET   /api/technicians/service-requests
GET   /api/technicians/service-requests/{serviceRequestId}
PATCH /api/technicians/service-requests/{serviceRequestId}/accept
PATCH /api/technicians/service-requests/{serviceRequestId}/reject
PATCH /api/technicians/service-requests/{serviceRequestId}/start
PATCH /api/technicians/service-requests/{serviceRequestId}/complete
```

### Notification Client Existing, Backend Deferred

```text
GET   /api/notifications
PATCH /api/notifications/{notificationId}/read
```

---

## 14.2 Endpoint yang perlu ditambahkan berikutnya di mobile

### Status History

```text
GET /api/customers/service-requests/{serviceRequestId}/status-history
GET /api/technicians/service-requests/{serviceRequestId}/status-history
```

### Review

```text
POST /api/customers/service-requests/{serviceRequestId}/review
GET  /api/customers/technicians/{technicianProfileId}/reviews
```

### Profile / Availability

```text
PUT   /api/users/me
PATCH /api/technicians/availability
PUT   /api/technicians/profile
```

### Chat Later

```text
GET  /api/service-requests/{serviceRequestId}/messages
POST /api/service-requests/{serviceRequestId}/messages
WS   /ws
```

---

# 15. Testing Checklist Mobile

Setiap modul mobile baru wajib dites minimal:

- [ ] Screen bisa dibuka tanpa crash.
- [ ] Loading state muncul saat request berjalan.
- [ ] Success response tampil benar.
- [ ] Error response backend tampil sebagai pesan yang informatif.
- [ ] Network failure tidak membuat aplikasi crash.
- [ ] Empty state tampil jika data kosong.
- [ ] Unauthorized `401` mengarah ke login atau clear session.
- [ ] Forbidden `403` menampilkan pesan role tidak sesuai.
- [ ] Tombol submit tidak bisa ditekan berkali-kali saat loading.
- [ ] Data yang dikirim ke backend memakai field English sesuai kontrak.
- [ ] Setelah data berubah, screen refresh dari backend, bukan hanya ubah UI lokal.
- [ ] Role CUSTOMER tidak bisa masuk screen technician.
- [ ] Role TECHNICIAN tidak bisa masuk screen customer.
- [ ] Test di emulator.
- [ ] Test di HP fisik jika memakai IP laptop/backend Railway.

---

# 16. Manual Test Flow MVP

## 16.1 Customer full flow

```text
1. Register customer
2. Login customer
3. Masuk CustomerHomeActivity
4. Load device categories
5. Pilih Air Conditioner
6. Masuk TechnicianListActivity
7. Filter/sort technician
8. Buka TechnicianDetailActivity
9. Masuk OrderTechnicianActivity
10. Isi issueDescription
11. Isi address
12. Submit service request
13. Masuk OrderHistoryActivity
14. Buka ServiceRequestDetailActivity
15. Cancel request pada status yang boleh dibatalkan
16. Untuk request COMPLETED, beri review setelah fitur review aktif
```

## 16.2 Technician full flow

```text
1. Register technician
2. Login technician
3. Masuk TechnicianHomeActivity
4. Masuk TechnicianSkillActivity
5. Tambah device category skill
6. Lihat request masuk
7. Buka TechnicianRequestDetailActivity
8. Accept request
9. Start work
10. Complete work
11. Lihat TechnicianHistoryActivity
12. Cek status history setelah fitur timeline aktif
```

---

# 17. Urutan Pengerjaan Terdekat

Urutan paling aman dari kondisi mobile sekarang:

```text
1. MOB-70 Buat model status history response
2. MOB-71 Tambah customer status history endpoint + UI timeline
3. MOB-72 Tambah technician status history endpoint + UI timeline
4. MOB-80 Buat model review request/response
5. MOB-81 Tambah create review setelah request COMPLETED
6. MOB-82 Tambah list review di technician detail setelah backend read review siap
7. MOB-91 Tambah technician availability update setelah backend endpoint siap
8. MOB-90 Sinkronkan AccountActivity dengan GET /api/auth/profile
9. MOB-01 Rapikan BASE_URL debug/release
10. MOB-100 Notification setelah backend notification siap
11. MOB-111 REST chat setelah backend chat siap
12. MOB-112 WebSocket chat setelah REST chat stabil
```

Prioritas mutlak:

```text
Status History → Review → Availability → Profile Sync → Notification/Chat
```

---

# 18. Technical Debt

| Kode | Item | Prioritas |
|---|---|---|
| TD-MOB-01 | Pisahkan `BASE_URL` debug/release | Tinggi |
| TD-MOB-02 | Tambah timeout OkHttp | Sedang |
| TD-MOB-03 | Standarkan loading/empty/error state | Tinggi |
| TD-MOB-04 | Hindari duplicate API handling di setiap Activity | Sedang |
| TD-MOB-05 | Pertimbangkan Repository layer setelah MVP stabil | Sedang |
| TD-MOB-06 | Pertimbangkan MVVM setelah flow utama selesai | Rendah/Sedang |
| TD-MOB-07 | Tambah instrumentation/manual test checklist per screen | Sedang |
| TD-MOB-08 | Clear session otomatis saat 401 | Tinggi |
| TD-MOB-09 | Cegah double submit pada semua form | Tinggi |
| TD-MOB-10 | Rapikan format tanggal/waktu agar user-friendly | Sedang |

---

# 19. Git Rules Mobile

## File yang tidak boleh di-commit

```text
.gradle/
build/
local.properties
.idea/workspace.xml
.idea/caches/
*.iml
.DS_Store
captures/
.externalNativeBuild/
.cxx/
```

## File yang boleh di-commit

```text
app/build.gradle.kts
settings.gradle.kts
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
app/src/main/AndroidManifest.xml
app/src/main/java/**
app/src/main/res/**
```

Catatan:

- Jangan commit credential/token/API secret.
- Base URL production boleh di-commit jika memang endpoint public demo.
- Untuk URL lokal, sebaiknya pakai build config/debug config, bukan diedit manual bolak-balik.

---

# 20. Commit Convention Mobile

Contoh commit per phase:

```bash
git add .
git commit -m "mobile add customer service request flow"
```

Contoh commit integrasi endpoint:

```bash
git add .
git commit -m "mobile integrate service request status history api"
```

Contoh commit UI:

```bash
git add .
git commit -m "mobile add review dialog for completed service request"
```

Contoh commit fix:

```bash
git add .
git commit -m "mobile fix token reset after logout"
```

---

# 21. Catatan Penting untuk Tim

- Jangan ubah besar-besaran struktur mobile selama core flow masih belum 100% dites.
- Jangan langsung masuk chat/WebSocket sebelum status history dan review selesai.
- Jangan paksa GPS karena MVP terbaru memakai alamat manual.
- Jangan pakai endpoint lama seperti `/api/services` atau `/api/service-requests/me`.
- Jangan pakai istilah Indonesia pada field request/response API.
- Semua endpoint protected harus menggunakan JWT dari `TokenManager`.
- Setiap Activity yang memanggil API harus aman dari `null body`, `errorBody`, dan `onFailure`.
- Setelah backend menambah endpoint baru, update `ApiService`, model request/response, Activity, lalu manual test.
- Setelah mobile menambah flow baru, update roadmap ini.
