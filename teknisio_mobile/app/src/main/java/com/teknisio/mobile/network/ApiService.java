package com.teknisio.mobile.network;

import com.teknisio.mobile.model.request.LoginRequest;
import com.teknisio.mobile.model.request.RegisterCustomerRequest;
import com.teknisio.mobile.model.request.RegisterTechnicianRequest;
import com.teknisio.mobile.model.request.CreateServiceRequestRequest;
import com.teknisio.mobile.model.request.CancelServiceRequestRequest;
import com.teknisio.mobile.model.request.CompleteServiceRequestRequest;
import com.teknisio.mobile.model.request.RejectServiceRequestRequest;
import com.teknisio.mobile.model.request.AddTechnicianDeviceCategoryRequest;
import com.teknisio.mobile.model.request.CreateReviewRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.AuthResponse;
import com.teknisio.mobile.model.response.AuthUserResponse;
import com.teknisio.mobile.model.response.CustomerTechnicianResponse;
import com.teknisio.mobile.model.response.DeviceCategoryResponse;
import com.teknisio.mobile.model.response.ReviewResponse;
import com.teknisio.mobile.model.response.ServiceRequestResponse;
import com.teknisio.mobile.model.response.ChatMessageResponse;
import com.teknisio.mobile.model.response.LocationResponse;
import com.teknisio.mobile.model.response.StatusHistoryResponse;

import java.util.List;
import java.util.Map;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
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

    @GET("api/customers/technicians/{technicianProfileId}")
    Call<ApiResponse<CustomerTechnicianResponse>> getTechnicianDetail(
            @Path("technicianProfileId") String technicianProfileId
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
    @GET("api/customers/service-requests")
    Call<ApiResponse<List<ServiceRequestResponse>>> getMyServiceRequests(
            @Query("status") String status
    );

    @GET("api/customers/service-requests/{serviceRequestId}")
    Call<ApiResponse<ServiceRequestResponse>> getMyServiceRequestDetail(
            @Path("serviceRequestId") String serviceRequestId
    );

    @PATCH("api/customers/service-requests/{serviceRequestId}/cancel")
    Call<ApiResponse<ServiceRequestResponse>> cancelMyServiceRequest(
            @Path("serviceRequestId") String serviceRequestId,
            @Body CancelServiceRequestRequest request
    );


    @GET("api/technicians/service-requests")
    Call<ApiResponse<List<ServiceRequestResponse>>> getTechnicianServiceRequests(
            @Query("status") String status,
            @Query("sort") String sort
    );

    @GET("api/technicians/service-requests/{serviceRequestId}")
    Call<ApiResponse<ServiceRequestResponse>> getTechnicianServiceRequestDetail(
            @Path("serviceRequestId") String serviceRequestId
    );


    @GET("api/technicians/device-categories")
    Call<ApiResponse<List<DeviceCategoryResponse>>> getTechnicianDeviceCategories();

    @POST("api/technicians/device-categories")
    Call<ApiResponse<DeviceCategoryResponse>> addTechnicianDeviceCategory(
            @Body AddTechnicianDeviceCategoryRequest request
    );

    @DELETE("api/technicians/device-categories/{deviceCategoryId}")
    Call<ApiResponse<Object>> deleteTechnicianDeviceCategory(
            @Path("deviceCategoryId") String deviceCategoryId
    );


    @PATCH("api/technicians/service-requests/{serviceRequestId}/accept")
    Call<ApiResponse<ServiceRequestResponse>> acceptTechnicianServiceRequest(
            @Path("serviceRequestId") String serviceRequestId
    );

    @PATCH("api/technicians/service-requests/{serviceRequestId}/reject")
    Call<ApiResponse<ServiceRequestResponse>> rejectTechnicianServiceRequest(
            @Path("serviceRequestId") String serviceRequestId,
            @Body RejectServiceRequestRequest request
    );

    @PATCH("api/technicians/service-requests/{serviceRequestId}/start")
    Call<ApiResponse<ServiceRequestResponse>> startTechnicianServiceRequest(
            @Path("serviceRequestId") String serviceRequestId
    );

    @PATCH("api/technicians/service-requests/{serviceRequestId}/complete")
    Call<ApiResponse<ServiceRequestResponse>> completeTechnicianServiceRequest(
            @Path("serviceRequestId") String serviceRequestId,
            @Body CompleteServiceRequestRequest request
    );

    @GET("api/customers/service-requests/{serviceRequestId}/status-history")
    Call<ApiResponse<List<StatusHistoryResponse>>> getMyServiceRequestStatusHistory(
            @Path("serviceRequestId") String serviceRequestId
    );

    @GET("api/technicians/service-requests/{serviceRequestId}/status-history")
    Call<ApiResponse<List<StatusHistoryResponse>>> getTechnicianServiceRequestStatusHistory(
            @Path("serviceRequestId") String serviceRequestId
    );

    @POST("api/customers/service-requests/{serviceRequestId}/review")
    Call<ApiResponse<ReviewResponse>> createReview(
            @Path("serviceRequestId") String serviceRequestId,
            @Body CreateReviewRequest request
    );

    @PUT("api/users/me")
    Call<ApiResponse<AuthUserResponse>> updateProfile(@Body Map<String, String> request);

    // GPS Location — polling fallback
    @GET("api/location/{serviceRequestId}")
    Call<ApiResponse<LocationResponse>> getLastTechnicianLocation(
            @Path("serviceRequestId") String serviceRequestId
    );

    // Chat endpoints
    @GET("api/chat/{serviceRequestId}")
    Call<ApiResponse<List<ChatMessageResponse>>> getChatHistory(
            @Path("serviceRequestId") String serviceRequestId
    );

    @PATCH("api/chat/{serviceRequestId}/read")
    Call<ApiResponse<Void>> markChatAsRead(
            @Path("serviceRequestId") String serviceRequestId
    );

    @GET("api/chat/unread-count")
    Call<ApiResponse<Long>> getUnreadCount();
}
