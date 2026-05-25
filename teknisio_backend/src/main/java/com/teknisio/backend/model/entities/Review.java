package com.teknisio.backend.model.entities;

import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "review")
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_review")
  private UUID idReview;

  @OneToOne
  @JoinColumn(name = "id_permintaan", nullable = false, unique = true)
  private PermintaanLayanan permintaan;

  @ManyToOne
  @JoinColumn(name = "id_pengguna")
  private User pengguna;

  @ManyToOne
  @JoinColumn(name = "id_teknisi")
  private User teknisi;

  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "komentar")
  private String komentar;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
