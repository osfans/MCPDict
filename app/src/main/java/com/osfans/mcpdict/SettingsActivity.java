package com.osfans.mcpdict;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.setLocale(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            MultiSelectListPreference msp = findPreference(getString(R.string.pref_key_custom_languages));
            boolean visible = MCPDatabase.getLanguages() != null;
            if (msp != null) {
                msp.setVisible(visible);
                if (visible) {
                    msp.setEntries(MCPDatabase.getLanguages().toArray(new String[0]));
                    msp.setEntryValues(MCPDatabase.getFields().toArray(new String[0]));
                }
            }
        }
    }
}
