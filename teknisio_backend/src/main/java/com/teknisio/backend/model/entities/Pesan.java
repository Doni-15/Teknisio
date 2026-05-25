package com.teknisio.backend.model.entities;

import com.teknisio.backend.model.enums.PesanType;
import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "pesan")
public class Pesan {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_pesan")
  private UUID idPesan;

  @ManyToOne
  @JoinColumn(name = "id_permintaan", nullable = false)
  private PermintaanLayanan permintaan;

  @ManyToOne
  @JoinColumn(name = "id_pengirim")
  private User pengirim;

  @Column(name = "isi_pesan")
  private String isiPesan;

  @Column(name = "file_url")
  private String fileUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipe_pesan", nullable = false)
  private PesanType tipePesan;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "read_at")
  private OffsetDateTime readAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;
}
