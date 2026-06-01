package com.teknisio.mobile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.view.auth.LoginActivity;
import com.teknisio.mobile.view.customer.CustomerHomeActivity;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);

        if (tokenManager.isLoggedIn() && tokenManager.isCustomer()) {
            goToCustomerHome();
        } else {
            tokenManager.clearSession();
            goToLogin();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToCustomerHome() {
        Intent intent = new Intent(MainActivity.this, CustomerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
