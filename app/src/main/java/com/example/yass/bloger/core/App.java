package com.example.yass.bloger.core;

import android.app.Application;
import android.content.Context;


public class App extends Application  {

    private static Context context;

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        context = base;
    }

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
