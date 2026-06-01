package com.teknisio.mobile.network;

import android.content.Context;
import com.teknisio.mobile.local.TokenManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
  private final TokenManager tokenManager;

  public AuthInterceptor(Context context) {
    this.tokenManager = new TokenManager(context.getApplicationContext());
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request originalRequest = chain.request();
    String token = tokenManager.getAccessToken();

    if (token == null || token.trim().isEmpty()) {
      return chain.proceed(originalRequest);
    }

    Request newRequest = originalRequest.newBuilder()
      .addHeader("Authorization", "Bearer " + token)
      .addHeader("Accept", "application/json")
      .build();

    return chain.proceed(newRequest);
  }
}
