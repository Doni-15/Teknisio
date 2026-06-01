package com.teknisio.mobile.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.teknisio.mobile.model.response.ApiResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ErrorParser {

    private static final Gson gson = new Gson();

    private ErrorParser() {
        // Prevent instantiation
    }

    public static String parseError(Response<?> response, String fallbackMessage) {
        if (response == null) {
            return fallbackMessage;
        }

        ResponseBody errorBody = response.errorBody();

        if (errorBody == null) {
            return fallbackMessage;
        }

        try {
            String json = errorBody.string();

            if (json == null || json.trim().isEmpty()) {
                return fallbackMessage;
            }

            Type type = new TypeToken<ApiResponse<Object>>() {}.getType();
            ApiResponse<Object> apiResponse = gson.fromJson(json, type);

            return getBestMessage(apiResponse, fallbackMessage);

        } catch (IOException | JsonSyntaxException exception) {
            return fallbackMessage;
        }
    }

    public static String getBestMessage(ApiResponse<?> apiResponse, String fallbackMessage) {
        if (apiResponse == null) {
            return fallbackMessage;
        }

        String fieldError = getFirstFieldError(apiResponse.errors);

        if (fieldError != null && !fieldError.trim().isEmpty()) {
            return fieldError;
        }

        if (apiResponse.message != null && !apiResponse.message.trim().isEmpty()) {
            return apiResponse.message;
        }

        return fallbackMessage;
    }

    private static String getFirstFieldError(Map<String, String> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }

        for (String message : errors.values()) {
            if (message != null && !message.trim().isEmpty()) {
                return message;
            }
        }

        return null;
    }
}
