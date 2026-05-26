-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- Teknisio Migration V2: Tables dan Foreign Keys
-- ============================================================

-- ============================================================
-- USERS
-- Semua aktor ada di sini: CUSTOMER, TEKNISI, ADMIN.
-- ============================================================
CREATE TABLE users (
  id_user             UUID PRIMARY KEY DEFAULT gen_random_uuid(),

  nama                VARCHAR(100) NOT NULL,
  email               VARCHAR(100) NOT NULL,
  no_telepon          VARCHAR(20) NOT NULL,
  password_hash       TEXT NOT NULL,
  foto_profil         TEXT,
  alamat              TEXT,

  role                user_role NOT NULL,
  status_akun         user_status NOT NULL DEFAULT 'ACTIVE',

  created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login          TIMESTAMPTZ,
  deleted_at          TIMESTAMPTZ,

  CONSTRAINT chk_users_email_format
    CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$'),

  CONSTRAINT chk_users_no_telepon_format
    CHECK (no_telepon ~ '^\+?[0-9]{10,15}$')
);

-- ============================================================
-- TEKNISI PROFILE
-- Data khusus teknisi. Satu user TEKNISI hanya boleh punya satu profil teknisi.
-- ============================================================
CREATE TABLE teknisi_profile (
  id_teknisi_profile    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_user               UUID NOT NULL UNIQUE,

  status_ketersediaan   teknisi_status NOT NULL DEFAULT 'OFFLINE',
  rating_avg            DECIMAL(3, 2) NOT NULL DEFAULT 0 CHECK (rating_avg BETWEEN 0 AND 5),
  rating_count          INTEGER NOT NULL DEFAULT 0 CHECK (rating_count >= 0),
  total_pekerjaan       INTEGER NOT NULL DEFAULT 0 CHECK (total_pekerjaan >= 0),
  deskripsi             TEXT,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_teknisi_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE CASCADE
);

-- ============================================================
-- KATEGORI LAYANAN
-- Contoh: AC, Kulkas, Mesin Cuci, TV, Kipas, Rice Cooker.
-- ============================================================
CREATE TABLE kategori_layanan (
  id_kategori           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nama_kategori         VARCHAR(100) NOT NULL,
  icon                  TEXT,
  aktif                 BOOLEAN NOT NULL DEFAULT TRUE,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at            TIMESTAMPTZ
);

-- ============================================================
-- JENIS LAYANAN
-- Contoh: Cuci AC, Service AC Tidak Dingin, Perbaikan Mesin Cuci.
-- ============================================================
CREATE TABLE jenis_layanan (
  id_layanan            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_kategori           UUID NOT NULL,

  nama_layanan          VARCHAR(100) NOT NULL,
  deskripsi             TEXT,
  estimasi_menit        INTEGER NOT NULL CHECK (estimasi_menit > 0),
  harga_min             NUMERIC(12, 2) CHECK (harga_min >= 0),
  harga_max             NUMERIC(12, 2) CHECK (harga_max >= 0),
  aktif                 BOOLEAN NOT NULL DEFAULT TRUE,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at            TIMESTAMPTZ,

  CONSTRAINT fk_jenis_layanan_kategori
    FOREIGN KEY (id_kategori)
    REFERENCES kategori_layanan (id_kategori)
    ON DELETE RESTRICT,

  CONSTRAINT chk_jenis_layanan_harga
    CHECK (harga_min IS NULL OR harga_max IS NULL OR harga_max >= harga_min)
);

-- ============================================================
-- TEKNISI LAYANAN
-- Pengganti ENUM TEKNISI_SPESIALISASI.
-- Satu teknisi bisa menangani banyak jenis layanan.
-- Satu jenis layanan bisa ditangani banyak teknisi.
-- ============================================================
CREATE TABLE teknisi_layanan (
  id_teknisi_profile    UUID NOT NULL,
  id_layanan            UUID NOT NULL,
  aktif                 BOOLEAN NOT NULL DEFAULT TRUE,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id_teknisi_profile, id_layanan),

  CONSTRAINT fk_teknisi_layanan_teknisi
    FOREIGN KEY (id_teknisi_profile)
    REFERENCES teknisi_profile (id_teknisi_profile)
    ON DELETE CASCADE,

  CONSTRAINT fk_teknisi_layanan_layanan
    FOREIGN KEY (id_layanan)
    REFERENCES jenis_layanan (id_layanan)
    ON DELETE CASCADE
);

