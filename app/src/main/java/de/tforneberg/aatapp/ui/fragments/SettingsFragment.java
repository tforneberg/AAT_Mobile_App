package de.tforneberg.aatapp.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.util.DownloadSettingsTask;
import de.tforneberg.aatapp.model.Settings;
import de.tforneberg.aatapp.ui.dialogs.TextInputDialog;

public class SettingsFragment extends PreferenceFragmentCompat implements DownloadSettingsTask.Listener{

    private static final int IMPORT_SETTINGS_LOCAL_REQUEST_CODE = 1;
    private static final int REQUEST_EXT_STORAGE_FOR_EXPORT = 2;
    private static final int REQUEST_EXT_STORAGE_FOR_IMPORT = 3;

    private Activity activity;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        activity = Objects.requireNonNull(getActivity());

        addGestureModeEntries();

        findPreference("export_prefs").setOnPreferenceClickListener(preference -> {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                if(Build.VERSION.SDK_INT>22){
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXT_STORAGE_FOR_EXPORT);
                }
            } else {
                // Permission granted
                exportData();
            }
            return false;
        });

        findPreference("import_prefs").setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
            builder.setTitle(R.string.load_local_or_remote);
            builder.setPositiveButton(R.string.device, (dialog, which) -> {
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //"Read External Storage" permission is not granted, request it
                    if(Build.VERSION.SDK_INT>22){
                        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXT_STORAGE_FOR_IMPORT);
                    }
                } else {
                    // Permission granted
                    importDataLocal();
                }
            });
            builder.setNeutralButton(R.string.internet, (dialog, which) -> {
                TextInputDialog textInputDialog = TextInputDialog.create(getContext(),
                        R.string.enter_url_for_download, "", 128);
                textInputDialog.getEditText().setText("http://www../aat_app_settings.ser");
                textInputDialog.setNegativeButton(R.string.buttonCancel, (d, w) -> d.cancel());
                textInputDialog.setPositiveButton(R.string.buttonOk, (d, w) -> {
                    String url = textInputDialog.getInput();
                    new DownloadSettingsTask(SettingsFragment.this).execute(url);
                });
                textInputDialog.show();
            });
            builder.show();

            return false;
        });
    }

    /**
     * Adds the gesture modes to the ListPreference so that if certain sensors are not available on
     * the current device, the gesture modes who need these sensors are not displayed in the list.
     */
    private void addGestureModeEntries() {
        List<CharSequence> entries = new ArrayList<>();
        List<CharSequence> entryValues = new ArrayList<>();
        entries.add(getResources().getString(R.string.gestureMode_pinch));
        entryValues.add(getResources().getString(R.string.gestureMode_pinch_val));

        //check if all sensors for AngleGestureMode are available on the current device.
        //If not, don't add it as an option
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (accelerometer != null && geomagneticSensor != null) {
            entries.add(getResources().getString(R.string.gestureMode_angle));
            entryValues.add(getResources().getString(R.string.gestureMode_angle_val));
        }

        //check if accelerometer is available for Move gesture mode. If not, don't add it as an option
        if (accelerometer != null) {
            entries.add(getResources().getString(R.string.gestureMode_move));
            entryValues.add(getResources().getString(R.string.gestureMode_move_val));
        }

        ListPreference gestureModePref = (ListPreference) findPreference("gesture_mode");
        gestureModePref.setEntries(entries.toArray(new CharSequence[0]));
        gestureModePref.setEntryValues(entryValues.toArray(new CharSequence[0]));
    }

    /**
     * Exports the current app settings to a serialized file and starts a share intent with it.
     */
    public void exportData() {
        Application app = Objects.requireNonNull(getActivity()).getApplication();
        if (app != null) {
            Settings settingsToExport = new Settings(app);
            Uri settingsUri = settingsToExport.exportSettings(app);

            Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                    .setStream(settingsUri).getIntent();

            // Provide read access
            shareIntent.setData(settingsUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (shareIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                getActivity().startActivity(shareIntent);
            }
        }
    }

    /**
     * Opens a file chooser intent to import a settings file (=a serialized Settings object)
     */
    public void importDataLocal() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Open Settings file"), IMPORT_SETTINGS_LOCAL_REQUEST_CODE);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMPORT_SETTINGS_LOCAL_REQUEST_CODE) {
            boolean success = false;
            Application app = Objects.requireNonNull(getActivity()).getApplication();

            //try import the settings from the given data
            if (data != null && data.getData() != null) {
                success = Settings.importSettings(app, data.getData(), true);
            }

            displayImportResultMessage(success);
        }
    }

    private void displayImportResultMessage(boolean success) {
        //display message
        if(success) {
            Toast.makeText(getContext(), R.string.loading_successfull, Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(getContext(), R.string.error_could_not_load_data, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void settingsDownloadDone(Settings settings) {
        boolean success = false;
        Application app = Objects.requireNonNull(getActivity()).getApplication();

        if (settings != null) {
            success = Settings.importSettings(app, settings, true);
        }

        displayImportResultMessage(success);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXT_STORAGE_FOR_EXPORT: {
                //Check for valid permission (if request is cancelled, the result arrays are empty)
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, export data
                    exportData();
                }
                break;
            }
            case REQUEST_EXT_STORAGE_FOR_IMPORT: {
                //Check for valid permission (if request is cancelled, the result arrays are empty)
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, import data
                    importDataLocal();
                }
                break;
            }
        }
    }
}
