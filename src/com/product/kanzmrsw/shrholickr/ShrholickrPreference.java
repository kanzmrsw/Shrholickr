
package com.product.kanzmrsw.shrholickr;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ShrholickrPreference extends PreferenceActivity {
    private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("list_key")) {
                findPreference("list_key").setSummary(sharedPreferences.getString(key, "40"));
            } else if (key.equals("service_key")) {
                findPreference("service_key").setSummary(
                        getResources().getStringArray(R.array.service_entries)[Integer
                                .valueOf(sharedPreferences.getString(key, "0"))]);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);

        addPreferencesFromResource(R.xml.pref);
        findPreference("list_key").setSummary(
                getPreferenceScreen().getSharedPreferences().getString("list_key", "40"));
        findPreference("service_key").setSummary(
                getResources().getStringArray(R.array.service_entries)[Integer
                        .valueOf(getPreferenceScreen().getSharedPreferences().getString(
                                "service_key", "0"))]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(
                listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                listener);
    }
}
