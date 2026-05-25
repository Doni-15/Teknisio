package com.teknisio.backend.model.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "kategori_layanan")
public class KategoriLayanan {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_kategori")
  private UUID idKategori;

  @Column(name = "nama_kategori", nullable = false, length = 100)
  private String namaKategori;

  @Column(name = "icon")
  private String icon;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @CreationTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
