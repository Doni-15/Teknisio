package com.teknisio.backend.model.entities;

import java.time.OffsetDateTime;
import java.util.*;
import org.hibernate.annotations.*;
import lombok.*;
import jakarta.persistence.*;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name = "log_aktivitas")
public class LogAktivitas {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_log")
  private UUID idLog;

  @ManyToOne
  @JoinColumn(name = "id_user")
  private User user;

  @Column(name = "aktivitas", nullable = false)
  private String aktivitas;

  @Column(name = "ip_address", length = 100)
  private String ipAddress;

  @Column(name = "user_agent")
  private String userAgent;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}
