package com.teknisio.mobile;

import android.content.Intent;
import android.os.Bundle;

import com.teknisio.mobile.base.BaseActivity;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.view.auth.LoginActivity;
import com.teknisio.mobile.view.customer.CustomerHomeActivity;
import com.teknisio.mobile.view.technician.TechnicianHomeActivity;

public class MainActivity extends BaseActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);

        if (!tokenManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        if (tokenManager.isCustomer()) {
            goToCustomerHome();
            return;
        }

        if (tokenManager.isTechnician()) {
            goToTechnicianHome();
            return;
        }

        tokenManager.clearSession();
        goToLogin();
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

    private void goToTechnicianHome() {
        Intent intent = new Intent(MainActivity.this, TechnicianHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
