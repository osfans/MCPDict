package com.osfans.mcpdict;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale.setDefault(Locale.KOREA);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }
}
