package com.teknisio.backend.model.entities;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "jenis_layanan")
public class JenisLayanan {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_layanan")
  private UUID idLayanan;

  // FK ke kategori dengan konsep many to one
  @ManyToOne
  @JoinColumn(name = "id_kategori", nullable = false)
  private KategoriLayanan idKategori;

  @Column(name = "nama_layanan", nullable = false, length = 100)
  private String namaLayanan;

  @Column(name = "deskripsi")
  private String deskripsi;

  @Column(name = "estimasi_menit", nullable = false)
  private int estimasiMenit;

  @Column(name = "aktif", nullable = false)
  private boolean aktif = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @CreationTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;
}
