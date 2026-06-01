package com.teknisio.mobile.model.response;

import java.util.Map;

public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
    public Map<String, String> errors;
}
