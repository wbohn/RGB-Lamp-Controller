package com.wbohn.rgblamp.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.wbohn.rgblamp.R;

/**
 * Created by William on 5/18/2015.
 */
public class AppPreferences {

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    int defaultColors[];

    public AppPreferences(Context context) {
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.prefsEditor = sharedPrefs.edit();
        defaultColors = context.getResources().getIntArray(R.array.default_colors);
    }

    public int getBulbColor(int index) {
        return sharedPrefs.getInt("lampColor_" + index, defaultColors[index]);
    }

    public void saveBulbColor(int index, int color) {
        prefsEditor.putInt("lampColor_" + index, color).commit();
    }

    public boolean getAutoConnect() {
        return sharedPrefs.getBoolean("auto_connect", false);
    }
    public void saveAutoConnect(boolean autoConnect) {
        prefsEditor.putBoolean("auto_connect", autoConnect).commit();
    }

    public String getDefaultDeviceAddress() {
        return sharedPrefs.getString("default_device", null);
    }

    public String getFadeType() {
        return sharedPrefs.getString("fade_steps", "2");
    }

    public int getHighScore() {
        return sharedPrefs.getInt("high_score", 0);
    }
    public void saveHighScore(int highScore) {
        prefsEditor.putInt("high_score", highScore);
    }
}
