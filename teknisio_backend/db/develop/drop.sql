-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- DROP ALL TABLES
-- ============================================================

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
