package com.teknisio.services;

import com.teknisio.common.exception.BadRequestException;
import com.teknisio.common.exception.ForbiddenException;
import com.teknisio.common.exception.ResourceNotFoundException;
import com.teknisio.dto.requests.LocationUpdateRequest;
import com.teknisio.dto.responses.LocationResponse;
import com.teknisio.model.entities.LokasiTeknisi;
import com.teknisio.model.entities.PermintaanLayanan;
import com.teknisio.model.location.TechnicianLocation;
import com.teknisio.repositories.LokasiTeknisiRepository;
import com.teknisio.repositories.PermintaanLayananRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LocationService {

  private final SimpMessagingTemplate messagingTemplate;
  private final LokasiTeknisiRepository lokasiRepo;
  private final PermintaanLayananRepository permintaanLayananRepository;
  private final WebSocketAuthorizationService webSocketAuthorizationService;

  private final ConcurrentHashMap<String, TechnicianLocation> locationCache = new ConcurrentHashMap<>();

  public LocationService(
    SimpMessagingTemplate messagingTemplate,
    LokasiTeknisiRepository lokasiRepo,
    PermintaanLayananRepository permintaanLayananRepository,
    WebSocketAuthorizationService webSocketAuthorizationService
  ) {
    this.messagingTemplate = messagingTemplate;
    this.lokasiRepo = lokasiRepo;
    this.permintaanLayananRepository = permintaanLayananRepository;
    this.webSocketAuthorizationService = webSocketAuthorizationService;
  }

  public void updateAndBroadcast(String serviceRequestId, LocationUpdateRequest request, UUID currentUserId) {
    if (serviceRequestId == null || serviceRequestId.isBlank()) {
      log.warn("LocationService: received update with null/blank serviceRequestId - ignored");
      return;
    }

    UUID requestId = parseServiceRequestId(serviceRequestId);

    if (!webSocketAuthorizationService.canSendLocation(requestId, currentUserId)) {
      throw new ForbiddenException("You are not allowed to update this location");
    }

    TechnicianLocation location = TechnicianLocation.of(serviceRequestId, request.latitude, request.longitude);
    locationCache.put(serviceRequestId, location);

    log.debug(
      "LocationService: broadcasting {},{} for request {}",
      request.latitude,
      request.longitude,
      serviceRequestId
    );

    LocationResponse response = toResponse(location);
    messagingTemplate.convertAndSend("/topic/location/" + serviceRequestId, response);

    persistAsync(serviceRequestId, request.latitude, request.longitude);
  }

  @Transactional(readOnly = true)
  public LocationResponse getLastLocation(String serviceRequestId, UUID currentUserId) {
    UUID requestId = parseServiceRequestId(serviceRequestId);
    validateLocationAccess(requestId, currentUserId);

    TechnicianLocation cached = locationCache.get(serviceRequestId);
    if (cached != null) {
      return toResponse(cached);
    }

    Optional<LokasiTeknisi> dbRow = lokasiRepo.findById(requestId);
    if (dbRow.isPresent()) {
      LokasiTeknisi row = dbRow.get();

      TechnicianLocation fromDb = TechnicianLocation.of(
        serviceRequestId,
        row.getLatitude().doubleValue(),
        row.getLongitude().doubleValue()
      );

      locationCache.put(serviceRequestId, fromDb);
      return toResponse(fromDb);
    }

    return null;
  }

  public void clearLocation(String serviceRequestId) {
    locationCache.remove(serviceRequestId);

    try {
      lokasiRepo.deleteById(UUID.fromString(serviceRequestId));
    } catch (Exception e) {
      log.warn("LocationService: failed to delete DB row for {}", serviceRequestId, e);
    }

    log.info("LocationService: cleared location for request {}", serviceRequestId);
  }

  private UUID parseServiceRequestId(String serviceRequestId) {
    try {
      return UUID.fromString(serviceRequestId);
    } catch (IllegalArgumentException e) {
      throw new BadRequestException("Invalid service request id");
    }
  }

  private void validateLocationAccess(UUID serviceRequestId, UUID currentUserId) {
    PermintaanLayanan permintaan = permintaanLayananRepository.findById(serviceRequestId)
      .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

    boolean isCustomer = permintaan.getPengguna() != null
      && permintaan.getPengguna().getIdUser().equals(currentUserId);

    boolean isTechnician = permintaan.getTeknisiProfile() != null
      && permintaan.getTeknisiProfile().getUser() != null
      && permintaan.getTeknisiProfile().getUser().getIdUser().equals(currentUserId);

    if (!isCustomer && !isTechnician) {
      throw new ForbiddenException("You are not allowed to access this location");
    }
  }

  @Async
  protected void persistAsync(String serviceRequestId, double latitude, double longitude) {
    try {
      UUID id = UUID.fromString(serviceRequestId);

      LokasiTeknisi entity = LokasiTeknisi.builder()
        .idPermintaan(id)
        .latitude(BigDecimal.valueOf(latitude))
        .longitude(BigDecimal.valueOf(longitude))
        .updatedAt(OffsetDateTime.now())
        .build();

      lokasiRepo.save(entity);
    } catch (Exception e) {
      log.warn("LocationService: async DB persist failed for {}: {}", serviceRequestId, e.getMessage());
    }
  }

  private LocationResponse toResponse(TechnicianLocation location) {
    return new LocationResponse(
      location.getServiceRequestId(),
      location.getLatitude(),
      location.getLongitude(),
      location.getTimestamp()
    );
  }
}
