package com.wbohn.rgblamp.prefs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.wbohn.rgblamp.App;
import com.wbohn.rgblamp.bus.MessageBuilder;

public class PrefsActivity extends Activity implements PrefsFragment.PrefsFragmentInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        PrefsFragment prefsFragment = PrefsFragment.newInstance(
                intent.getStringArrayExtra("pairedDeviceNames"),
                intent.getStringArrayExtra("pairedDeviceAddresses"));
        getFragmentManager().beginTransaction().replace(android.R.id.content, prefsFragment).commit();
    }

    @Override
    public void fadeTypeChanged(String stringValue) {
        getIntent().putExtra("fadeTypeChanged", true);
        setResult(RESULT_OK, getIntent());
    }
}