-- ============================================================
-- PERMINTAAN LAYANAN
-- Order utama dari customer ke teknisi.
-- id_teknisi_profile dibuat nullable karena saat WAITING teknisi belum tentu dipilih/diterima.
-- ============================================================
CREATE TABLE permintaan_layanan (
  id_permintaan           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  kode_permintaan         VARCHAR(50) NOT NULL UNIQUE DEFAULT (
    'TK-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD') || '-' || UPPER(SUBSTRING(REPLACE(gen_random_uuid()::TEXT, '-', '') FROM 1 FOR 8))
  ),

  id_pengguna             UUID NOT NULL,
  id_teknisi_profile      UUID,
  id_layanan              UUID NOT NULL,

  latitude                DECIMAL(10, 7) NOT NULL,
  longitude               DECIMAL(10, 7) NOT NULL,
  alamat                  TEXT NOT NULL,
  detail_alamat           TEXT,
  deskripsi_masalah       TEXT NOT NULL,

  status                  request_status NOT NULL DEFAULT 'WAITING',
  estimasi_biaya          NUMERIC(12, 2) CHECK (estimasi_biaya >= 0),
  biaya_akhir             NUMERIC(12, 2) CHECK (biaya_akhir >= 0),
  catatan_teknisi         TEXT,
  alasan_batal            TEXT,

  waktu_permintaan        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  waktu_diterima          TIMESTAMPTZ,
  waktu_diproses          TIMESTAMPTZ,
  waktu_selesai           TIMESTAMPTZ,
  waktu_dibatalkan        TIMESTAMPTZ,

  diubah_oleh_terakhir    UUID,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_permintaan_pengguna
    FOREIGN KEY (id_pengguna)
    REFERENCES users (id_user)
    ON DELETE RESTRICT,

  CONSTRAINT fk_permintaan_teknisi_profile
    FOREIGN KEY (id_teknisi_profile)
    REFERENCES teknisi_profile (id_teknisi_profile)
    ON DELETE SET NULL,

  CONSTRAINT fk_permintaan_layanan
    FOREIGN KEY (id_layanan)
    REFERENCES jenis_layanan (id_layanan)
    ON DELETE RESTRICT,

  CONSTRAINT fk_permintaan_diubah_oleh
    FOREIGN KEY (diubah_oleh_terakhir)
    REFERENCES users (id_user)
    ON DELETE SET NULL,

  CONSTRAINT chk_permintaan_latitude
    CHECK (latitude BETWEEN -90 AND 90),

  CONSTRAINT chk_permintaan_longitude
    CHECK (longitude BETWEEN -180 AND 180),

  CONSTRAINT chk_permintaan_biaya
    CHECK (estimasi_biaya IS NULL OR biaya_akhir IS NULL OR biaya_akhir >= 0)
);

-- ============================================================
-- MEDIA PERMINTAAN
-- Foto/video pendukung kondisi perangkat saat membuat order.
-- ============================================================
CREATE TABLE media_permintaan (
  id_media                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_permintaan           UUID NOT NULL,
  url_file                TEXT NOT NULL,
  tipe_file               VARCHAR(50),
  mime_type               VARCHAR(100),
  ukuran_file             BIGINT CHECK (ukuran_file >= 0),

  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at              TIMESTAMPTZ,

  CONSTRAINT fk_media_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE
);

-- ============================================================
-- PESAN
-- Chat antara customer dan teknisi dalam konteks satu permintaan layanan.
-- ============================================================
CREATE TABLE pesan (
  id_pesan                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_permintaan             UUID NOT NULL,
  id_pengirim               UUID,

  isi_pesan                 TEXT,
  file_url                  TEXT,
  tipe_pesan                pesan_type NOT NULL,

  created_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at                   TIMESTAMPTZ,
  deleted_at                TIMESTAMPTZ,

  CONSTRAINT fk_pesan_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE,

  CONSTRAINT fk_pesan_pengirim
    FOREIGN KEY (id_pengirim)
    REFERENCES users (id_user)
    ON DELETE SET NULL,

  CONSTRAINT chk_pesan_content CHECK (
    (tipe_pesan = 'TEXT' AND isi_pesan IS NOT NULL) OR
    (tipe_pesan = 'IMAGE' AND file_url IS NOT NULL) OR
    (tipe_pesan = 'SYSTEM' AND isi_pesan IS NOT NULL)
  )
);

