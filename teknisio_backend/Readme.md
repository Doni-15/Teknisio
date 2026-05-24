# Teknisio Backend

Backend service untuk aplikasi Teknisio, dibangun dengan Spring Boot dan PostgreSQL.
Disclimer: Beberapa tahap dan metode instalasi menggunakan OS Linux, silahkan
sesuaikan dengan OS anda

---

## Tech Stack

| Teknologi | Versi | Keterangan |
|-----------|-------|------------|
| Java | 17 | Bahasa pemrograman |
| Spring Boot | 3.5.14 | Framework backend |
| PostgreSQL | 16 | Database (via Docker) |
| Docker | latest | Container PostgreSQL |
| Gradle | Kotlin DSL | Build tool |

---

## Prasyarat

Pastikan sudah terinstall di komputer:

```bash
# Java 17
java -version
# harus muncul: openjdk version "17.x.x"

# Docker
docker --version
docker compose version
```

Jika belum install (Arch Linux):

```bash
sudo pacman -S jdk17-openjdk docker docker-compose
sudo archlinux-java set java-17-openjdk
sudo systemctl enable --now docker
sudo usermod -aG docker $USER
# logout dan login ulang setelah perintah ini
```

---

## Struktur Folder

```
Teknisio Backend/
│
├── docker-compose.yml              ← konfigurasi PostgreSQL
├── build.gradle.kts                ← dependencies project
├── settings.gradle.kts
│
└── src/
    ├── main/
    │   ├── java/com/teknisio/backend/
    │   │   │
    │   │   ├── config/             ← konfigurasi Spring (Security, WebSocket)
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── WebSocketConfig.java
    │   │   │
    │   │   ├── controller/         ← endpoint REST API
    │   │   │   ├── AuthController.java
    │   │   │   ├── ServiceController.java
    │   │   │   └── ChatController.java
    │   │   │
    │   │   ├── service/            ← logika bisnis
    │   │   │   ├── AuthService.java
    │   │   │   ├── ServiceRequestService.java
    │   │   │   └── ChatService.java
    │   │   │
    │   │   ├── repository/         ← akses database (JPA)
    │   │   │   ├── UserRepository.java
    │   │   │   ├── ServiceRequestRepository.java
    │   │   │   └── MessageRepository.java
    │   │   │
    │   │   ├── model/              ← entity / tabel database
    │   │   │   ├── User.java
    │   │   │   ├── ServiceRequest.java
    │   │   │   └── Message.java
    │   │   │
    │   │   └── dto/                ← object request & response API
    │   │       ├── LoginRequest.java
    │   │       ├── RegisterRequest.java
    │   │       └── ServiceRequestDto.java
    │   │
    │   └── resources/
    │       └── application.yml     ← konfigurasi aplikasi
    │
    └── test/
```

---

## Docker — PostgreSQL

### Apa itu docker-compose.yml di sini?

File `docker-compose.yml` digunakan untuk menjalankan **PostgreSQL di dalam container Docker**.
Tujuannya agar semua anggota tim tidak perlu install PostgreSQL manual di komputer masing-masing
— cukup jalankan satu perintah, database langsung siap.

### Isi docker-compose.yml

```yaml
services:
  postgres:
    image: postgres:16
    container_name: teknisio_db
    environment:
      POSTGRES_DB: teknisio_db
      POSTGRES_USER: teknisio_user
      POSTGRES_PASSWORD: teknisio_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data:
```

Penjelasan tiap bagian:

| Bagian | Keterangan |
|--------|------------|
| `image: postgres:16` | Menggunakan image PostgreSQL versi 16 dari Docker Hub |
| `container_name: teknisio_db` | Nama container di Docker |
| `POSTGRES_DB` | Nama database yang dibuat otomatis saat pertama jalan |
| `POSTGRES_USER` | Username untuk koneksi database |
| `POSTGRES_PASSWORD` | Password untuk koneksi database |
| `ports: 5432:5432` | Expose port 5432 container ke port 5432 komputer |
| `volumes` | Data PostgreSQL disimpan permanen, tidak hilang saat container restart |
| `restart: unless-stopped` | Container otomatis restart jika komputer restart |

