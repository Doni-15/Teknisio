package com.teknisio.mobile.network;

import com.teknisio.mobile.model.request.LoginRequest;
import com.teknisio.mobile.model.request.RegisterCustomerRequest;
import com.teknisio.mobile.model.request.RegisterTechnicianRequest;
import com.teknisio.mobile.model.request.CreateServiceRequestRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.AuthResponse;
import com.teknisio.mobile.model.response.AuthUserResponse;
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/device-categories")
    Call<ApiResponse<List<DeviceCategoryResponse>>> getDeviceCategories();

    @GET("api/customers/technicians")
    Call<ApiResponse<List<CustomerTechnicianResponse>>> searchTechnicians(
            @Query("deviceCategoryId") String deviceCategoryId,
            @Query("availabilityStatus") String availabilityStatus,
            @Query("sort") String sort
    );

    @POST("api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/auth/register/customer")
    Call<ApiResponse<AuthResponse>> registerCustomer(@Body RegisterCustomerRequest request);

    @POST("api/auth/register/technician")
    Call<ApiResponse<AuthResponse>> registerTechnician(@Body RegisterTechnicianRequest request);

    @GET("api/auth/profile")
    Call<ApiResponse<AuthUserResponse>> getProfile();

    @POST("api/customers/service-requests")
    Call<ApiResponse<ServiceRequestResponse>> createServiceRequest(
            @Body CreateServiceRequestRequest request
    );
}
