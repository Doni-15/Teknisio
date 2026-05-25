-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db

-- ============================================================
-- INDEXES
-- ============================================================

-- Mempercepat pencarian user berdasarkan email
-- Digunakan saat login, validasi email, reset password
CREATE INDEX idx_users_email ON users (email);

-- Mempercepat pencarian user berdasarkan nomor telepon
CREATE INDEX idx_users_no_telepon ON users (no_telepon);

-- Mempercepat filter permintaan berdasarkan status
-- Contoh: WAITING, COMPLETED, dll
CREATE INDEX idx_permintaan_status ON permintaan_layanan (status);

-- Mempercepat pengambilan riwayat/order milik customer
CREATE INDEX idx_permintaan_pengguna ON permintaan_layanan (id_pengguna);

-- Mempercepat pengambilan pekerjaan milik teknisi
CREATE INDEX idx_permintaan_teknisi ON permintaan_layanan (id_teknisi);

-- Mempercepat load chat berdasarkan permintaan layanan
CREATE INDEX idx_pesan_permintaan ON pesan (id_permintaan);

-- Mempercepat pengambilan notifikasi user
CREATE INDEX idx_notifikasi_user ON notifikasi (id_user);

-- Mempercepat pencarian session milik user
CREATE INDEX idx_session_user ON user_session (id_user);

-- Mempercepat validasi refresh token saat auth
CREATE INDEX idx_session_token ON user_session (refresh_token_hash);
