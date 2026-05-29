package com.teknisio.services;

import com.teknisio.common.exception.BadRequestException;
import com.teknisio.common.exception.ResourceNotFoundException;
import com.teknisio.common.exception.UnauthorizedException;
import com.teknisio.common.util.TextUtil;
import com.teknisio.dto.requests.CreateServiceRequestRequest;
import com.teknisio.dto.responses.DeviceCategoryResponse;
import com.teknisio.dto.responses.ServiceRequestResponse;
import com.teknisio.model.entities.KategoriLayanan;
import com.teknisio.model.entities.PermintaanLayanan;
import com.teknisio.model.entities.PermintaanLayananKategori;
import com.teknisio.model.entities.PermintaanLayananKategoriId;
import com.teknisio.model.entities.TeknisiKategoriLayanan;
import com.teknisio.model.entities.TeknisiProfile;
import com.teknisio.model.entities.User;
import com.teknisio.model.enums.RequestStatus;
import com.teknisio.model.enums.UserRole;
import com.teknisio.model.enums.UserStatus;
import com.teknisio.repositories.KategoriLayananRepository;
import com.teknisio.repositories.PermintaanLayananKategoriRepository;
import com.teknisio.repositories.PermintaanLayananRepository;
import com.teknisio.repositories.TeknisiKategoriLayananRepository;
import com.teknisio.repositories.TeknisiProfileRepository;
import com.teknisio.repositories.UserRepository;
import com.teknisio.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceRequestService {

  private final CurrentUserService currentUserService;
  private final UserRepository userRepository;
  private final TeknisiProfileRepository teknisiProfileRepository;
  private final KategoriLayananRepository kategoriLayananRepository;
  private final TeknisiKategoriLayananRepository teknisiKategoriLayananRepository;
  private final PermintaanLayananRepository permintaanLayananRepository;
  private final PermintaanLayananKategoriRepository permintaanLayananKategoriRepository;

  @Transactional
  public ServiceRequestResponse createServiceRequest(CreateServiceRequestRequest request) {
    User customer = getCurrentActiveCustomer();
    TeknisiProfile technicianProfile = getActiveTechnicianProfile(request.technicianProfileId());

    List<UUID> deviceCategoryIds = parseDeviceCategoryIds(request.deviceCategoryIds());
    List<KategoriLayanan> selectedCategories = getActiveDeviceCategories(deviceCategoryIds);

    validateTechnicianSupportsSelectedCategories(technicianProfile, selectedCategories);

    PermintaanLayanan serviceRequest = PermintaanLayanan.builder()
      .pengguna(customer)
      .teknisiProfile(technicianProfile)
      .alamat(TextUtil.trim(request.address()))
      .detailAlamat(TextUtil.trim(request.addressDetail()))
      .deskripsiMasalah(TextUtil.trim(request.issueDescription()))
      .status(RequestStatus.WAITING)
      .diubahOlehTerakhir(customer)
      .build();

    PermintaanLayanan savedServiceRequest = permintaanLayananRepository.saveAndFlush(serviceRequest);

    List<PermintaanLayananKategori> selectedCategoryEntities = selectedCategories.stream()
      .map(category -> PermintaanLayananKategori.builder()
        .id(new PermintaanLayananKategoriId(
          savedServiceRequest.getIdPermintaan(),
          category.getIdKategori()
        ))
        .permintaan(savedServiceRequest)
        .kategori(category)
        .build()
      )
      .toList();

    permintaanLayananKategoriRepository.saveAll(selectedCategoryEntities);

    return toResponse(savedServiceRequest, selectedCategories);
  }

  private User getCurrentActiveCustomer() {
    UUID currentUserId = currentUserService.getCurrentUserId();

    return userRepository.findByIdUserAndDeletedAtIsNull(currentUserId)
      .filter(user -> user.getStatusAkun() == UserStatus.ACTIVE)
      .filter(user -> user.getRole() == UserRole.CUSTOMER)
      .orElseThrow(() -> new UnauthorizedException("Unauthorized"));
  }

  private TeknisiProfile getActiveTechnicianProfile(String technicianProfileId) {
    UUID idTeknisiProfile = parseTechnicianProfileId(technicianProfileId);

    return teknisiProfileRepository.findById(idTeknisiProfile)
      .filter(this::isActiveTechnician)
      .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));
  }

  private boolean isActiveTechnician(TeknisiProfile technicianProfile) {
    return technicianProfile != null
      && technicianProfile.getUser() != null
      && technicianProfile.getUser().getDeletedAt() == null
      && technicianProfile.getUser().getStatusAkun() == UserStatus.ACTIVE
      && technicianProfile.getUser().getRole() == UserRole.TECHNICIAN;
  }

  private UUID parseTechnicianProfileId(String technicianProfileId) {
    try {
      return UUID.fromString(technicianProfileId);
    } catch (IllegalArgumentException exception) {
      throw new BadRequestException("Invalid technician profile id");
    }
  }

  private List<UUID> parseDeviceCategoryIds(List<String> rawDeviceCategoryIds) {
    List<UUID> parsedIds = new ArrayList<>();

    for (String rawDeviceCategoryId : rawDeviceCategoryIds) {
      try {
        parsedIds.add(UUID.fromString(rawDeviceCategoryId));
      } catch (IllegalArgumentException exception) {
        throw new BadRequestException("Invalid device category id");
      }
    }

    Set<UUID> uniqueIds = new LinkedHashSet<>(parsedIds);

    if (uniqueIds.size() != parsedIds.size()) {
      throw new BadRequestException("Device category ids must not contain duplicate values");
    }

    return new ArrayList<>(uniqueIds);
  }

  private List<KategoriLayanan> getActiveDeviceCategories(List<UUID> deviceCategoryIds) {
    List<KategoriLayanan> categories = new ArrayList<>();

    for (UUID deviceCategoryId : deviceCategoryIds) {
      KategoriLayanan category = kategoriLayananRepository
        .findByIdKategoriAndAktifTrueAndDeletedAtIsNull(deviceCategoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Device category not found"));

      categories.add(category);
    }

    return categories;
  }

  private void validateTechnicianSupportsSelectedCategories(
    TeknisiProfile technicianProfile,
    List<KategoriLayanan> selectedCategories
  ) {
    Set<UUID> supportedCategoryIds = teknisiKategoriLayananRepository
      .findByTeknisiProfile_IdTeknisiProfileAndAktifTrue(technicianProfile.getIdTeknisiProfile())
      .stream()
      .map(TeknisiKategoriLayanan::getKategori)
      .filter(category -> Boolean.TRUE.equals(category.getAktif()))
      .filter(category -> category.getDeletedAt() == null)
      .map(KategoriLayanan::getIdKategori)
      .collect(Collectors.toSet());

    for (KategoriLayanan selectedCategory : selectedCategories) {
      if (!supportedCategoryIds.contains(selectedCategory.getIdKategori())) {
        throw new BadRequestException(
          "Technician does not support selected device category: " + selectedCategory.getNamaKategori()
        );
      }
    }
  }

  private ServiceRequestResponse toResponse(
    PermintaanLayanan serviceRequest,
    List<KategoriLayanan> selectedCategories
  ) {
    return new ServiceRequestResponse(
      serviceRequest.getIdPermintaan(),
      serviceRequest.getKodePermintaan(),
      serviceRequest.getPengguna().getIdUser(),
      serviceRequest.getTeknisiProfile().getIdTeknisiProfile(),
      serviceRequest.getStatus(),
      serviceRequest.getDeskripsiMasalah(),
      serviceRequest.getAlamat(),
      serviceRequest.getDetailAlamat(),
      selectedCategories.stream()
        .map(this::toDeviceCategoryResponse)
        .toList(),
      serviceRequest.getWaktuPermintaan()
    );
  }

  private DeviceCategoryResponse toDeviceCategoryResponse(KategoriLayanan category) {
    return new DeviceCategoryResponse(
      category.getIdKategori(),
      category.getNamaKategori(),
      category.getIcon()
    );
  }
}