-- ============================================================
-- NOTIFIKASI
-- Untuk status order, chat, dan review.
-- ============================================================
CREATE TABLE notifikasi (
  id_notifikasi             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_user                   UUID NOT NULL,
  reference_id              UUID,

  judul                     VARCHAR(255) NOT NULL,
  isi                       TEXT NOT NULL,
  tipe                      VARCHAR(100) NOT NULL,
  reference_type            notification_reference_type,

  created_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at                   TIMESTAMPTZ,

  CONSTRAINT fk_notifikasi_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE CASCADE
);

-- ============================================================
-- RIWAYAT STATUS
-- Audit trail perubahan status permintaan layanan.
-- ============================================================
CREATE TABLE riwayat_status (
  id_riwayat                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_permintaan             UUID NOT NULL,
  diubah_oleh               UUID,

  status_sebelum            request_status,
  status_sesudah            request_status NOT NULL,
  catatan                   TEXT,
  created_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_riwayat_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE,

  CONSTRAINT fk_riwayat_pengubah
    FOREIGN KEY (diubah_oleh)
    REFERENCES users (id_user)
    ON DELETE SET NULL
);

-- ============================================================
-- LOG AKTIVITAS
-- Untuk audit: login, membuat order, update status, dll.
-- ============================================================
CREATE TABLE log_aktivitas (
  id_log                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_user                 UUID,

  aktivitas               TEXT NOT NULL,
  ip_address              VARCHAR(100),
  user_agent              TEXT,
  metadata                JSONB,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_log_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE SET NULL
);

-- ============================================================
-- JADWAL TEKNISI
-- Jadwal kerja teknisi per hari.
-- ============================================================
CREATE TABLE jadwal_teknisi (
  id_jadwal               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_teknisi_profile      UUID NOT NULL,
  hari                    hari_enum NOT NULL,
  jam_mulai               TIME NOT NULL,
  jam_selesai             TIME NOT NULL,
  aktif                   BOOLEAN NOT NULL DEFAULT TRUE,

  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT chk_jadwal_jam_valid
    CHECK (jam_selesai > jam_mulai),

  CONSTRAINT fk_jadwal_teknisi
    FOREIGN KEY (id_teknisi_profile)
    REFERENCES teknisi_profile (id_teknisi_profile)
    ON DELETE CASCADE,

  CONSTRAINT uq_jadwal_teknisi
    UNIQUE (id_teknisi_profile, hari, jam_mulai, jam_selesai)
);

-- ============================================================
-- USER SESSION
-- Mendukung refresh token dan auto logout.
-- ============================================================
CREATE TABLE user_session (
  id_session            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_user               UUID NOT NULL,
  refresh_token_hash    TEXT NOT NULL UNIQUE,
  expired_at            TIMESTAMPTZ NOT NULL,
  revoked_at            TIMESTAMPTZ,
  device_info           TEXT,
  ip_address            VARCHAR(100),

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_session_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE CASCADE
);

-- ============================================================
-- REVIEW
-- Satu permintaan hanya boleh memiliki satu review.
-- ============================================================
CREATE TABLE review (
  id_review             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  id_permintaan         UUID NOT NULL UNIQUE,
  id_pengguna           UUID NOT NULL,
  id_teknisi_profile    UUID NOT NULL,

  rating                INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
  komentar              TEXT,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_review_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE,

  CONSTRAINT fk_review_pengguna
    FOREIGN KEY (id_pengguna)
    REFERENCES users (id_user)
    ON DELETE RESTRICT,

  CONSTRAINT fk_review_teknisi_profile
    FOREIGN KEY (id_teknisi_profile)
    REFERENCES teknisi_profile (id_teknisi_profile)
    ON DELETE RESTRICT
);
