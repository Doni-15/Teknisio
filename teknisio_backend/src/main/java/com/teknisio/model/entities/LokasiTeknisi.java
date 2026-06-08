package com.teknisio.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Persistent record of the last known GPS position reported by a technician
 * for a specific service request.
 *
 * <p>One row per service request; upserted on every location update.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "lokasi_teknisi")
public class LokasiTeknisi {

  /**
   * The UUID of the service request (matches {@code permintaan_layanan.id_permintaan}).
   * This is both the primary key and the FK.
   */
  @Id
  @Column(name = "id_permintaan", nullable = false, updatable = false)
  private UUID idPermintaan;

  @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
