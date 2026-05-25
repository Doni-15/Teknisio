package com.teknisio.backend.model.entities;

import com.teknisio.backend.model.enums.NotificationReferenceType;
import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "notifikasi")
public class Notifikasi {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_notifikasi")
  private UUID idNotifikasi;

  @ManyToOne
  @JoinColumn(name = "id_user", nullable = false)
  private User user;

  @Column(name = "reference_id")
  private UUID referenceId;

  @Column(name = "judul", nullable = false, length = 255)
  private String judul;

  @Column(name = "isi", nullable = false)
  private String isi;

  @Column(name = "tipe", nullable = false, length = 100)
  private String tipe;

  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type")
  private NotificationReferenceType referenceType;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "read_at")
  private OffsetDateTime readAt;
}
