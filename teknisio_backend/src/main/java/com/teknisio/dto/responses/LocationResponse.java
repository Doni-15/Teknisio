package com.teknisio.dto.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body returned by the REST polling endpoint
 * {@code GET /api/location/{serviceRequestId}}.
 */
public class LocationResponse {

  @JsonProperty("serviceRequestId")
  public String serviceRequestId;

  @JsonProperty("latitude")
  public double latitude;

  @JsonProperty("longitude")
  public double longitude;

  @JsonProperty("timestamp")
  public long timestamp;

  public LocationResponse() {}

  public LocationResponse(String serviceRequestId, double latitude, double longitude, long timestamp) {
    this.serviceRequestId = serviceRequestId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.timestamp = timestamp;
  }
}
