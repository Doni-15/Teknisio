package com.teknisio.backend.model.entities;

import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "media_permintaan")
public class MediaPermintaan {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_media")
  private UUID idMedia;

  @ManyToOne
  @JoinColumn(name = "id_permintaan", nullable = false)
  private PermintaanLayanan permintaan;

  @Column(name = "url_file", nullable = false)
  private String urlFile;

  @Column(name = "tipe_file", length = 50)
  private String tipeFile;

  @Column(name = "mime_type", length = 100)
  private String mimeType;

  @Column(name = "ukuran_file")
  private Long ukuranFile;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;
}
