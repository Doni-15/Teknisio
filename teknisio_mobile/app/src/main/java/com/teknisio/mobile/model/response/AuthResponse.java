package com.teknisio.mobile.model.response;

public class AuthResponse {
    public String accessToken;
    public String tokenType;
    public Long expiresInMs;
    public AuthUserResponse user;
}
