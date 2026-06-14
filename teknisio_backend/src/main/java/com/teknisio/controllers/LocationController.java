package com.teknisio.controllers;

import com.teknisio.common.response.ApiResponse;
import com.teknisio.dto.requests.LocationUpdateRequest;
import com.teknisio.dto.responses.LocationResponse;
import com.teknisio.security.CustomUserDetails;
import com.teknisio.services.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

  private final LocationService locationService;

  @MessageMapping("/location/update/{serviceRequestId}")
  public void receiveLocationUpdate(
    @DestinationVariable String serviceRequestId,
    @Payload LocationUpdateRequest request,
    Principal principal
  ) {
    if (!(principal instanceof UsernamePasswordAuthenticationToken authentication)
      || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
      log.error("STOMP location update rejected: user not authenticated");
      throw new IllegalStateException("User not authenticated");
    }

    log.debug("STOMP location update received for {}: {},{}", serviceRequestId, request.latitude, request.longitude);
    request.serviceRequestId = serviceRequestId;
    locationService.updateAndBroadcast(serviceRequestId, request, userDetails.getIdUser());
  }

  @GetMapping("/{serviceRequestId}")
  public ResponseEntity<ApiResponse<LocationResponse>> getLastLocation(
    @PathVariable String serviceRequestId,
    @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID currentUserId = userDetails.getIdUser();
    LocationResponse location = locationService.getLastLocation(serviceRequestId, currentUserId);

    if (location == null) {
      return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(false, "Lokasi teknisi belum tersedia", null, null));
    }

    return ResponseEntity.ok(
      ApiResponse.success("Lokasi teknisi berhasil diambil", location)
    );
  }
}
