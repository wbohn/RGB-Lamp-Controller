package com.wbohn.rgblamp;

import android.bluetooth.BluetoothDevice;
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

import com.wbohn.rgblamp.bluetooth.BluetoothConnection;
import com.wbohn.rgblamp.game.GameFragment;
import com.wbohn.rgblamp.bluetooth.BluetoothDialog;
import com.wbohn.rgblamp.prefs.AppPreferences;
import com.wbohn.rgblamp.prefs.PrefsActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends ActionBarActivity implements
        ActionBar.OnNavigationListener,
        MainFragment.MainFragmentInterface,
        BluetoothConnection.BluetoothConnectionInterface,
        BluetoothDialog.BluetoothDialogInterface,
        GameFragment.GameFragmentInterface {

    public static final String TAG = "MainActivity";

    public static final int MODE_SOLID = 0;
    public static final int MODE_FADE = 1;
    public static final int MODE_GAME = 2;

    public static final char SEQUENCE_RECEIVED = '#';
    public static final char LEVEL_SHOWN = '+';

    public static final int PREFS_ACTIVITY_REQUEST = 9;

    private GameFragment gameFragment;
    private MainFragment mainFragment;
    private BluetoothConnection bluetoothConnection;
    private BluetoothDialog bluetoothDialog;

    private AppPreferences appPrefs;

    public int mode = MODE_SOLID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appPrefs = new AppPreferences(this);

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

        mainFragment = (MainFragment) fragmentManager.findFragmentById(R.id.fragment_main);

        if (savedInstanceState == null) {
            gameFragment = GameFragment.newInstance(appPrefs.getHighScore());
            bluetoothConnection = BluetoothConnection.newInstance(appPrefs.getAutoConnect(), appPrefs.getDefaultDeviceAddress());

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", mode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        mode = position;
        onModeChange();
        return true;
    }

    public void onModeChange() {
        if (mode == MODE_GAME) {
            showGameFragment();

        } else {
            hideGameFragment();
        }

        sendModeChangeMessage();
        updateAllBulbs();
        sendFadeSettings();
        mainFragment.setMode(mode);
    }

    public void sendModeChangeMessage() {
        if (bluetoothConnection != null && bluetoothConnection.isConnected()) {
            String modeStr = Integer.toString(mode);
            String msg = "@" + modeStr;
            bluetoothConnection.write(msg);
        }
    }

    public void hideGameFragment() {
        if (gameFragment != null && gameFragment.isVisible()) {
            getFragmentManager().beginTransaction().detach(gameFragment).commit();
        }
    }
    public void showGameFragment() {
        if (gameFragment != null && gameFragment.isDetached()) {
            getFragmentManager().beginTransaction().attach(gameFragment).commit();
        }
    }

    @Override
    public void guessMade(int id) {
        gameFragment.guessMade(id);
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        bluetoothConnection.connect(device);
    }

    @Override
    public void setSequence(int[] sequence) {
        bluetoothConnection.write(Arrays.toString(sequence));
        Log.i(TAG, Arrays.toString(sequence));
    }

    @Override
    public void connected() {
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();

        sendModeChangeMessage();
        updateAllBulbs();
        sendFadeSettings();
    }

    @Override
    public void messageReceived(char msg) {
        Log.i(TAG, String.valueOf(msg));

        switch (msg) {
            case SEQUENCE_RECEIVED:
                Log.i(TAG, "sequenceReceived");
                gameFragment.startGame();
                break;
            case LEVEL_SHOWN:
                Log.i(TAG, "level shown");
                gameFragment.levelShown();
                break;
        }
    }

    @Override
    public void showLevel() {
        bluetoothConnection.write("+");
    }

    @Override
    public void gameOver(int score) {
        Log.i(TAG, "gameOver()");
        bluetoothConnection.write("*");
        int currentHighScore = appPrefs.getHighScore();
        if (score >= currentHighScore) {
            appPrefs.saveHighScore(score);
        }
    }

    @Override
    public void saveHighScore(int score) {
        appPrefs.saveHighScore(score);
    }

    private void sendFadeSettings() {

        if (bluetoothConnection != null && bluetoothConnection.isConnected()) {
            String fadeType = appPrefs.getFadeType();

            String msg = "#" + fadeType;

            bluetoothConnection.write(msg);
        }
    }

    public void updateAllBulbs() {
        int[] colors = mainFragment.getColors();
        for (int i = 0; i < colors.length; i++) {
            updateBulb(i, colors[i]);
        }
    }

    @Override
    public void updateBulb(int index, int color) {
        appPrefs.saveBulbColor(index, color);

        if (bluetoothConnection.isConnected()) {
            String msg = "<" + index + ":" + color + ">";
            bluetoothConnection.write(msg);
            Log.i(TAG, msg);
        }
    }
}
