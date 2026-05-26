-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- Teknisio Migration V3: Indexes Minimal untuk Development
-- ============================================================

-- ============================================================
-- UNIQUE INDEXES
-- Dipakai untuk menjaga data tidak duplikat.
-- ============================================================

-- Email unik, case-insensitive, dan tetap aman untuk soft delete.
CREATE UNIQUE INDEX uq_users_email_active
  ON users (LOWER(email))
  WHERE deleted_at IS NULL;

-- Nomor telepon unik untuk user aktif/tidak terhapus.
CREATE UNIQUE INDEX uq_users_no_telepon_active
  ON users (no_telepon)
  WHERE deleted_at IS NULL;

-- Nama kategori tidak boleh duplikat selama kategori belum di-soft-delete.
CREATE UNIQUE INDEX uq_kategori_layanan_nama_active
  ON kategori_layanan (LOWER(nama_kategori))
  WHERE deleted_at IS NULL;

-- Nama layanan tidak boleh duplikat dalam kategori yang sama.
CREATE UNIQUE INDEX uq_jenis_layanan_per_kategori_active
  ON jenis_layanan (id_kategori, LOWER(nama_layanan))
  WHERE deleted_at IS NULL;

-- ============================================================
-- PERFORMANCE INDEXES INTI
-- Index ini tidak dipanggil langsung dari aplikasi.
-- PostgreSQL akan memakainya otomatis saat query cocok.
-- ============================================================

-- Untuk mengambil daftar layanan per kategori.
CREATE INDEX idx_jenis_layanan_kategori
  ON jenis_layanan (id_kategori);

-- Untuk mencari layanan yang bisa ditangani teknisi tertentu / sebaliknya.
CREATE INDEX idx_teknisi_layanan_layanan
  ON teknisi_layanan (id_layanan);

-- Untuk riwayat order customer.
CREATE INDEX idx_permintaan_pengguna
  ON permintaan_layanan (id_pengguna);

-- Untuk daftar pekerjaan teknisi.
CREATE INDEX idx_permintaan_teknisi_profile
  ON permintaan_layanan (id_teknisi_profile);

-- Untuk filter order berdasarkan status: WAITING, ACCEPTED, ON_PROGRESS, dll.
CREATE INDEX idx_permintaan_status
  ON permintaan_layanan (status);

-- Untuk urutan order terbaru.
CREATE INDEX idx_permintaan_waktu
  ON permintaan_layanan (waktu_permintaan DESC);

-- Untuk load chat berdasarkan permintaan layanan.
CREATE INDEX idx_pesan_permintaan_created
  ON pesan (id_permintaan, created_at ASC);

-- Untuk load notifikasi user terbaru.
CREATE INDEX idx_notifikasi_user_created
  ON notifikasi (id_user, created_at DESC);

-- Untuk melihat notifikasi yang belum dibaca.
CREATE INDEX idx_notifikasi_unread
  ON notifikasi (id_user)
  WHERE read_at IS NULL;

-- Untuk menampilkan riwayat perubahan status order.
CREATE INDEX idx_riwayat_status_permintaan
  ON riwayat_status (id_permintaan, created_at ASC);

-- Untuk pencarian session user saat auth.
CREATE INDEX idx_session_user
  ON user_session (id_user);

-- Untuk review teknisi.
CREATE INDEX idx_review_teknisi
  ON review (id_teknisi_profile, created_at DESC);
