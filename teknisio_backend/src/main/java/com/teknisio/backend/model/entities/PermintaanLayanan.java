package com.teknisio.backend.model.entities;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.teknisio.backend.model.enums.RequestStatus;
import com.teknisio.backend.utils.idGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "permintaan_layanan")
public class PermintaanLayanan {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_permintaan")
  private UUID idPermintaan;

  @Column(name = "kode_permintaan", nullable = false, unique = true, length = 50)
  private String kodePermintaan;

  @PrePersist
  public void PrePersist(){
    if (kodePermintaan == null) {
      this.kodePermintaan = idGenerator.kodePermintaan();
    }
  }

  // FK ke user dengan konsep banyak user ke satu perminataan layanan
  @ManyToOne
  @JoinColumn(name = "id_pengguna", nullable = false)
  private User idPengguna;

  // FK ke teknisi dengan konsep banyak teknisi ke satu perminataan layanan
  @ManyToOne
  @JoinColumn(name = "id_teknisi", nullable = false)
  private User idTeknisi;

  // FK ke jenis layanan dengan konsep banyak permintaan ke satu perminataan layanan
  @ManyToOne
  @JoinColumn(name = "id_layanan", nullable = false)
  private JenisLayanan idLayanan;

  @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "alamat", nullable = false)
  private String alamat;

  @Column(name = "detail_alamat")
  private String detailAlamat;

  @Column(name = "deskripsi_masalah", nullable = false)
  private String deskripsiMasalah;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RequestStatus status;

  @Column(name = "estimasi_biaya", precision = 12, scale = 2)
  private BigDecimal estimasiBiaya;

  @Column(name = "catatan_teknisi")
  private String catatanTeknisi;

  @Column(name = "waktu_permintaan")
  private OffsetDateTime waktuPermintaan;

  @Column(name = "waktu_diterima")
  private OffsetDateTime waktuDiterima;

  @Column(name = "waktu_diproses")
  private OffsetDateTime waktuDiproses;

  @Column(name = "waktu_selesai")
  private OffsetDateTime waktuSelesai;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @CreationTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
