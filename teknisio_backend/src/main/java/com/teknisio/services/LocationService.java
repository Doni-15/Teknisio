package com.teknisio.services;

import com.teknisio.dto.requests.LocationUpdateRequest;
import com.teknisio.dto.responses.LocationResponse;
import com.teknisio.model.entities.LokasiTeknisi;
import com.teknisio.model.location.TechnicianLocation;
import com.teknisio.repositories.LokasiTeknisiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages real-time GPS location sharing for the Teknisio tracking feature.
 *
 * <p>Architecture:
 * <ol>
 *   <li>Technician app sends location via STOMP to {@code /app/location/update/{serviceRequestId}}</li>
 *   <li>This service stores the location in-memory <em>and</em> persists it to {@code lokasi_teknisi}</li>
 *   <li>It broadcasts to {@code /topic/location/{serviceRequestId}} for live subscribers</li>
 *   <li>Customer app subscribes to that topic; falls back to polling {@code GET /api/location/{serviceRequestId}}</li>
 * </ol>
 */
@Slf4j
@Service
public class LocationService {

  private final SimpMessagingTemplate messagingTemplate;
  private final LokasiTeknisiRepository lokasiRepo;

  /**
   * In-memory location store: serviceRequestId → last known location.
   * Provides low-latency reads for the polling fallback within the same JVM.
   */
  private final ConcurrentHashMap<String, TechnicianLocation> locationCache = new ConcurrentHashMap<>();

  public LocationService(SimpMessagingTemplate messagingTemplate,
                         LokasiTeknisiRepository lokasiRepo) {
    this.messagingTemplate = messagingTemplate;
    this.lokasiRepo = lokasiRepo;
  }

  /**
   * Called by the STOMP message handler when the technician sends a location update.
   * <ol>
   *   <li>Updates the in-memory cache</li>
   *   <li>Broadcasts to the STOMP topic</li>
   *   <li>Persists asynchronously to the DB (best-effort)</li>
   * </ol>
   */
  public void updateAndBroadcast(String serviceRequestId, LocationUpdateRequest request) {
    if (serviceRequestId == null || serviceRequestId.isBlank()) {
      log.warn("LocationService: received update with null/blank serviceRequestId – ignored");
      return;
    }

    // 1. Update in-memory cache
    TechnicianLocation location = TechnicianLocation.of(serviceRequestId, request.latitude, request.longitude);
    locationCache.put(serviceRequestId, location);

    log.debug("LocationService: broadcasting {},{} for request {}", request.latitude, request.longitude, serviceRequestId);

    // 2. Broadcast to WebSocket subscribers
    LocationResponse response = toResponse(location);
    messagingTemplate.convertAndSend("/topic/location/" + serviceRequestId, response);

    // 3. Persist to DB asynchronously (does not block the WebSocket thread)
    persistAsync(serviceRequestId, request.latitude, request.longitude);
  }

  /**
   * Returns the last known location for the given service request.
   * Checks the in-memory cache first; falls back to the DB if the cache is cold
   * (e.g. after a server restart).
   *
   * @param serviceRequestId UUID of the service request
   * @return the last known location, or {@code null} if not yet received
   */
  public LocationResponse getLastLocation(String serviceRequestId) {
    // Check in-memory cache first
    TechnicianLocation cached = locationCache.get(serviceRequestId);
    if (cached != null) {
      return toResponse(cached);
    }

    // Fallback: DB lookup (cold start / server restart scenario)
    try {
      UUID id = UUID.fromString(serviceRequestId);
      Optional<LokasiTeknisi> dbRow = lokasiRepo.findById(id);
      if (dbRow.isPresent()) {
        LokasiTeknisi row = dbRow.get();
        TechnicianLocation fromDb = TechnicianLocation.of(
          serviceRequestId,
          row.getLatitude().doubleValue(),
          row.getLongitude().doubleValue()
        );
        // Warm the in-memory cache
        locationCache.put(serviceRequestId, fromDb);
        return toResponse(fromDb);
      }
    } catch (IllegalArgumentException e) {
      log.warn("LocationService: invalid UUID '{}'", serviceRequestId);
    } catch (Exception e) {
      log.error("LocationService: DB lookup failed for {}", serviceRequestId, e);
    }

    return null;
  }

  /**
   * Removes cached location for a service request (e.g. when order completes).
   */
  public void clearLocation(String serviceRequestId) {
    locationCache.remove(serviceRequestId);
    try {
      lokasiRepo.deleteById(UUID.fromString(serviceRequestId));
    } catch (Exception e) {
      log.warn("LocationService: failed to delete DB row for {}", serviceRequestId, e);
    }
    log.info("LocationService: cleared location for request {}", serviceRequestId);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Private helpers
  // ─────────────────────────────────────────────────────────────────────────

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
