package com.teknisio.backend.model.entities;

import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "user_session")
public class UserSession {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_session")
  private UUID idSession;

  @ManyToOne
  @JoinColumn(name = "id_user", nullable = false)
  private User user;

  @Column(name = "refresh_token_hash", nullable = false)
  private String refreshTokenHash;

  @Column(name = "expired_at", nullable = false)
  private OffsetDateTime expiredAt;

  @Column(name = "device_info")
  private String deviceInfo;

  @Column(name = "ip_address", length = 100)
  private String ipAddress;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
