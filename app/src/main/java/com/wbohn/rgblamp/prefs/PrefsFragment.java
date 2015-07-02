package com.wbohn.rgblamp.prefs;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.wbohn.rgblamp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrefsFragment extends PreferenceFragment {

    private String[] pairedDeviceNames;
    private String[] pairedDeviceAddresses;

    public interface PrefsFragmentInterface {
        void fadeTypeChanged();
    }
    private PrefsFragmentInterface prefsFragmentInterface;

    public static PrefsFragment newInstance(String[] pairedDeviceNames, String[] pairedDeviceAddresses) {

        PrefsFragment prefsFragment = new PrefsFragment();

        Bundle args = new Bundle();
        args.putStringArray("pairedDeviceNames", pairedDeviceNames);
        args.putStringArray("pairedDeviceAddresses", pairedDeviceAddresses);

        prefsFragment.setArguments(args);
        return prefsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.app_preferences);

        pairedDeviceNames = getArguments().getStringArray("pairedDeviceNames");
        pairedDeviceAddresses = getArguments().getStringArray("pairedDeviceAddresses");

        ListPreference listPreference = (ListPreference) findPreference("default_device");
        listPreference.setEntries(pairedDeviceNames);
        listPreference.setEntryValues(pairedDeviceAddresses);

        bindPreferenceSummaryToValue(findPreference("fade_steps"));
        bindPreferenceSummaryToValue(findPreference("default_device"));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        prefsFragmentInterface = (PrefsFragmentInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        prefsFragmentInterface = null;
    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
                // Notify the lamp
                prefsFragmentInterface.fadeTypeChanged();
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }
}
