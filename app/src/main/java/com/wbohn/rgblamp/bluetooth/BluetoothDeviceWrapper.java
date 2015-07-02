package com.wbohn.rgblamp.bluetooth;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceWrapper {
    public BluetoothDevice device;
    public String name;
    public String address;

    public BluetoothDeviceWrapper(BluetoothDevice device) {
        this.device = device;
        this.name = device.getName();
        this.address = device.getAddress();
    }

    public BluetoothDevice getDevice(){
        return device;
    }

    @Override
    public String toString() {
        return name + "\n" + address;
    }
}
