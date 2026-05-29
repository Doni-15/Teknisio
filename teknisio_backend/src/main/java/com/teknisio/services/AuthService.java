package com.teknisio.services;

import com.teknisio.common.exception.ConflictException;
import com.teknisio.common.exception.UnauthorizedException;
import com.teknisio.common.util.TextUtil;
import com.teknisio.dto.requests.LoginRequest;
import com.teknisio.dto.requests.RegisterCustomerRequest;
import com.teknisio.dto.requests.RegisterTechnicianRequest;
import com.teknisio.dto.responses.AuthResponse;
import com.teknisio.dto.responses.AuthUserResponse;
import com.teknisio.dto.responses.UserProfileResponse;
import com.teknisio.model.entities.TeknisiProfile;
import com.teknisio.model.entities.User;
import com.teknisio.model.enums.TeknisiStatus;
import com.teknisio.model.enums.UserRole;
import com.teknisio.model.enums.UserStatus;
import com.teknisio.repositories.TeknisiProfileRepository;
import com.teknisio.repositories.UserRepository;
import com.teknisio.security.CurrentUserService;
import com.teknisio.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final String TOKEN_TYPE = "Bearer";

  private final UserRepository userRepository;
  private final TeknisiProfileRepository teknisiProfileRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final CurrentUserService currentUserService;

  @Transactional
  public AuthResponse registerCustomer(RegisterCustomerRequest request) {
    String email = TextUtil.normalizeEmail(request.email());
    String phoneNumber = TextUtil.trim(request.phoneNumber());

    validateEmailAndPhoneUnique(email, phoneNumber);

    User user = User.builder()
      .nama(TextUtil.trim(request.name()))
      .email(email)
      .noTelepon(phoneNumber)
      .passwordHash(passwordEncoder.encode(request.password()))
      .alamat(TextUtil.trim(request.address()))
      .role(UserRole.CUSTOMER)
      .statusAkun(UserStatus.ACTIVE)
      .build();

    User savedUser = userRepository.save(user);

    return buildAuthResponse(savedUser, null);
  }

  @Transactional
  public AuthResponse registerTechnician(RegisterTechnicianRequest request) {
    String email = TextUtil.normalizeEmail(request.email());
    String phoneNumber = TextUtil.trim(request.phoneNumber());

    validateEmailAndPhoneUnique(email, phoneNumber);

    User user = User.builder()
      .nama(TextUtil.trim(request.name()))
      .email(email)
      .noTelepon(phoneNumber)
      .passwordHash(passwordEncoder.encode(request.password()))
      .alamat(TextUtil.trim(request.address()))
      .role(UserRole.TECHNICIAN)
      .statusAkun(UserStatus.ACTIVE)
      .build();

    User savedUser = userRepository.save(user);

    TeknisiProfile technicianProfile = TeknisiProfile.builder()
      .user(savedUser)
      .statusKetersediaan(TeknisiStatus.OFFLINE)
      .deskripsi(TextUtil.trim(request.description()))
      .build();

    TeknisiProfile savedTechnicianProfile = teknisiProfileRepository.save(technicianProfile);

    return buildAuthResponse(savedUser, savedTechnicianProfile.getIdTeknisiProfile());
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    String email = TextUtil.normalizeEmail(request.email());

    User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
      .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid email or password");
    }

    if (user.getStatusAkun() != UserStatus.ACTIVE) {
      throw new UnauthorizedException("Account is not active");
    }

    user.setLastLogin(OffsetDateTime.now());
    User savedUser = userRepository.save(user);

    UUID technicianProfileId = null;

    if (savedUser.getRole() == UserRole.TECHNICIAN) {
      technicianProfileId = teknisiProfileRepository.findByUser_IdUser(savedUser.getIdUser())
        .map(TeknisiProfile::getIdTeknisiProfile)
        .orElse(null);
    }

    return buildAuthResponse(savedUser, technicianProfileId);
  }

  @Transactional(readOnly = true)
  public UserProfileResponse getProfile() {
    UUID currentUserId = currentUserService.getCurrentUserId();

    User user = userRepository.findByIdUserAndDeletedAtIsNull(currentUserId)
      .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

    UUID technicianProfileId = null;

    if (user.getRole() == UserRole.TECHNICIAN) {
      technicianProfileId = teknisiProfileRepository.findByUser_IdUser(user.getIdUser())
        .map(TeknisiProfile::getIdTeknisiProfile)
        .orElse(null);
    }

    return new UserProfileResponse(
      user.getIdUser(),
      technicianProfileId,
      user.getNama(),
      user.getEmail(),
      user.getNoTelepon(),
      user.getFotoProfil(),
      user.getAlamat(),
      user.getRole(),
      user.getStatusAkun()
    );
  }

  private void validateEmailAndPhoneUnique(String email, String phoneNumber) {
    if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
      throw new ConflictException("Email is already registered");
    }

    if (userRepository.existsByNoTeleponAndDeletedAtIsNull(phoneNumber)) {
      throw new ConflictException("Phone number is already registered");
    }
  }

  private AuthResponse buildAuthResponse(User user, UUID technicianProfileId) {
    String accessToken = jwtService.generateAccessToken(user);

    AuthUserResponse userResponse = new AuthUserResponse(
      user.getIdUser(),
      technicianProfileId,
      user.getNama(),
      user.getEmail(),
      user.getNoTelepon(),
      user.getFotoProfil(),
      user.getAlamat(),
      user.getRole(),
      user.getStatusAkun()
    );

    return new AuthResponse(
      accessToken,
      TOKEN_TYPE,
      jwtService.getAccessTokenExpirationMs(),
      userResponse
    );
  }
}
