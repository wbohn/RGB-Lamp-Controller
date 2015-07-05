package com.wbohn.rgblamp;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.wbohn.rgblamp.bluetooth.BluetoothConnection;
import com.wbohn.rgblamp.bus.MessageBuilder;
import com.wbohn.rgblamp.bus.ModeChangeEvent;
import com.wbohn.rgblamp.color_circle.ColorCircleFragment;
import com.wbohn.rgblamp.game.GameFragment;
import com.wbohn.rgblamp.bluetooth.BluetoothDialog;
import com.wbohn.rgblamp.prefs.PrefsActivity;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements
        ActionBar.OnNavigationListener {

    public static final String TAG = "MainActivity";

    public static final int MODE_SOLID = 0;
    public static final int MODE_FADE = 1;
    public static final int MODE_GAME = 2;

    public static final int PREFS_ACTIVITY_REQUEST = 9;

    private GameFragment gameFragment;
    private ColorCircleFragment colorCircleFragment;
    private BluetoothConnection bluetoothConnection;
    private BluetoothDialog bluetoothDialog;

    public int mode = MODE_SOLID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        App.getEventBus().register(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);

        ArrayList<String> modeList = new ArrayList<String>();
        modeList.add("Solid");
        modeList.add("Fade");
        modeList.add("Game");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1, modeList);

        actionBar.setListNavigationCallbacks(arrayAdapter, this);

        FragmentManager fragmentManager = getFragmentManager();

        colorCircleFragment = (ColorCircleFragment) fragmentManager.findFragmentById(R.id.fragment_main);

        if (savedInstanceState == null) {
            gameFragment = new GameFragment();
            bluetoothConnection = BluetoothConnection.newInstance(App.getAppPreferences().getAutoConnect(), App.getAppPreferences().getDefaultDeviceAddress());

            fragmentManager.beginTransaction()
                    .add(bluetoothConnection, "bluetooth_connection")
                    .add(R.id.container_fragment_swap, gameFragment, "gameFragment")
                    .detach(gameFragment)
                    .commit();
        } else {
            gameFragment = (GameFragment) fragmentManager.findFragmentByTag("gameFragment");
            bluetoothConnection = (BluetoothConnection) fragmentManager.findFragmentByTag("bluetooth_connection");
            mode = savedInstanceState.getInt("mode");
            actionBar.setSelectedNavigationItem(mode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", mode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getEventBus().unregister(this);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        mode = position;
        App.getEventBus().post(new ModeChangeEvent(position));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_bluetooth:
                bluetoothDialog = new BluetoothDialog();
                bluetoothDialog.show(getFragmentManager(), "dialog_bluetooth");
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, PrefsActivity.class);
                intent.putExtra("pairedDeviceNames", bluetoothConnection.getPairedDeviceNames());
                intent.putExtra("pairedDeviceAddresses", bluetoothConnection.getPairedDeviceAddresses());

                startActivityForResult(intent, PREFS_ACTIVITY_REQUEST);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PREFS_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("fadeTypeChanged", false)) {
                    sendFadeSettings();
                }
            }
        }
    }

    private void sendFadeSettings() {
        String fadeType = App.getAppPreferences().getFadeType();
        App.getEventBus().post(MessageBuilder.createFadeChangeMessage(fadeType));
    }

    @Subscribe
    public void onModeChange(ModeChangeEvent event) {
        if (event.mode == MODE_GAME) {
            showGameFragment();
        } else {
            hideGameFragment();
        }
    }

    public void showGameFragment() {
        if (gameFragment != null && gameFragment.isDetached()) {
            getFragmentManager().beginTransaction().attach(gameFragment).commit();
        }
    }

    public void hideGameFragment() {
        if (gameFragment != null && gameFragment.isVisible()) {
            getFragmentManager().beginTransaction().detach(gameFragment).commit();
        }
    }
}
