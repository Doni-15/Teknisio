-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db

-- ============================================================
--  Teknisio Database Schema
--  DBMS   : PostgreSQL >= 13
--  Version: 1.0
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- USERS
CREATE TABLE users (
  id_user             UUID PRIMARY KEY DEFAULT gen_random_uuid (),

  nama                VARCHAR(100) NOT NULL,
  email               VARCHAR(100) NOT NULL UNIQUE,
  no_telepon          VARCHAR(20) NOT NULL UNIQUE,
  password_hash       TEXT NOT NULL,
  foto_profil         TEXT,
  alamat              TEXT,

  role                USER_ROLE NOT NULL,
  status_akun         USER_STATUS NOT NULL DEFAULT 'ACTIVE',
  created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_login          TIMESTAMPTZ,
  deleted_at          TIMESTAMPTZ
);

-- TEKNISI PROFILE
CREATE TABLE teknisi_profile (
  id_teknisi_profile    UUID PRIMARY KEY DEFAULT gen_random_uuid (),

  id_user               UUID NOT NULL UNIQUE,
  spesialisasi          VARCHAR(100),
  status_ketersediaan   TEKNISI_STATUS NOT NULL DEFAULT 'OFFLINE',
  rating_avg            DECIMAL(3, 2) NOT NULL DEFAULT 0 CHECK (rating_avg >= 0),
  rating_count          INTEGER NOT NULL DEFAULT 0 CHECK (rating_count >= 0),
  total_pekerjaan       INTEGER NOT NULL DEFAULT 0,
  deskripsi             TEXT,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_teknisi_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE CASCADE
);

-- KATEGORI LAYANAN
CREATE TABLE kategori_layanan (
  id_kategori           UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  nama_kategori         VARCHAR(100) NOT NULL,
  icon                  TEXT,
  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- JENIS LAYANAN
CREATE TABLE jenis_layanan (
  id_layanan            UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_kategori           UUID NOT NULL,

  nama_layanan          VARCHAR(100) NOT NULL,
  deskripsi             TEXT,
  estimasi_menit        INTEGER NOT NULL,
  aktif                 BOOLEAN NOT NULL DEFAULT TRUE,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at            TIMESTAMPTZ,

  CONSTRAINT fk_jenis_layanan_kategori
    FOREIGN KEY (id_kategori)
    REFERENCES kategori_layanan (id_kategori)
    ON DELETE RESTRICT
);

-- PERMINTAAN LAYANAN
CREATE TABLE permintaan_layanan (
  id_permintaan           UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  kode_permintaan         VARCHAR(50) NOT NULL UNIQUE,
  id_pengguna             UUID NOT NULL,
  id_teknisi              UUID,
  id_layanan              UUID NOT NULL,

  latitude                DECIMAL(10, 7) NOT NULL,
  longitude               DECIMAL(10, 7) NOT NULL,
  alamat                  TEXT NOT NULL,
  detail_alamat           TEXT,
  deskripsi_masalah       TEXT NOT NULL,
  status                  REQUEST_STATUS NOT NULL DEFAULT 'WAITING',
  estimasi_biaya          NUMERIC(12, 2),
  catatan_teknisi         TEXT,
  waktu_permintaan        TIMESTAMPTZ,
  waktu_diterima          TIMESTAMPTZ,
  waktu_diproses          TIMESTAMPTZ,
  waktu_selesai           TIMESTAMPTZ,

  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_permintaan_pengguna
    FOREIGN KEY (id_pengguna)
    REFERENCES users (id_user)
    ON DELETE RESTRICT,

  CONSTRAINT fk_permintaan_teknisi
    FOREIGN KEY (id_teknisi)
    REFERENCES users (id_user)
    ON DELETE SET NULL,

  CONSTRAINT fk_permintaan_layanan
    FOREIGN KEY (id_layanan)
    REFERENCES jenis_layanan (id_layanan)
    ON DELETE RESTRICT
);

-- PESAN
CREATE TABLE pesan (
  id_pesan                  UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_permintaan             UUID NOT NULL,
  id_pengirim               UUID,

  isi_pesan                 TEXT,
  file_url                  TEXT,
  tipe_pesan                PESAN_TYPE NOT NULL,

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
    ON DELETE SET NULL
);

-- NOTIFIKASI
CREATE TABLE notifikasi (
  id_notifikasi             UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_user                   UUID NOT NULL,
  reference_id              UUID,

  judul                     VARCHAR(255) NOT NULL,
  isi                       TEXT NOT NULL,
  tipe                      VARCHAR(100) NOT NULL,
  reference_type            NOTIFICATION_REFERENCE_TYPE,

  created_at                TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at                   TIMESTAMPTZ,

  CONSTRAINT fk_notifikasi_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE CASCADE
);

-- RIWAYAT STATUS
CREATE TABLE riwayat_status (
  id_riwayat                UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_permintaan             UUID NOT NULL,
  diubah_oleh               UUID,

  status_sebelum            REQUEST_STATUS,
  status_sesudah            REQUEST_STATUS NOT NULL,
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

-- LOG AKTIVITAS
CREATE TABLE log_aktivitas (
  id_log                  UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_user                 UUID,

  aktivitas               TEXT NOT NULL,
  ip_address              VARCHAR(100),
  user_agent              TEXT,
  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_log_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE SET NULL
);

-- MEDIA PERMINTAAN
CREATE TABLE media_permintaan (
  id_media                UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_permintaan           UUID NOT NULL,
  url_file                TEXT NOT NULL,
  tipe_file               VARCHAR(50),
  mime_type               VARCHAR(100),
  ukuran_file             BIGINT,

  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at              TIMESTAMPTZ,

  CONSTRAINT fk_media_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE
);

-- JADWAL TEKNISI
CREATE TABLE jadwal_teknisi (
  id_jadwal               UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_teknisi              UUID NOT NULL,
  hari                    HARI_ENUM NOT NULL,
  jam_mulai               TIME NOT NULL,
  jam_selesai             TIME NOT NULL,
  aktif                   BOOLEAN NOT NULL DEFAULT TRUE,

  created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT chk_jam_valid
    CHECK (jam_selesai > jam_mulai),

  CONSTRAINT fk_jadwal_teknisi
    FOREIGN KEY (id_teknisi)
    REFERENCES teknisi_profile (id_teknisi_profile)
    ON DELETE CASCADE
);

-- USER SESSION
CREATE TABLE user_session (
  id_session            UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_user               UUID NOT NULL,
  refresh_token_hash    TEXT NOT NULL,
  expired_at            TIMESTAMPTZ NOT NULL,
  device_info           TEXT,
  ip_address            VARCHAR(100),

  created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_session_user
    FOREIGN KEY (id_user)
    REFERENCES users (id_user)
    ON DELETE CASCADE
);

-- REVIEW
CREATE TABLE review (
  id_review             UUID PRIMARY KEY DEFAULT gen_random_uuid (),
  id_permintaan         UUID NOT NULL UNIQUE,
  id_pengguna           UUID,
  id_teknisi            UUID,

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
    ON DELETE SET NULL,

  CONSTRAINT fk_review_teknisi
    FOREIGN KEY (id_teknisi)
    REFERENCES users (id_user)
    ON DELETE SET NULL
);
