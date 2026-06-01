package com.teknisio.mobile.controller;

import android.content.Context;

import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.request.LoginRequest;
import com.teknisio.mobile.model.request.RegisterCustomerRequest;
import com.teknisio.mobile.model.request.RegisterTechnicianRequest;
import com.teknisio.mobile.model.response.ApiResponse;
import com.teknisio.mobile.model.response.AuthResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.util.ErrorParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthController {

    public interface AuthCallback {
        void onSuccess(AuthResponse authResponse);
        void onError(String message);
    }

    private final Context context;
    private final TokenManager tokenManager;

    public AuthController(Context context) {
        this.context = context.getApplicationContext();
        this.tokenManager = new TokenManager(this.context);
    }

    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(
                safeTrim(email),
                safeValue(password)
        );

        ApiClient.getApiService(context)
                .login(request)
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AuthResponse>> call,
                            Response<ApiResponse<AuthResponse>> response
                    ) {
                        handleAuthResponse(response, "Login gagal.", callback);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                        callback.onError("Tidak bisa terhubung ke server: " + t.getMessage());
                    }
                });
    }

    public void registerCustomer(
            String name,
            String email,
            String phoneNumber,
            String password,
            String address,
            AuthCallback callback
    ) {
        RegisterCustomerRequest request = new RegisterCustomerRequest(
                safeTrim(name),
                safeTrim(email),
                safeTrim(phoneNumber),
                safeValue(password),
                safeTrim(address)
        );

        ApiClient.getApiService(context)
                .registerCustomer(request)
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AuthResponse>> call,
                            Response<ApiResponse<AuthResponse>> response
                    ) {
                        handleAuthResponse(response, "Registrasi customer gagal.", callback);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                        callback.onError("Tidak bisa terhubung ke server: " + t.getMessage());
                    }
                });
    }

    public void registerTechnician(
            String name,
            String email,
            String phoneNumber,
            String password,
            String address,
            String description,
            AuthCallback callback
    ) {
        RegisterTechnicianRequest request = new RegisterTechnicianRequest(
                safeTrim(name),
                safeTrim(email),
                safeTrim(phoneNumber),
                safeValue(password),
                safeTrim(address),
                safeTrim(description)
        );

        ApiClient.getApiService(context)
                .registerTechnician(request)
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<AuthResponse>> call,
                            Response<ApiResponse<AuthResponse>> response
                    ) {
                        handleAuthResponse(response, "Registrasi teknisi gagal.", callback);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                        callback.onError("Tidak bisa terhubung ke server: " + t.getMessage());
                    }
                });
    }

    private void handleAuthResponse(
            Response<ApiResponse<AuthResponse>> response,
            String fallbackMessage,
            AuthCallback callback
    ) {
        if (!response.isSuccessful()) {
            String message = ErrorParser.parseError(response, fallbackMessage);
            callback.onError(message);
            return;
        }

        ApiResponse<AuthResponse> body = response.body();

        if (body == null) {
            callback.onError("Response server kosong.");
            return;
        }

        if (!body.success) {
            String message = ErrorParser.getBestMessage(body, fallbackMessage);
            callback.onError(message);
            return;
        }

        if (body.data == null || body.data.accessToken == null) {
            callback.onError("Token autentikasi tidak ditemukan.");
            return;
        }

        tokenManager.saveAuth(body.data);
        ApiClient.reset();

        callback.onSuccess(body.data);
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
