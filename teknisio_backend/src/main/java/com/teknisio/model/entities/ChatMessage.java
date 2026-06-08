package com.teknisio.model.entities;

import com.teknisio.model.entities.base.BaseCreatedAtEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_message")
public class ChatMessage extends BaseCreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_chat", nullable = false, updatable = false)
  private UUID idChat;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_permintaan", nullable = false)
  private PermintaanLayanan permintaanLayanan;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_pengirim", nullable = false)
  private User pengirim;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_penerima", nullable = false)
  private User penerima;

  @Column(name = "pesan", nullable = false, columnDefinition = "TEXT")
  private String pesan;

  @Column(name = "dibaca", nullable = false)
  @Builder.Default
  private Boolean dibaca = false;

  @Column(name = "waktu_kirim", nullable = false)
  private OffsetDateTime waktuKirim;

  @PrePersist
  void prePersist() {
    if (waktuKirim == null) {
      waktuKirim = OffsetDateTime.now();
    }
  }
}