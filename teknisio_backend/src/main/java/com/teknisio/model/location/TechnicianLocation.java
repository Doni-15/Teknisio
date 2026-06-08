package com.teknisio.model.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * In-memory DTO representing the last known GPS location of a technician
 * for a given service request.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianLocation {

  /** UUID of the related service request. */
  private String serviceRequestId;

  private double latitude;
  private double longitude;

  /** Epoch-millis timestamp of the last update. */
  private long timestamp;

  public static TechnicianLocation of(String serviceRequestId, double lat, double lng) {
    return TechnicianLocation.builder()
      .serviceRequestId(serviceRequestId)
      .latitude(lat)
      .longitude(lng)
      .timestamp(Instant.now().toEpochMilli())
      .build();
  }
}
