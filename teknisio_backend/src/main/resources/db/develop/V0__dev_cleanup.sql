-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
--  Teknisio Database Cleanup for Development
--  DBMS   : PostgreSQL >= 13
--  NOTE   : DEV ONLY. Jangan jalankan di production.
--
--  Tujuan:
--  1. Membersihkan schema Teknisio saat development.
--  2. Aman dijalankan berulang karena memakai IF EXISTS.
--  3. Menghapus object dari versi lama dan versi terbaru.
-- ============================================================

-- ============================================================
-- DROP VIEWS
-- Drop view dulu supaya dependency ke table bersih.
-- ============================================================
DROP VIEW IF EXISTS v_permintaan_detail CASCADE;
DROP VIEW IF EXISTS v_teknisi_ringkasan CASCADE;

-- ============================================================
-- DROP TABLES
-- Urutan dari tabel paling dependen ke tabel induk.
-- CASCADE dipakai agar constraint/trigger/index terkait ikut terhapus.
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
DROP TABLE IF EXISTS teknisi_layanan CASCADE;
DROP TABLE IF EXISTS jenis_layanan CASCADE;
DROP TABLE IF EXISTS kategori_layanan CASCADE;
DROP TABLE IF EXISTS teknisi_profile CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- DROP FUNCTIONS
-- Function tetap ada walaupun table sudah di-drop, jadi bersihkan juga.
-- ============================================================
DROP FUNCTION IF EXISTS trigger_refresh_teknisi_stats_from_permintaan() CASCADE;
DROP FUNCTION IF EXISTS trigger_refresh_teknisi_stats_from_review() CASCADE;
DROP FUNCTION IF EXISTS refresh_teknisi_stats(UUID) CASCADE;
DROP FUNCTION IF EXISTS ensure_review_valid() CASCADE;
DROP FUNCTION IF EXISTS ensure_pesan_sender_valid() CASCADE;
DROP FUNCTION IF EXISTS create_status_history() CASCADE;
DROP FUNCTION IF EXISTS validate_request_status_flow() CASCADE;
DROP FUNCTION IF EXISTS ensure_permintaan_layanan_valid() CASCADE;
DROP FUNCTION IF EXISTS ensure_teknisi_profile_role() CASCADE;
DROP FUNCTION IF EXISTS set_updated_at() CASCADE;

-- ============================================================
-- DROP ENUM TYPES
-- Mencakup enum versi terbaru dan enum lama yang mungkin pernah dipakai.
-- ============================================================
DROP TYPE IF EXISTS hari_enum CASCADE;
DROP TYPE IF EXISTS day_enum CASCADE;
DROP TYPE IF EXISTS notification_reference_type CASCADE;
DROP TYPE IF EXISTS pesan_type CASCADE;
DROP TYPE IF EXISTS request_status CASCADE;
DROP TYPE IF EXISTS teknisi_status CASCADE;
DROP TYPE IF EXISTS teknisi_spesialisasi CASCADE;
DROP TYPE IF EXISTS user_status CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;

-- ============================================================
-- DROP EXTENSIONS (OPTIONAL)
-- Biasanya JANGAN dihapus saat development karena bisa dipakai object lain.
-- Uncomment hanya kalau benar-benar ingin reset extension juga.
-- ============================================================
-- DROP EXTENSION IF EXISTS "pgcrypto" CASCADE;
-- DROP EXTENSION IF EXISTS "citext" CASCADE;
