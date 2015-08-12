package com.wbohn.rgblamp;

import com.squareup.otto.Subscribe;
import com.wbohn.rgblamp.bus.ConnectionEvent;
import com.wbohn.rgblamp.bus.MessageBuilder;
import com.wbohn.rgblamp.bus.ModeChangeEvent;

/**
 * Created by William on 7/3/2015.
 */
public class LampManager {
    public interface LampInterface {
        int[] getColors();
    }
    private LampInterface lampInterface;

    private int mode;

    public LampManager() {
    }

    public void setLampInterface(LampInterface lampInterface) {
        this.lampInterface = lampInterface;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Subscribe
    public void onModeChange(ModeChangeEvent event) {
        setMode(event.mode);
        initializeLamp();
    }

    @Subscribe
    public void onConnection(ConnectionEvent event) {
        initializeLamp();
    }

    public void initializeLamp() {
        sendModeChangeMessage();
        sendFadeSettings();
        updateAllBulbs();
    }

    public void sendModeChangeMessage() {
        App.getEventBus().post(MessageBuilder.createModeChangeMessage(mode));
    }

    public void sendFadeSettings() {
        String fadeType = App.getAppPreferences().getFadeType();
        App.getEventBus().post(MessageBuilder.createFadeChangeMessage(fadeType));
    }

    public void updateAllBulbs() {
        int[] colors = lampInterface.getColors();
        for (int i = 0; i < colors.length; i++) {
            updateBulb(i, colors[i]);
        }
    }

    public void updateBulb(int index, int color) {
        App.getAppPreferences().saveBulbColor(index, color);
        App.getEventBus().post(MessageBuilder.createBulbChangeMessage(index, color));
    }
}
