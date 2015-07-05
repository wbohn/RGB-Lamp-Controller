package com.wbohn.rgblamp;

import android.app.Application;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import com.wbohn.rgblamp.prefs.AppPreferences;

/**
 * Created by William on 7/2/2015.
 */
public class App extends Application {

    private static AppPreferences appPreferences;
    private static Bus eventBus;

    @Override
    public void onCreate() {
        super.onCreate();

        appPreferences = new AppPreferences(this);
        eventBus = new Bus(ThreadEnforcer.ANY);
    }

    public static AppPreferences getAppPreferences() {
        return appPreferences;
    }

    public static Bus getEventBus() {
        return eventBus;
    }
}
