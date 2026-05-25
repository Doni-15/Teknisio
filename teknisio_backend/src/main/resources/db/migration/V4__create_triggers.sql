-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- Function
-- ============================================================

-- AUTO UPDATE updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- TRIGGER: users.updated_at
-- Otomatis mengubah updated_at
-- setiap data user di-update
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: teknisi_profile.updated_at
-- Otomatis mengubah updated_at
-- setiap data profil teknisi di-update
CREATE TRIGGER trg_teknisi_profile_updated_at
BEFORE UPDATE ON teknisi_profile
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: kategori_layanan.updated_at
-- Otomatis mengubah updated_at
-- setiap kategori layanan di-update
CREATE TRIGGER trg_kategori_layanan_updated_at
BEFORE UPDATE ON kategori_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: jenis_layanan.updated_at
-- Otomatis mengubah updated_at
-- setiap jenis layanan di-update
CREATE TRIGGER trg_jenis_layanan_updated_at
BEFORE UPDATE ON jenis_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: permintaan_layanan.updated_at
-- Otomatis mengubah updated_at
-- setiap permintaan layanan di-update
CREATE TRIGGER trg_permintaan_layanan_updated_at
BEFORE UPDATE ON permintaan_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: jadwal_teknisi.updated_at
-- Otomatis mengubah updated_at
-- setiap jadwal teknisi di-update
CREATE TRIGGER trg_jadwal_teknisi_updated_at
BEFORE UPDATE ON jadwal_teknisi
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: user_session.updated_at
-- Otomatis mengubah updated_at
-- setiap session user di-update
CREATE TRIGGER trg_user_session_updated_at
BEFORE UPDATE ON user_session
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- TRIGGER: review.updated_at
-- Otomatis mengubah updated_at
-- setiap review di-update
CREATE TRIGGER trg_review_updated_at
BEFORE UPDATE ON review
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();
