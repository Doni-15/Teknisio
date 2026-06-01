package com.teknisio.mobile.view.auth;

import com.teknisio.mobile.base.BaseActivity;
import android.view.MotionEvent;
import android.text.InputType;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.content.Intent;
import android.text.style.UnderlineSpan;
import android.text.style.ForegroundColorSpan;
import android.text.Spanned;
import android.text.SpannableString;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.teknisio.mobile.R;
import com.teknisio.mobile.controller.AuthController;
import com.teknisio.mobile.local.TokenManager;
import com.teknisio.mobile.model.response.AuthResponse;
import com.teknisio.mobile.view.customer.CustomerHomeActivity;
import com.teknisio.mobile.view.technician.TechnicianHomeActivity;

public class LoginActivity extends BaseActivity {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView txtGoRegister;

    private AuthController authController;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authController = new AuthController(this);
        tokenManager = new TokenManager(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        setupPasswordToggle(edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoRegister = findViewById(R.id.txtGoRegister);

        setupRegisterLinkText();

        btnLogin.setOnClickListener(v -> doLogin());

        txtGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterCustomerActivity.class);
            startActivity(intent);
        });
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

    private void setupRegisterLinkText() {
        String fullText = "Did not have account? Register now";
        String linkText = "Register now";

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

        txtGoRegister.setText(spannableString);
    }

    private void doLogin() {
        setLoading(true);

        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        authController.login(email, password, new AuthController.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse authResponse) {
                setLoading(false);
Toast.makeText(LoginActivity.this, "Login berhasil.", Toast.LENGTH_SHORT).show();
                goToCustomerHome();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Memuat..." : "Masuk");
    }

    private void goToCustomerHome() {
        Intent intent = new Intent(LoginActivity.this, tokenManager.isTechnician() ? TechnicianHomeActivity.class : CustomerHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
