package com.osfans.mcpdict;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.osfans.mcpdict.Orth.Orthography;
import com.osfans.mcpdict.Util.FontUtil;
import com.osfans.mcpdict.Util.Pref;
import com.osfans.mcpdict.Util.App;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.setLocale();
        App.setActivityTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            ListPreference lp = findPreference(getString(R.string.pref_key_fq));
            String[] entries = DB.getFqColumns();
            if (entries != null && lp != null) {
                lp.setEntries(entries);
                lp.setEntryValues(entries);
            }
            lp = findPreference(getString(R.string.pref_key_font));
            if (lp == null) return;
            List<String> packages = FontUtil.getFontPackages();
            if (packages.isEmpty()) return;
            List<String> l = new ArrayList<>();
            l.addAll(Arrays.asList(getResources().getStringArray(R.array.pref_entries_font)));
            l.addAll(FontUtil.getFontNames(packages, false));
            lp.setEntries(l.toArray(new String[0]));
            l.clear();
            l.addAll(Arrays.asList(getResources().getStringArray(R.array.pref_values_font)));
            l.addAll(FontUtil.getFontNames(packages, true));
            lp.setEntryValues(l.toArray(new String[0]));
        }

        @Override
        public void onResume() {
            super.onResume();
            Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if (TextUtils.isEmpty(s)) return;
            if (s.contentEquals(getString(R.string.pref_key_fq)) || s.contentEquals(getString(R.string.pref_key_locale)) || s.contentEquals(getString(R.string.pref_key_font)) || s.contentEquals(getString(R.string.pref_key_custom_title))) {
                if (s.contentEquals(getString(R.string.pref_key_font)) || s.contentEquals(getString(R.string.pref_key_locale))) FontUtil.refreshTypeface();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else if (s.contentEquals(getString(R.string.pref_key_tone_display)) || s.contentEquals(getString(R.string.pref_key_tone_value_display))) {
                Orthography.setToneStyle(Pref.getToneStyle(R.string.pref_key_tone_display));
                Orthography.setToneValueStyle(Pref.getToneStyle(R.string.pref_key_tone_value_display));
            } // TODO: R.string.pref_key_format restart
        }
    }
}
