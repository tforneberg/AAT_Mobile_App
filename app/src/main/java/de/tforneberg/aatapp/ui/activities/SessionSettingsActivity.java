package de.tforneberg.aatapp.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.ui.fragments.SessionSettingsFragment;

public class SessionSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_settings);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction().replace(R.id.pref_content, new SessionSettingsFragment()).commit();
    }
}
