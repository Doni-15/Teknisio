# Teknisio Backend

Backend service untuk aplikasi Teknisio menggunakan:

- Java 17
- Spring Boot
- PostgreSQL
- Docker
- Flyway

> Beberapa contoh instalasi menggunakan Linux. Sesuaikan dengan OS masing-masing.

---

# Tech Stack

| Teknologi | Versi |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.14 |
| PostgreSQL | 16 |
| Docker | Latest |
| Gradle | Kotlin DSL |

---

# Yang Perlu Diinstall

## 1. Java 17

Cek apakah Java sudah terinstall:

```bash
java -version
```

Harus muncul kurang lebih:

```bash
openjdk version "17"
```

---

## 2. Docker

Cek Docker:

```bash
docker --version
docker compose version
```

---

## Install di Arch Linux

```bash
sudo pacman -S jdk17-openjdk docker docker-compose
```

Set Java 17:

```bash
sudo archlinux-java set java-17-openjdk
```

Aktifkan Docker:

```bash
sudo systemctl enable --now docker
```

Tambahkan user ke grup docker:

```bash
sudo usermod -aG docker $USER
```

Lalu logout/login ulang.

---

# Clone Project

```bash
git clone <url-repository>
cd "Teknisio Backend"
```

---

# Konfigurasi Environment

Buat file `.env` di root project:

```env
Udah kita kirim (this is secret brother)
```

File ini dipakai oleh Docker dan Spring Boot.

---

# Menjalankan PostgreSQL

Project menggunakan PostgreSQL di dalam Docker supaya semua anggota tim memakai environment database yang sama.

Jalankan:

```bash
docker compose up -d
```

Cek apakah container berjalan:

```bash
docker ps
```

Harus muncul container:

```bash
teknisio_db
```

---

# Menjalankan Backend

Setelah database jalan:

```bash
./gradlew bootRun
```

Jika berhasil akan muncul:

```bash
Tomcat started on port 8080
Started BackendApplication
```

Backend berjalan di:

```text
http://localhost:8080
```

---

# Struktur Folder

```text
src/main/java/com/teknisio/backend
│
├── config/        # konfigurasi Spring
├── controller/    # endpoint API
├── service/       # business logic
├── repository/    # akses database
├── model/         # entity JPA
└── dto/           # request & response object
```

---

# Database & Migration

Project menggunakan:

- JPA/Hibernate → mapping entity Java
- Flyway → migration database

Konfigurasi penting di `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${POSTGRES_DB}
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
```

Karena menggunakan:

```yaml
ddl-auto: validate
```

maka Hibernate tidak membuat tabel otomatis.

Semua tabel, relasi, dan foreign key dibuat melalui migration Flyway di:

```text
src/main/resources/db/migration
```

Contoh:

```text
V1__create_users.sql
V2__create_requests.sql
```

---

# Docker Commands

Menjalankan database:

```bash
docker compose up -d
```

Stop database:

```bash
docker compose down
```

Reset database:

```bash
docker compose down -v
docker compose up -d
```

> `down -v` akan menghapus semua data database.

Melihat log PostgreSQL:

```bash
docker logs teknisio_db
```

Masuk ke PostgreSQL:

```bash
docker exec -it teknisio_db psql -U teknisio_user -d teknisio_db
```

---

# Flow Menjalankan Project

```bash
# 1. clone project
git clone <repo>
cd "Teknisio Backend"

# 2. jalankan database
docker compose up -d

# 3. jalankan backend
./gradlew bootRun
```

---

# Endpoint Utama

| Method | Endpoint |
|---|---|
| POST | `/api/auth/register` |
| POST | `/api/auth/login` |
| GET | `/api/services` |
| POST | `/api/services/request` |
| WS | `/ws` |

---

# Troubleshooting

## Docker permission denied

```bash
sudo usermod -aG docker $USER
```

Logout/login ulang.

---

## Port 5432 sudah dipakai

Stop PostgreSQL lokal:

```bash
sudo systemctl stop postgresql
```

---

## Container sudah ada

```bash
docker compose down
docker rm -f teknisio_db
```

---

# Catatan

- Jalankan Docker dulu sebelum Spring Boot.
- Jangan edit migration lama yang sudah dipakai.
- Tambahkan migration baru jika ada perubahan schema database.
- Gunakan `.env` untuk credential database, jangan hardcode di source code.
