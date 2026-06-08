package com.teknisio.dto.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body / STOMP payload sent by the technician app
 * when sharing their GPS position.
 */
public class LocationUpdateRequest {

  @JsonProperty("serviceRequestId")
  public String serviceRequestId;

  @JsonProperty("latitude")
  public double latitude;

  @JsonProperty("longitude")
  public double longitude;

  /** Optional client-side timestamp (epoch-millis). */
  @JsonProperty("timestamp")
  public Long timestamp;
}
