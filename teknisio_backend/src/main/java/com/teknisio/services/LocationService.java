package com.teknisio.services;

import com.teknisio.dto.location.LocationUpdateMessage;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory service for storing and retrieving the latest GPS location
 * of technicians per service request.
 *
 * <p>Data is intentionally not persisted to the database because:
 * <ul>
 *   <li>Location data is real-time and ephemeral</li>
 *   <li>We only need the current position, not the full history</li>
 *   <li>Reduces database write load significantly</li>
 * </ul>
 * </p>
 */
@Service
public class LocationService {

    /** Maps serviceRequestId → latest LocationUpdateMessage from the technician */
    private final ConcurrentHashMap<String, LocationUpdateMessage> latestLocations =
            new ConcurrentHashMap<>();

    /**
     * Stores the latest location for a given service request.
     *
     * @param message the incoming location update from the technician
     */
    public void updateLocation(LocationUpdateMessage message) {
        if (message == null || message.getServiceRequestId() == null) {
            return;
        }
        latestLocations.put(message.getServiceRequestId(), message);
    }

    /**
     * Returns the last known location for a service request, or null if not available.
     *
     * @param serviceRequestId the UUID string of the service request
     * @return the last known location, or null
     */
    public LocationUpdateMessage getLastLocation(String serviceRequestId) {
        if (serviceRequestId == null) {
            return null;
        }
        return latestLocations.get(serviceRequestId);
    }

    /**
     * Removes the stored location when the service request is completed or cancelled.
     *
     * @param serviceRequestId the UUID string of the service request
     */
    public void clearLocation(String serviceRequestId) {
        if (serviceRequestId != null) {
            latestLocations.remove(serviceRequestId);
        }
    }
}
