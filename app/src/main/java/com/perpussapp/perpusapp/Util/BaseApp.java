package com.perpussapp.perpusapp.Util;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate();
    }
}
