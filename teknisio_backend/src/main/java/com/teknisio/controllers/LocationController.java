package com.teknisio.controllers;

import com.teknisio.common.response.ApiResponse;
import com.teknisio.dto.requests.LocationUpdateRequest;
import com.teknisio.dto.responses.LocationResponse;
import com.teknisio.services.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles GPS location sharing between technician and customer.
 *
 * <p><b>WebSocket / STOMP path (technician → server):</b><br>
 * {@code /app/location/update/{serviceRequestId}}
 *
 * <p><b>Broker topic (server → customer subscribers):</b><br>
 * {@code /topic/location/{serviceRequestId}}
 *
 * <p><b>REST polling fallback (customer → server):</b><br>
 * {@code GET /api/location/{serviceRequestId}}
 */
@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

  private final LocationService locationService;

  // ─────────────────────────────────────────────────────────────────────────
  // STOMP — technician pushes location
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Receives a GPS location update from the technician via STOMP.
   * Stores it in the cache and broadcasts it to {@code /topic/location/{serviceRequestId}}.
   *
   * <p>Android sends a STOMP SEND frame to {@code /app/location/update/{serviceRequestId}}
   * with a JSON body like:
   * <pre>
   * {
   *   "serviceRequestId": "uuid",
   *   "latitude": -6.200000,
   *   "longitude": 106.816666,
   *   "timestamp": 1717000000000
   * }
   * </pre>
   */
  @MessageMapping("/location/update/{serviceRequestId}")
  public void receiveLocationUpdate(
    @DestinationVariable String serviceRequestId,
    @Payload LocationUpdateRequest request
  ) {
    log.debug("STOMP location update received for {}: {},{}", serviceRequestId, request.latitude, request.longitude);
    request.serviceRequestId = serviceRequestId; // ensure consistency
    locationService.updateAndBroadcast(serviceRequestId, request);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // REST — customer polls last known location (WebSocket fallback)
  // ─────────────────────────────────────────────────────────────────────────

  /**
   * Returns the last known GPS location of the technician for the given
   * service request. Used as a polling fallback when WebSocket is unavailable.
   *
   * @param serviceRequestId UUID of the service request
   * @return last location or 404 if not yet received
   */
  @GetMapping("/{serviceRequestId}")
  public ResponseEntity<ApiResponse<LocationResponse>> getLastLocation(
    @PathVariable String serviceRequestId
  ) {
    LocationResponse location = locationService.getLastLocation(serviceRequestId);

    if (location == null) {
      return ResponseEntity.ok(
        new ApiResponse<>(false, "Lokasi teknisi belum tersedia", null, null)
      );
    }

    return ResponseEntity.ok(
      ApiResponse.success("Lokasi teknisi berhasil diambil", location)
    );
  }
}
