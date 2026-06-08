package com.teknisio.controllers;

import com.teknisio.common.exception.BadRequestException;
import com.teknisio.common.exception.ResourceNotFoundException;
import com.teknisio.common.response.ApiResponse;
import com.teknisio.dto.location.LocationUpdateMessage;
import com.teknisio.model.entities.PermintaanLayanan;
import com.teknisio.model.enums.RequestStatus;
import com.teknisio.repositories.PermintaanLayananRepository;
import com.teknisio.security.CurrentUserService;
import com.teknisio.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

/**
 * Handles real-time GPS location sharing between technicians and customers.
 *
 * <p><b>WebSocket flow (via STOMP):</b>
 * <ol>
 *   <li>Technician sends location to {@code /app/location/update/{serviceRequestId}}</li>
 *   <li>Server stores it in-memory and forwards to {@code /topic/location/{serviceRequestId}}</li>
 *   <li>Customer (subscribed to the topic) receives the update in real-time</li>
 * </ol>
 * </p>
 *
 * <p><b>REST fallback:</b>
 * {@code GET /api/location/{serviceRequestId}} returns the last known location for polling.
 * </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
public class LocationController {

    private static final String LOCATION_TOPIC = "/topic/location/";

    private final SimpMessagingTemplate messagingTemplate;
    private final LocationService locationService;
    private final PermintaanLayananRepository permintaanLayananRepository;
    private final CurrentUserService currentUserService;

    // ─────────────────────────────────────────────────────────────────────────
    // WebSocket — Technician sends location
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Technician sends their GPS coordinates here.
     * The message is stored in memory and broadcast to all subscribers on the matching topic.
     *
     * @param serviceRequestId the service request UUID string
     * @param message          the location payload from the technician
     * @param principal        the authenticated WebSocket user (technician)
     */
    @MessageMapping("/location/update/{serviceRequestId}")
    public void receiveLocation(
            @DestinationVariable String serviceRequestId,
            @Payload LocationUpdateMessage message,
            Principal principal
    ) {
        // Ensure the message has the correct service request ID from the path variable
        message.setServiceRequestId(serviceRequestId);

        // Persist in-memory cache
        locationService.updateLocation(message);

        // Broadcast to customer subscribers
        messagingTemplate.convertAndSend(LOCATION_TOPIC + serviceRequestId, message);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REST — Polling fallback: get last known location
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the last known GPS location for a service request.
     * Used as a polling fallback when WebSocket is not available.
     *
     * <p>Both the customer and the technician involved in this service request
     * are authorized to call this endpoint.</p>
     *
     * @param serviceRequestId the UUID string of the service request
     * @return the last known location, or 404 if not yet available
     */
    @GetMapping("/{serviceRequestId}")
    public ResponseEntity<ApiResponse<LocationUpdateMessage>> getLastLocation(
            @PathVariable String serviceRequestId
    ) {
        UUID id = parseId(serviceRequestId);
        UUID currentUserId = currentUserService.getCurrentUserId();

        PermintaanLayanan order = permintaanLayananRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));

        // Authorization: only the customer or the assigned technician can view this
        boolean isCustomer = order.getPengguna() != null
                && currentUserId.equals(order.getPengguna().getIdUser());
        boolean isTechnician = order.getTeknisiProfile() != null
                && order.getTeknisiProfile().getUser() != null
                && currentUserId.equals(order.getTeknisiProfile().getUser().getIdUser());

        if (!isCustomer && !isTechnician) {
            throw new ResourceNotFoundException("Service request not found");
        }

        // Only expose live location for ACCEPTED (on the way) orders
        if (order.getStatus() != RequestStatus.ACCEPTED) {
            throw new BadRequestException("Location tracking is only available for orders with ACCEPTED status");
        }

        LocationUpdateMessage location = locationService.getLastLocation(serviceRequestId);

        if (location == null) {
            throw new ResourceNotFoundException("Technician location not yet available");
        }

        return ResponseEntity.ok(ApiResponse.success("Location retrieved successfully", location));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private UUID parseId(String id) {
        if (id == null || id.isBlank()) {
            throw new BadRequestException("Service request id is required");
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid service request id");
        }
    }
}
