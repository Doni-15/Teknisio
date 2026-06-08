-- ============================================================
-- Teknisio Migration V7: Technician GPS location cache table
-- ============================================================
-- Stores the last known GPS position of a technician per service request.
-- Used as a persistent fallback for the REST polling endpoint and
-- for resilience when the server restarts.
--
-- The Spring LocationService writes to this table on every STOMP update
-- and reads from it when serving GET /api/location/{serviceRequestId}.
-- ============================================================

CREATE TABLE lokasi_teknisi (
  id_permintaan  UUID PRIMARY KEY,

  latitude       DECIMAL(10, 7) NOT NULL
                   CONSTRAINT chk_lokasi_latitude CHECK (latitude BETWEEN -90 AND 90),

  longitude      DECIMAL(10, 7) NOT NULL
                   CONSTRAINT chk_lokasi_longitude CHECK (longitude BETWEEN -180 AND 180),

  updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_lokasi_teknisi_permintaan
    FOREIGN KEY (id_permintaan)
    REFERENCES permintaan_layanan (id_permintaan)
    ON DELETE CASCADE
);

-- Index for fast lookup by service request
CREATE INDEX IF NOT EXISTS idx_lokasi_teknisi_permintaan ON lokasi_teknisi (id_permintaan);
