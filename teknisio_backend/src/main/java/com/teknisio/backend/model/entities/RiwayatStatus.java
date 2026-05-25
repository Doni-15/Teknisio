package com.teknisio.backend.model.entities;

import com.teknisio.backend.model.enums.RequestStatus;
import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "riwayat_status")
public class RiwayatStatus {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_riwayat")
  private UUID idRiwayat;

  @ManyToOne
  @JoinColumn(name = "id_permintaan", nullable = false)
  private PermintaanLayanan permintaan;

  @ManyToOne
  @JoinColumn(name = "diubah_oleh")
  private User diubahOleh;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_sebelum")
  private RequestStatus statusSebelum;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_sesudah", nullable = false)
  private RequestStatus statusSesudah;

  @Column(name = "catatan")
  private String catatan;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
