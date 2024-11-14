package com.osfans.mcpdict;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLocale();
        Utils.setActivityTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        MultiSelectListPreference mCustomLanguages;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            mCustomLanguages = findPreference(getString(R.string.pref_key_custom_languages));
            ListPreference lp = findPreference(getString(R.string.pref_key_fq));
            String[] entries = DB.getFqColumns();
            if (entries != null) {
                lp.setEntries(entries);
                lp.setEntryValues(entries);
            }
            initCustomLanguages();
        }

        private void initCustomLanguages() {
            String[] languages = DB.getLanguages();
            if (mCustomLanguages != null && languages != null) {
                mCustomLanguages.setEntries(languages);
                mCustomLanguages.setEntryValues(languages);
            }
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            if (preference.getKey().equals(getString(R.string.pref_key_custom_languages))) initCustomLanguages();
            super.onDisplayPreferenceDialog(preference);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (s.contentEquals(getString(R.string.pref_key_fq)) || s.contentEquals(getString(R.string.pref_key_locale)) || s.contentEquals(getString(R.string.pref_key_font))) {
                if (s.contentEquals(getString(R.string.pref_key_font))) Utils.refreshTypeface();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (s.contentEquals(getString(R.string.pref_key_tone_display)) || s.contentEquals(getString(R.string.pref_key_tone_value_display))) {
                Orthography.setToneStyle(Utils.getToneStyle(R.string.pref_key_tone_display));
                Orthography.setToneValueStyle(Utils.getToneStyle(R.string.pref_key_tone_value_display));
            } else if (s.contentEquals(getString(R.string.pref_key_format))) {
                //TODO: restart
            }
        }
    }
}
