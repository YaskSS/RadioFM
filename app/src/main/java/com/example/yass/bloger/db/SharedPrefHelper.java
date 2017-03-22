package com.example.yass.bloger.db;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.yass.bloger.core.App;

/**
 * Created by Serhey on 22.11.2016.
 */

public class SharedPrefHelper {

    public final String SOUND_64 = "/blogerfm-64";
    public final String SOUND_128 = "/blogerfm-128";

    private static final String APP_PREFERENCES = "settings";

    private static final String TAG = "SharedPrefHelper";
    private static final String PORT = "port";
    private static final String DEFAULT_PORT = "8000";
    private static final String SOUND = "sound";
    private static final String DEFAULT_SOUND = "/blogerfm-128";

    private static final String NAME_SOUND = "nameSound";
    private static final String DEFAULT_NAME_SOUND = "";

    private static final String CHOISE_CHANNEL = "choise";
    private static final String DEFAULT_CHOISE_CHANNEL = "JAZZ";

    private static SharedPrefHelper ourInstance;

    public static SharedPrefHelper getInstance() {
        Context context = App.getContext();
        if (ourInstance == null) {
            ourInstance = new SharedPrefHelper(context);
        }

        return ourInstance;
    }

    private SharedPrefHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    private SharedPreferences sharedPreferences;

    public void savePort(String port) {
        sharedPreferences.edit().putString(PORT, port).apply();
    }

    public String getPORT() {
        return sharedPreferences.getString(PORT, DEFAULT_PORT);
    }

    public void saveSound(String sound) {
        sharedPreferences.edit().putString(SOUND, sound).apply();
    }

    public String getSound() {
        return sharedPreferences.getString(SOUND, DEFAULT_SOUND);
    }


    public void saveNameSound(String sound) {
        sharedPreferences.edit().putString(NAME_SOUND, sound).apply();
    }

    public String getNameSound() {
        return sharedPreferences.getString(NAME_SOUND, DEFAULT_NAME_SOUND);
    }

    public void saveChoiseChannel(String channel){
        sharedPreferences.edit().putString(CHOISE_CHANNEL, channel).apply();
    }

    public String getChoiseChannel(){
        return sharedPreferences.getString(CHOISE_CHANNEL, DEFAULT_CHOISE_CHANNEL);
    }
}
