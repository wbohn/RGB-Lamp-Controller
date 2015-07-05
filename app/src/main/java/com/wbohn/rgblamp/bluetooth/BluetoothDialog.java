package com.wbohn.rgblamp.bluetooth;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.wbohn.rgblamp.App;
import com.wbohn.rgblamp.bluetooth.BluetoothDeviceWrapper;
import com.wbohn.rgblamp.R;
import com.wbohn.rgblamp.bus.DeviceClickedEvent;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothDialog extends DialogFragment {

    public final static String TAG = "BluetoothDialog";

    private ArrayAdapter<BluetoothDeviceWrapper> pairedAdapter;
    private ArrayAdapter<BluetoothDeviceWrapper> otherAdapter;

    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<BluetoothDevice> otherDevices = new ArrayList<BluetoothDevice>();

    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.i(TAG, "device found");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (pairedDevices.contains(device)) {
                    return;
                }
                if (!otherDevices.contains(device)) {
                    otherDevices.add(device);
                    deviceFound(device);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getEventBus().register(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "This device does not support Bluetooth", Toast.LENGTH_SHORT);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {

        } else {
            /* Bluetooth is already on, store paired devices now
             because we won't enter onActivityResult */
            pairedDevices = bluetoothAdapter.getBondedDevices();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View bluetoothDialogView = inflater.inflate(R.layout.dialog_bluetooth, null);

        ListView pairedListView = (ListView) bluetoothDialogView.findViewById(R.id.paired_listview);
        ListView otherListView = (ListView) bluetoothDialogView.findViewById(R.id.other_devices_listview);

        pairedAdapter = new ArrayAdapter<BluetoothDeviceWrapper>(getActivity(), android.R.layout.simple_list_item_1);
        otherAdapter = new ArrayAdapter<BluetoothDeviceWrapper>(getActivity(), android.R.layout.simple_list_item_1);

        // set the adapters for the ListViews
        pairedListView.setAdapter(pairedAdapter);
        otherListView.setAdapter(otherAdapter);

        // declare a single click listener for both listviews. android will handle pairing devices
        // before attempting a connection, so clicking a device in either list should begin a connection
        AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lv, View v, int position, long id) {

                BluetoothDeviceWrapper clickedDevice = (BluetoothDeviceWrapper) lv.getItemAtPosition(position);
                String msg = "Connecting to " + clickedDevice.getDevice().getName();
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();

                App.getEventBus().post(new DeviceClickedEvent(clickedDevice.getDevice()));
                getDialog().dismiss();
            }
        };
        pairedListView.setOnItemClickListener(deviceClickListener);
        otherListView.setOnItemClickListener(deviceClickListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(bluetoothDialogView).setTitle("Connect to a device");

        builder.setPositiveButton("Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                /* do nothing because this onClick is overridden in onStart
                 to prevent automatically dismissing the dialog */
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                bluetoothAdapter.cancelDiscovery();
            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        IntentFilter foundFilter = new IntentFilter((BluetoothDevice.ACTION_FOUND));
        activity.registerReceiver(broadcastReceiver, foundFilter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (broadcastReceiver != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (bluetoothAdapter.isDiscovering() || bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                        return;
                    }
                    bluetoothAdapter.startDiscovery();
                    Toast.makeText(getActivity(), "Searching for devices", Toast.LENGTH_LONG).show();
                }
            });
        }

        updateDeviceLists();
    }

    public void updateDeviceLists() {
        // get all known devices
        pairedDevices = bluetoothAdapter.getBondedDevices();

        // add known devices to listview
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                pairedAdapter.add(new BluetoothDeviceWrapper(device));
            }
            pairedAdapter.notifyDataSetChanged();
        }
        if (otherDevices != null) {
            for (BluetoothDevice device : otherDevices) {
                otherAdapter.add(new BluetoothDeviceWrapper(device));
            }
            otherAdapter.notifyDataSetChanged();
        }
    }

    public void deviceFound(BluetoothDevice device) {
        otherAdapter.add(new BluetoothDeviceWrapper(device));
        otherAdapter.notifyDataSetChanged();
    }
}
