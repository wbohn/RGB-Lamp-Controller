package com.wbohn.rgblamp.bus;

import android.bluetooth.BluetoothDevice;

/**
 * Created by William on 7/2/2015.
 */
public class DeviceClickedEvent {
    private BluetoothDevice device;

    public DeviceClickedEvent(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }
}
