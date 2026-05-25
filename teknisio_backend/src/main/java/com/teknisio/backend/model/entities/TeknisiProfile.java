package com.teknisio.backend.model.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.teknisio.backend.model.enums.TeknisiSpesialisasi;
import com.teknisio.backend.model.enums.TeknisiStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
@Entity
@Table(name = "teknisi_profile")
public class TeknisiProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_teknisi_profile")
  private UUID idTeknisiProfile;

  // FK ke id_user one to one
  @OneToOne
  @JoinColumn(name = "id_user", nullable = false, unique = true)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "spesialisasi", nullable = false)
  private TeknisiSpesialisasi spesialisasi;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_ketersediaan", nullable = false)
  private TeknisiStatus statusKetersediaan = TeknisiStatus.OFFLINE;

  @Column(name = "rating_avg", nullable = false, precision = 3, scale = 2)
  @DecimalMin("0.0")
  private BigDecimal ratingAvg = BigDecimal.ZERO;

  @Column(name = "rating_count", nullable = false)
  private int ratingCount;

  @Min(0)
  @Column(name = "total_pekerjaan", nullable = false)
  private int totalPekerjaan = 0;

  @Column(name = "deskripsi")
  private String deskripsi;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @CreationTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
