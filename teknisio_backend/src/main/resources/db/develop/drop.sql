-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- DROP ALL TABLES
-- ============================================================

DROP TABLE IF EXISTS review CASCADE;

DROP TABLE IF EXISTS user_session CASCADE;

DROP TABLE IF EXISTS jadwal_teknisi CASCADE;

DROP TABLE IF EXISTS media_permintaan CASCADE;

DROP TABLE IF EXISTS log_aktivitas CASCADE;

DROP TABLE IF EXISTS riwayat_status CASCADE;

DROP TABLE IF EXISTS notifikasi CASCADE;

DROP TABLE IF EXISTS pesan CASCADE;

DROP TABLE IF EXISTS permintaan_layanan CASCADE;

DROP TABLE IF EXISTS jenis_layanan CASCADE;

DROP TABLE IF EXISTS kategori_layanan CASCADE;

DROP TABLE IF EXISTS teknisi_profile CASCADE;

DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- DROP ALL ENUM TYPES
-- ============================================================

DROP TYPE IF EXISTS day_enum CASCADE;

DROP TYPE IF EXISTS notification_reference_type CASCADE;

DROP TYPE IF EXISTS pesan_type CASCADE;

DROP TYPE IF EXISTS request_status CASCADE;

DROP TYPE IF EXISTS teknisi_status CASCADE;

DROP TYPE IF EXISTS user_status CASCADE;

DROP TYPE IF EXISTS user_role CASCADE;

-- ============================================================
-- DROP EXTENSIONS (OPTIONAL)
-- ============================================================

-- Uncomment jika benar-benar ingin hapus extension

-- DROP EXTENSION IF EXISTS "citext";
-- DROP EXTENSION IF EXISTS "pgcrypto";
