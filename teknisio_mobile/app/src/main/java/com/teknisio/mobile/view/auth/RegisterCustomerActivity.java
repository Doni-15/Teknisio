package com.teknisio.mobile.view.auth;

import android.view.MotionEvent;
import android.text.InputType;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.teknisio.mobile.R;
import com.teknisio.mobile.controller.AuthController;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.AuthResponse;
import com.teknisio.mobile.network.ApiClient;
import com.teknisio.mobile.view.customer.CustomerHomeActivity;

public class RegisterCustomerActivity extends AppCompatActivity {

    private TextView txtTabCustomer;
    private TextView txtTabTechnician;

    private EditText edtName;
    private EditText edtEmail;
    private EditText edtPhone;
    private EditText edtPassword;
    private EditText edtAddress;
    private EditText edtDescription;

    private LinearLayout layoutDescription;

    private Button btnRegister;
    private TextView txtGoLogin;

    private AuthController authController;
    private TokenManager tokenManager;

    private boolean technicianMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_customer);

        authController = new AuthController(this);
        tokenManager = new TokenManager(this);

        txtTabCustomer = findViewById(R.id.txtTabCustomer);
        txtTabTechnician = findViewById(R.id.txtTabTechnician);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        setupPasswordToggle(edtPassword);
        edtAddress = findViewById(R.id.edtAddress);
        edtDescription = findViewById(R.id.edtDescription);

        layoutDescription = findViewById(R.id.layoutDescription);

        btnRegister = findViewById(R.id.btnRegister);
        txtGoLogin = findViewById(R.id.txtGoLogin);

        setupLoginLinkText();
        setRegisterMode(false);

        txtTabCustomer.setOnClickListener(v -> setRegisterMode(false));
        txtTabTechnician.setOnClickListener(v -> setRegisterMode(true));

        btnRegister.setOnClickListener(v -> doRegister());

        txtGoLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterCustomerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setRegisterMode(boolean technician) {
        technicianMode = technician;

        if (technicianMode) {
            txtTabTechnician.setBackgroundResource(R.drawable.bg_role_tab_active);
            txtTabTechnician.setTextColor(Color.WHITE);

            txtTabCustomer.setBackgroundResource(R.drawable.bg_role_tab_inactive);
            txtTabCustomer.setTextColor(Color.parseColor("#2F4A8A"));

            layoutDescription.setVisibility(View.VISIBLE);
        } else {
            txtTabCustomer.setBackgroundResource(R.drawable.bg_role_tab_active);
            txtTabCustomer.setTextColor(Color.WHITE);

            txtTabTechnician.setBackgroundResource(R.drawable.bg_role_tab_inactive);
            txtTabTechnician.setTextColor(Color.parseColor("#2F4A8A"));

            layoutDescription.setVisibility(View.GONE);
        }
    }

    private void doRegister() {
        setLoading(true);

        if (technicianMode) {
            registerTechnician();
        } else {
            registerCustomer();
        }
    }

    private void registerCustomer() {
        authController.registerCustomer(
                edtName.getText().toString(),
                edtEmail.getText().toString(),
                edtPhone.getText().toString(),
                edtPassword.getText().toString(),
                edtAddress.getText().toString(),
                new AuthController.AuthCallback() {
                    @Override
                    public void onSuccess(AuthResponse authResponse) {
                        setLoading(false);

                        tokenManager.clearSession();
                        ApiClient.reset();

                        Toast.makeText(
                                RegisterCustomerActivity.this,
                                "Registrasi customer berhasil. Silakan login kembali.",
                                Toast.LENGTH_LONG
                        ).show();

                        goToLogin();
                    }

                    @Override
                    public void onError(String message) {
                        setLoading(false);
                        Toast.makeText(RegisterCustomerActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void registerTechnician() {
        authController.registerTechnician(
                edtName.getText().toString(),
                edtEmail.getText().toString(),
                edtPhone.getText().toString(),
                edtPassword.getText().toString(),
                edtAddress.getText().toString(),
                edtDescription.getText().toString(),
                new AuthController.AuthCallback() {
                    @Override
                    public void onSuccess(AuthResponse authResponse) {
                        setLoading(false);

                        tokenManager.clearSession();
                        ApiClient.reset();

                        Toast.makeText(
                                RegisterCustomerActivity.this,
                                "Registrasi teknisi berhasil. Halaman teknisi belum tersedia.",
                                Toast.LENGTH_LONG
                        ).show();

                        goToLogin();
                    }

                    @Override
                    public void onError(String message) {
                        setLoading(false);
                        Toast.makeText(RegisterCustomerActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        btnRegister.setText(loading ? "Loading..." : "Confirm");

        txtTabCustomer.setEnabled(!loading);
        txtTabTechnician.setEnabled(!loading);
    }

    private void goToCustomerHome() {
        Intent intent = new Intent(RegisterCustomerActivity.this, CustomerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void goToLogin() {
        Intent intent = new Intent(RegisterCustomerActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void setupPasswordToggle(EditText passwordField) {
        final boolean[] passwordVisible = {false};

        applyPasswordVisibilityState(passwordField, passwordVisible[0]);

        passwordField.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            Drawable drawableEnd = passwordField.getCompoundDrawables()[2];

            if (drawableEnd == null) {
                return false;
            }

            int touchAreaStart = passwordField.getWidth()
                    - passwordField.getPaddingEnd()
                    - drawableEnd.getBounds().width()
                    - dpToPx(16);

            if (event.getX() >= touchAreaStart) {
                passwordVisible[0] = !passwordVisible[0];

                applyPasswordVisibilityState(passwordField, passwordVisible[0]);
                passwordField.setSelection(passwordField.getText().length());

                return true;
            }

            return false;
        });
    }

    private void applyPasswordVisibilityState(EditText passwordField, boolean visible) {
        if (visible) {
            passwordField.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            passwordField.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_visibility_off,
                    0
            );
        } else {
            passwordField.setInputType(
                    InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            passwordField.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_visibility,
                    0
            );
        }

        passwordField.setTypeface(Typeface.DEFAULT);
        passwordField.setCompoundDrawablePadding(dpToPx(12));
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setupLoginLinkText() {
        String fullText = "Already have account? Login now";
        String linkText = "Login now";

        SpannableString spannableString = new SpannableString(fullText);

        int startIndex = fullText.indexOf(linkText);
        int endIndex = startIndex + linkText.length();

        if (startIndex >= 0) {
            spannableString.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#2F5BEA")),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            spannableString.setSpan(
                    new UnderlineSpan(),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        txtGoLogin.setText(spannableString);
    }
}
