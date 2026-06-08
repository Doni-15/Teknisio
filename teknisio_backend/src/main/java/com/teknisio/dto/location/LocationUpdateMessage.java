package com.teknisio.dto.location;

import java.time.Instant;

/**
 * Message sent by a technician to broadcast their current GPS location.
 * Forwarded to subscribers on /topic/location/{serviceRequestId}.
 */
public class LocationUpdateMessage {

    private String serviceRequestId;
    private double latitude;
    private double longitude;
    private Instant timestamp;

    public LocationUpdateMessage() {
    }

    public LocationUpdateMessage(String serviceRequestId, double latitude, double longitude) {
        this.serviceRequestId = serviceRequestId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = Instant.now();
    }

    public String getServiceRequestId() {
        return serviceRequestId;
    }

    public void setServiceRequestId(String serviceRequestId) {
        this.serviceRequestId = serviceRequestId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