---

### Perintah Docker

**Menjalankan PostgreSQL:**

```bash
docker compose up -d
```

**Memverifikasi PostgreSQL jalan:**

```bash
docker ps
# kolom PORTS harus muncul: 0.0.0.0:5432->5432/tcp
```

**Menghentikan PostgreSQL:**

```bash
docker compose down
```

**Melihat log PostgreSQL:**

```bash
docker logs teknisio_db
```

**Masuk ke dalam PostgreSQL:**

```bash
docker exec -it teknisio_db psql -U teknisio_user -d teknisio_db
```

**Reset database (hapus semua data):**

```bash
docker compose down -v
docker compose up -d
```

> **Perhatian:** `down -v` akan menghapus volume, artinya semua data di database ikut terhapus.
> Gunakan hanya jika ingin mulai dari awal.

---

### Troubleshooting Docker

**Error: permission denied to Docker socket**

```bash
sudo usermod -aG docker $USER
# logout dan login ulang dari sesi desktop
```

**Error: port 5432 already in use**

```bash
# PostgreSQL lokal mungkin masih jalan
sudo systemctl stop postgresql
docker compose up -d
```

**Error: container name already in use**

```bash
docker compose down
docker rm -f teknisio_db
docker compose up -d
```

---

## Konfigurasi application.yml

File ini berada di `src/main/resources/application.yml`.

```yaml
spring:
  application:
    name: teknisio-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/teknisio_db
    username: teknisio_user
    password: teknisio_pass
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  security:
    user:
      name: admin
      password: admin

server:
  port: 8080
```

Penjelasan konfigurasi penting:

| Key | Value | Keterangan |
|-----|-------|------------|
| `datasource.url` | `jdbc:postgresql://localhost:5432/teknisio_db` | Koneksi ke PostgreSQL di Docker |
| `datasource.username` | `teknisio_user` | Harus sama dengan `POSTGRES_USER` di docker-compose |
| `datasource.password` | `teknisio_pass` | Harus sama dengan `POSTGRES_PASSWORD` di docker-compose |
| `ddl-auto: update` | update | Hibernate otomatis buat/update tabel dari model Java |
| `show-sql: true` | true | Tampilkan query SQL di console (untuk debugging) |
| `server.port` | 8080 | Port Spring Boot berjalan |

---

## Menjalankan Backend

### Urutan yang benar

Selalu jalankan **Docker dulu**, baru Spring Boot:

```bash
# 1. Masuk ke folder backend
cd "Teknisio Backend"

# 2. Jalankan PostgreSQL
docker compose up -d

# 3. Verifikasi database jalan
docker ps
# pastikan kolom PORTS ada: 0.0.0.0:5432->5432/tcp

# 4. Jalankan Spring Boot
./gradlew bootRun
```

### Tanda berhasil

Di console IntelliJ atau terminal akan muncul:

```
Tomcat started on port 8080
Started BackendApplication in x.xxx seconds
HikariPool-1 - Start completed   ← koneksi ke PostgreSQL berhasil
```

### Menghentikan backend

Di terminal tekan `Ctrl + C`, lalu:

```bash
docker compose down
```

---

## API Endpoints

| Method | Endpoint | Keterangan |
|--------|----------|------------|
| POST | `/api/auth/register` | Registrasi pengguna |
| POST | `/api/auth/login` | Login pengguna |
| GET | `/api/services` | Daftar layanan servis |
| POST | `/api/services/request` | Buat permintaan layanan |
| GET | `/api/services/{id}/status` | Cek status layanan |
| WS | `/ws` | WebSocket untuk chat & status real-time |

---

## Cara Kerja Tim

Setiap anggota tim setelah clone repo:

```bash
# 1. Clone
git clone <url-repo>
cd "Teknisio Backend"

# 2. Jalankan database
docker compose up -d

# 3. Jalankan backend
./gradlew bootRun
```

Tidak perlu install PostgreSQL manual. Cukup Docker, semua langsung jalan.
