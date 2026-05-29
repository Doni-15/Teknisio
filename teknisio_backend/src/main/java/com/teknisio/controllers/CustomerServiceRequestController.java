package com.teknisio.controllers;

import com.teknisio.common.response.ApiResponse;
import com.teknisio.dto.requests.CreateServiceRequestRequest;
import com.teknisio.dto.responses.ServiceRequestResponse;
import com.teknisio.services.CustomerServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers/service-requests")
public class CustomerServiceRequestController {

  private final CustomerServiceRequestService customerServiceRequestService;

  @PostMapping
  public ResponseEntity<ApiResponse<ServiceRequestResponse>> createServiceRequest(
    @Valid @RequestBody CreateServiceRequestRequest request
  ) {
    ServiceRequestResponse response = customerServiceRequestService.createServiceRequest(request);

    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(ApiResponse.success("Service request created successfully", response));
  }
}
