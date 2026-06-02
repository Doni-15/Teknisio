package com.teknisio.mobile.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teknisio.mobile.util.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static ApiService apiService;

    private ApiClient() {
    }

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(isDebugBuild(context)
                    ? HttpLoggingInterceptor.Level.BASIC
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(loggingInterceptor)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }

        return apiService;
    }

    public static void reset() {
        apiService = null;
    }

    private static boolean isDebugBuild(Context context) {
        return context != null
                && (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

}
