package com.wbohn.rgblamp.bluetooth;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.wbohn.rgblamp.App;
import com.wbohn.rgblamp.bus.ConnectionEvent;
import com.wbohn.rgblamp.bus.DeviceClickedEvent;
import com.wbohn.rgblamp.bus.IncomingMessageEvent;
import com.wbohn.rgblamp.bus.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnection extends Fragment {

    public static final String TAG = "BluetoothConnection";

    public static final int REQUEST_ENABLE_BT = 5;

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;

    private boolean autoConnect;
    private String favoriteDeviceAddress;

    private ConnectThread connectThread;
    private ManageConnectionThread manageConnectionThread;
    private Handler handler;

    private int state = STATE_NONE;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
                stop();
            }
            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Toast.makeText(getActivity(), "disconnected", Toast.LENGTH_SHORT).show();
                stop();
            }
        }
    };

    public static BluetoothConnection newInstance(boolean autoConnect, String favoriteDeviceAddress) {
        BluetoothConnection bluetoothConnection = new BluetoothConnection();

        Bundle args = new Bundle();
        args.putBoolean("autoConnect", autoConnect);
        args.putString("favoriteDeviceAddress", favoriteDeviceAddress);

        bluetoothConnection.setArguments(args);
        return bluetoothConnection;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getEventBus().register(this);

        autoConnect = getArguments().getBoolean("autoConnect");
        favoriteDeviceAddress = getArguments().getString("favoriteDeviceAddress");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "This device does not support Bluetooth", Toast.LENGTH_SHORT);
            return;
        }

        if (bluetoothAdapter.isEnabled()) {

            tryAutoConnect();

        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        handler = new Handler(activity.getMainLooper());

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        activity.registerReceiver(broadcastReceiver, filter1);
        activity.registerReceiver(broadcastReceiver, filter2);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        App.getEventBus().unregister(this);
    }

    /* BluetoothDialog callback */
    public void connect(BluetoothDevice device) {
        connectThread = new ConnectThread(device);
        connectThread.start();

        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.i(TAG, "creating and starting a ManageConnectionThread");

        // cancel the tread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // cancel any thread currently running a connection
        if (manageConnectionThread != null) {
            manageConnectionThread.cancel();
            manageConnectionThread = null;
        }

        manageConnectionThread = new ManageConnectionThread(socket);
        manageConnectionThread.start();

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                App.getEventBus().post(new ConnectionEvent());
            }
        });
        setState(STATE_CONNECTED);
    }

    public synchronized void setState(int state) {
        this.state = state;
        // Give the new state to the Handler so the UI Activity can update
        // mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public void write(String msg) {

        if (isConnected()) {
            manageConnectionThread.write(msg.getBytes());
        }
    }

    @Subscribe
    public void onWriteRequest(Message message) {
        Log.i(TAG, message.text);
        if (isConnected()) {
            manageConnectionThread.write(message.text.getBytes());
        }
    }

    @Subscribe
    public void onDeviceClicked(DeviceClickedEvent event) {
        connect(event.getDevice());
    }

    // thread for initiating connection to a device
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        //constructor passed device to get socket
        public ConnectThread(BluetoothDevice device) {
            Log.i(TAG, "create connectThread");

            this.device = device;
            BluetoothSocket tmp = null;
            //get BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
            } catch (IOException e) { Log.e(TAG, "exception", e); }

            socket = tmp;
        }

        @Override
        public void run() {
            Log.i(TAG, "Begin connect thread");
            bluetoothAdapter.cancelDiscovery();

            try {
                // connect the device through the socket. this will block
                // until it succeeds or throws exception
                socket.connect();
                Log.i(TAG, "attempting to connect");
            } catch (IOException connectException) {
                Log.e(TAG, "connect exception", connectException);
                // unable to connect. close the socket and get out
                try {
                    socket.close();
                } catch (IOException closeException) { Log.e(TAG, "connect close exception", closeException); }
                return;
            }

            // reset mConnectThread after connection
            synchronized (BluetoothConnection.this) {
                connectThread = null;
            }

            // start a ConnectedThread with the socket
            // to handle reads and writes
            Log.i(TAG, "Connected. Starting ConnectedThread");
            connected(socket, device);
        }

        public void cancel() {
            try {
                if (socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) { Log.e(TAG, "connect close exception", e); }
        }

    } //end ConnectThread

    // thread for handling read and write
    private class ManageConnectionThread extends Thread {

        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ManageConnectionThread(BluetoothSocket socket) {
            Log.i(TAG, "create ManageConnectionThread");

            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // get the input and output streams from tmp objects
            try {
                tmpIn  = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { Log.e(TAG, "manageconnection get stream exception", e); }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        @Override
        public void run() {
            Log.i(TAG, "begin ManageConnectoinThread");
            byte[] buffer = new byte[1024];

            // keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // read from InputStream, receive each byte and add it to message
                    String message = "";
                    if (interrupted()) {
                        return;
                    }
                    while (inputStream.available() > 0) {
                        final char mbyte = (char) inputStream.read();
                        message += (char) mbyte;
                        Log.i(TAG, message);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                App.getEventBus().post(new IncomingMessageEvent(mbyte));
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, " manageconnection run exception", e);
                    break;
                }
            }
        }

        // call from main activity to send data to the remote device
        public synchronized void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                outputStream.flush();
            } catch (IOException e) { Log.e(TAG, "write exception", e); }
        }

        // call from main activity to shutdown the connection
        public synchronized void cancel() {
            try {
                Log.i(TAG, "cancel");

                if (socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) { Log.e(TAG, "manageconnection close exception", e); }
        }
    }

    /* Stop all threads */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (manageConnectionThread != null) {
            manageConnectionThread.interrupt();
            manageConnectionThread.cancel();
            manageConnectionThread = null;
        }
        setState(STATE_NONE);
    }

    public String[] getPairedDeviceAddresses() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String[] pairedDeviceAddresses = new String[pairedDevices.size()];

        int i = 0;
        for (BluetoothDevice device : pairedDevices) {
            pairedDeviceAddresses[i] = device.getAddress();
            i++;
        }
        return pairedDeviceAddresses;
    }

    public String[] getPairedDeviceNames() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String[] pairedDeviceNames = new String[pairedDevices.size()];

        int i = 0;
        for (BluetoothDevice device : pairedDevices) {
            pairedDeviceNames[i] = device.getName();
            i++;
        }
        return pairedDeviceNames;
    }

    public boolean isConnected() {
        return (state == STATE_CONNECTED);
    }

    private void tryAutoConnect() {
        if (autoConnect) {
            if (favoriteDeviceAddress != null) {
                BluetoothDevice defaultDevice = bluetoothAdapter.getRemoteDevice(favoriteDeviceAddress);
                if (defaultDevice != null) {
                    Log.i(TAG, "auto connecting to " + defaultDevice.getName());
                    connect(defaultDevice);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        favoriteDeviceAddress = getArguments().getString("favoriteDeviceAddress");
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                tryAutoConnect();
            }
        }
    }
}