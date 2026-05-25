package com.teknisio.backend.model.entities;

import com.teknisio.backend.model.enums.HariEnum;
import java.time.*;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "jadwal_teknisi")
public class JadwalTeknisi {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_jadwal")
  private UUID idJadwal;

  @ManyToOne
  @JoinColumn(name = "id_teknisi_profile")
  private TeknisiProfile teknisiProfile;

  @Enumerated(EnumType.STRING)
  @Column(name = "hari", nullable = false)
  private HariEnum hari;

  @Column(name = "jam_mulai", nullable = false)
  private LocalTime jamMulai;

  @Column(name = "jam_selesai", nullable = false)
  private LocalTime jamSelesai;

  @Column(name = "aktif", nullable = false)
  private Boolean aktif;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
