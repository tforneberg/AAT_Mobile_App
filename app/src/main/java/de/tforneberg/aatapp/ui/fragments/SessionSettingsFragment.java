package de.tforneberg.aatapp.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import java.util.Objects;

import de.tforneberg.aatapp.R;

public class SessionSettingsFragment extends PreferenceFragmentCompat {

    Preference[] ratioRounds = new Preference[5];

    @Override
    public void onCreatePreferencesFix(Bundle bundle, String s) {
        // Load the preferences from XML resource
        addPreferencesFromResource(R.xml.session_preferences);

        for (int i = 0; i < 5; i ++) {
            int j = i + 1;
            ratioRounds[i] = findPreference("ratio_push_pull_round"+j);
        }

        findPreference("amountOfRounds").setOnPreferenceChangeListener((preference, o) -> {
            setVisibilities(Integer.parseInt((String) o));
            return true;
        });

        setVisibilities();
    }

    private void setVisibilities() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        if (prefs != null) {
            String amountOfRoundsString = Objects.requireNonNull(prefs.getString("amountOfRounds", "1"));
            int amountOfRounds = Integer.parseInt(amountOfRoundsString);

            setVisibilities(amountOfRounds);
        }
    }

    private void setVisibilities(int newAmountOfRounds) {
        for (Preference p : ratioRounds) {
            p.setVisible(false);
        }

        for (int i = 0; i < newAmountOfRounds; i ++) {
            ratioRounds[i].setVisible(true);
        }
    }
}
