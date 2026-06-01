package com.teknisio.mobile.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.teknisio.mobile.util.SystemBarHelper;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hideSystemNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemNavigation();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemNavigation();
        }
    }

    protected void hideSystemNavigation() {
        SystemBarHelper.hideNavigationBar(this);
    }
}
