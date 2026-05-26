-- Active: 1779499726179@@127.0.0.1@5432@teknisio_db
-- ============================================================
-- Teknisio Migration V4: Functions dan Triggers Minimal untuk Development
-- ============================================================

-- ============================================================
-- FUNCTION: auto update updated_at
-- ============================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- FUNCTION: validasi alur status permintaan layanan
-- Alur valid:
-- WAITING -> ACCEPTED / REJECTED / CANCELLED
-- ACCEPTED -> ON_PROGRESS / CANCELLED
-- ON_PROGRESS -> COMPLETED / CANCELLED
-- COMPLETED, CANCELLED, REJECTED = final
-- ============================================================
CREATE OR REPLACE FUNCTION validate_request_status_flow()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    IF NEW.status <> 'WAITING' THEN
      RAISE EXCEPTION 'status awal permintaan_layanan harus WAITING';
    END IF;

    NEW.waktu_permintaan := COALESCE(NEW.waktu_permintaan, CURRENT_TIMESTAMP);
    RETURN NEW;
  END IF;

  IF NEW.status IS DISTINCT FROM OLD.status THEN
    IF OLD.status = 'WAITING' AND NEW.status NOT IN ('ACCEPTED', 'REJECTED', 'CANCELLED') THEN
      RAISE EXCEPTION 'transisi status tidak valid: WAITING hanya boleh ke ACCEPTED, REJECTED, atau CANCELLED';
    ELSIF OLD.status = 'ACCEPTED' AND NEW.status NOT IN ('ON_PROGRESS', 'CANCELLED') THEN
      RAISE EXCEPTION 'transisi status tidak valid: ACCEPTED hanya boleh ke ON_PROGRESS atau CANCELLED';
    ELSIF OLD.status = 'ON_PROGRESS' AND NEW.status NOT IN ('COMPLETED', 'CANCELLED') THEN
      RAISE EXCEPTION 'transisi status tidak valid: ON_PROGRESS hanya boleh ke COMPLETED atau CANCELLED';
    ELSIF OLD.status IN ('COMPLETED', 'CANCELLED', 'REJECTED') THEN
      RAISE EXCEPTION 'status % bersifat final dan tidak boleh diubah', OLD.status;
    END IF;

    IF NEW.status IN ('ACCEPTED', 'ON_PROGRESS', 'COMPLETED') AND NEW.id_teknisi_profile IS NULL THEN
      RAISE EXCEPTION 'status ACCEPTED/ON_PROGRESS/COMPLETED harus memiliki id_teknisi_profile';
    END IF;

    IF NEW.status = 'ACCEPTED' THEN
      NEW.waktu_diterima := COALESCE(NEW.waktu_diterima, CURRENT_TIMESTAMP);
    ELSIF NEW.status = 'ON_PROGRESS' THEN
      NEW.waktu_diproses := COALESCE(NEW.waktu_diproses, CURRENT_TIMESTAMP);
    ELSIF NEW.status = 'COMPLETED' THEN
      NEW.waktu_selesai := COALESCE(NEW.waktu_selesai, CURRENT_TIMESTAMP);
    ELSIF NEW.status IN ('CANCELLED', 'REJECTED') THEN
      NEW.waktu_dibatalkan := COALESCE(NEW.waktu_dibatalkan, CURRENT_TIMESTAMP);
    END IF;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- FUNCTION: simpan riwayat status otomatis
-- ============================================================
CREATE OR REPLACE FUNCTION create_status_history()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    INSERT INTO riwayat_status (
      id_permintaan,
      diubah_oleh,
      status_sebelum,
      status_sesudah,
      catatan
    ) VALUES (
      NEW.id_permintaan,
      NEW.diubah_oleh_terakhir,
      NULL,
      NEW.status,
      'Status awal permintaan dibuat'
    );
  ELSIF NEW.status IS DISTINCT FROM OLD.status THEN
    INSERT INTO riwayat_status (
      id_permintaan,
      diubah_oleh,
      status_sebelum,
      status_sesudah,
      catatan
    ) VALUES (
      NEW.id_permintaan,
      NEW.diubah_oleh_terakhir,
      OLD.status,
      NEW.status,
      'Status permintaan diperbarui'
    );
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- TRIGGERS: updated_at
-- ============================================================
CREATE TRIGGER trg_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_teknisi_profile_updated_at
BEFORE UPDATE ON teknisi_profile
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_kategori_layanan_updated_at
BEFORE UPDATE ON kategori_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_jenis_layanan_updated_at
BEFORE UPDATE ON jenis_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_teknisi_layanan_updated_at
BEFORE UPDATE ON teknisi_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_permintaan_layanan_updated_at
BEFORE UPDATE ON permintaan_layanan
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_jadwal_teknisi_updated_at
BEFORE UPDATE ON jadwal_teknisi
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_user_session_updated_at
BEFORE UPDATE ON user_session
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_review_updated_at
BEFORE UPDATE ON review
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- ============================================================
-- TRIGGERS: business rule status layanan
-- ============================================================
CREATE TRIGGER trg_permintaan_status_flow
BEFORE INSERT OR UPDATE OF status, id_teknisi_profile ON permintaan_layanan
FOR EACH ROW
EXECUTE FUNCTION validate_request_status_flow();

CREATE TRIGGER trg_status_history
AFTER INSERT OR UPDATE OF status ON permintaan_layanan
FOR EACH ROW
EXECUTE FUNCTION create_status_history();
