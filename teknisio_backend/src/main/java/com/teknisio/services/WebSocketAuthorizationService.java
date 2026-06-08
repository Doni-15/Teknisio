package com.teknisio.services;

import com.teknisio.model.entities.PermintaanLayanan;
import com.teknisio.repositories.PermintaanLayananRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebSocketAuthorizationService {

  private final PermintaanLayananRepository permintaanLayananRepository;

  @Transactional(readOnly = true)
  public boolean canAccessServiceRequest(UUID serviceRequestId, UUID currentUserId) {
    return permintaanLayananRepository.findById(serviceRequestId)
      .map(permintaan -> isCustomer(permintaan, currentUserId) || isAssignedTechnician(permintaan, currentUserId))
      .orElse(false);
  }

  @Transactional(readOnly = true)
  public boolean canSendLocation(UUID serviceRequestId, UUID currentUserId) {
    return permintaanLayananRepository.findById(serviceRequestId)
      .map(permintaan -> isAssignedTechnician(permintaan, currentUserId))
      .orElse(false);
  }

  private boolean isCustomer(PermintaanLayanan permintaan, UUID currentUserId) {
    return permintaan.getPengguna() != null
      && permintaan.getPengguna().getIdUser().equals(currentUserId);
  }

  private boolean isAssignedTechnician(PermintaanLayanan permintaan, UUID currentUserId) {
    return permintaan.getTeknisiProfile() != null
      && permintaan.getTeknisiProfile().getUser() != null
      && permintaan.getTeknisiProfile().getUser().getIdUser().equals(currentUserId);
  }
}
