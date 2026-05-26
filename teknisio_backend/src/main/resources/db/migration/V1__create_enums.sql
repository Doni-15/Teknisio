-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- Teknisio Migration V1: Extension dan ENUM
-- ============================================================

-- ============================================================
-- EXTENSION
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- ENUMS
-- ============================================================
CREATE TYPE user_role AS ENUM (
  'CUSTOMER',
  'TEKNISI',
  'ADMIN'
);

CREATE TYPE user_status AS ENUM (
  'ACTIVE',
  'INACTIVE',
  'BANNED',
  'SUSPENDED'
);

CREATE TYPE teknisi_status AS ENUM (
  'ONLINE',
  'OFFLINE',
  'BUSY',
  'ON_LEAVE'
);

CREATE TYPE request_status AS ENUM (
  'WAITING',
  'ACCEPTED',
  'ON_PROGRESS',
  'COMPLETED',
  'CANCELLED',
  'REJECTED'
);

CREATE TYPE pesan_type AS ENUM (
  'TEXT',
  'IMAGE',
  'SYSTEM'
);

CREATE TYPE notification_reference_type AS ENUM (
  'PERMINTAAN',
  'CHAT',
  'REVIEW'
);

CREATE TYPE hari_enum AS ENUM (
  'SENIN',
  'SELASA',
  'RABU',
  'KAMIS',
  'JUMAT',
  'SABTU',
  'MINGGU'
);
