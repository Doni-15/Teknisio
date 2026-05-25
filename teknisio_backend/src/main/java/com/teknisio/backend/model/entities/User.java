package com.teknisio.backend.model.entities;

// Import enums
import com.teknisio.backend.model.enums.UserRole;
import com.teknisio.backend.model.enums.UserStatus;

// import package
import java.util.*;
import java.time.OffsetDateTime;
import lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id_user")
  private UUID idUser;

  @Column(name = "nama", nullable = false, length = 100)
  private String nama;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "no_telepon", nullable = false, unique = true, length = 20)
  private String noTelpon;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "foto_profil")
  private String fotoProfil;

  @Column(name = "alamat")
  private String alamat;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_akun", nullable = false)
  private UserStatus statusAkun;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "last_login")
  private OffsetDateTime lastLogin;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;
}
