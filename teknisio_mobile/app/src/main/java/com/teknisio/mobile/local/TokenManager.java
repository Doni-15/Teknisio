package com.teknisio.mobile.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.teknisio.mobile.model.response.AuthResponse;
import com.teknisio.mobile.model.response.AuthUserResponse;

public class TokenManager {
  private static final String PREF_NAME = "teknisio_session";
  private static final String KEY_ACCESS_TOKEN = "access_token";
  private static final String KEY_TOKEN_TYPE = "token_type";
  private static final String KEY_EXPIRES_IN_MS = "expires_in_ms";
  private static final String KEY_EXPIRES_AT_MS = "expires_at_ms";
  private static final String KEY_USER_ID = "user_id";
  private static final String KEY_TECHNICIAN_PROFILE_ID = "technician_profile_id";
  private static final String KEY_NAME = "name";
  private static final String KEY_EMAIL = "email";
  private static final String KEY_PHONE_NUMBER = "phone_number";
  private static final String KEY_PROFILE_PHOTO = "profile_photo";
  private static final String KEY_ADDRESS = "address";
  private static final String KEY_ROLE = "role";
  private static final String KEY_ACCOUNT_STATUS = "account_status";
  private final SharedPreferences prefs;

  public TokenManager(Context context) {
      prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

    public void saveAuth(AuthResponse authResponse) {
      if (authResponse == null) {
          return;
      }

      AuthUserResponse user = authResponse.user;
      SharedPreferences.Editor editor = prefs.edit();
      long expiresInMs = authResponse.expiresInMs == null ? 0L : authResponse.expiresInMs;
      long expiresAtMs = expiresInMs <= 0L ? 0L : System.currentTimeMillis() + expiresInMs;

      editor.putString(KEY_ACCESS_TOKEN, authResponse.accessToken);
      editor.putString(KEY_TOKEN_TYPE, authResponse.tokenType);
      editor.putLong(KEY_EXPIRES_IN_MS, expiresInMs);
      editor.putLong(KEY_EXPIRES_AT_MS, expiresAtMs);

      if (user != null) {
        editor.putString(KEY_USER_ID, user.userId);
        editor.putString(KEY_TECHNICIAN_PROFILE_ID, user.technicianProfileId);
        editor.putString(KEY_NAME, user.name);
        editor.putString(KEY_EMAIL, user.email);
        editor.putString(KEY_PHONE_NUMBER, user.phoneNumber);
        editor.putString(KEY_PROFILE_PHOTO, user.profilePhoto);
        editor.putString(KEY_ADDRESS, user.address);
        editor.putString(KEY_ROLE, user.role);
        editor.putString(KEY_ACCOUNT_STATUS, user.accountStatus);
      }
      editor.apply();
    }

    public String getAccessToken() {
      return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getTokenType() {
      return prefs.getString(KEY_TOKEN_TYPE, "Bearer");
    }

    public String getRole() {
      return prefs.getString(KEY_ROLE, null);
    }

    public String getName() {
      return prefs.getString(KEY_NAME, null);
    }

    public String getEmail() {
      return prefs.getString(KEY_EMAIL, null);
    }

    public String getAddress() {
      return prefs.getString(KEY_ADDRESS, null);
    }


    public String getPhoneNumber() {
      return prefs.getString(KEY_PHONE_NUMBER, null);
    }

    public String getProfilePhoto() {
      return prefs.getString(KEY_PROFILE_PHOTO, null);
    }


    public String getTechnicianProfileId() {
      return prefs.getString(KEY_TECHNICIAN_PROFILE_ID, null);
    }

    public String getAccountStatus() {
      return prefs.getString(KEY_ACCOUNT_STATUS, null);
    }

    public void saveUser(AuthUserResponse user) {
      if (user == null) {
          return;
      }

      SharedPreferences.Editor editor = prefs.edit();
      editor.putString(KEY_USER_ID, user.userId);
      editor.putString(KEY_TECHNICIAN_PROFILE_ID, user.technicianProfileId);
      editor.putString(KEY_NAME, user.name);
      editor.putString(KEY_EMAIL, user.email);
      editor.putString(KEY_PHONE_NUMBER, user.phoneNumber);
      editor.putString(KEY_PROFILE_PHOTO, user.profilePhoto);
      editor.putString(KEY_ADDRESS, user.address);
      editor.putString(KEY_ROLE, user.role);
      editor.putString(KEY_ACCOUNT_STATUS, user.accountStatus);
      editor.apply();
    }

    public boolean isLoggedIn() {
      String token = getAccessToken();

      if (token == null || token.trim().isEmpty()) {
          return false;
      }

      long expiresAtMs = prefs.getLong(KEY_EXPIRES_AT_MS, 0L);

      if (expiresAtMs <= 0L) {
          return true;
      }

      if (System.currentTimeMillis() >= expiresAtMs) {
          clearSession();
          return false;
      }

      return true;
    }

    public boolean isCustomer() {
      return "CUSTOMER".equalsIgnoreCase(getRole());
    }

    public boolean isTechnician() {
      return "TECHNICIAN".equalsIgnoreCase(getRole());
    }

    public void clearSession() {
      prefs.edit().clear().apply();
    }
}
