package de.tforneberg.aatapp.ui.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.util.DownloadSettingsTask;
import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.model.Settings;
import de.tforneberg.aatapp.model.User;
import de.tforneberg.aatapp.repo.Repository;
import de.tforneberg.aatapp.ui.dialogs.TextInputDialog;

public class MainActivity extends AppCompatActivity implements DownloadSettingsTask.Listener {

    private Button startButton;
    private Button resultsButton;

    private Repository repository;
    private SharedPreferences sharedPreferences;

    private ImageSet imageSet;
    private List<Image> images;

    private ActionBar actionBar;
    private User user;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize the default values for the preferences at the first startup
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        repository = Repository.getInstance(getApplication());

        setContentView(R.layout.activity_main);

        setSupportActionBar(findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();

        startButton = findViewById(R.id.start_button);
        resultsButton = findViewById(R.id.results_button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //check if user is logged in, otherwise go to LoginActivity
        long userId = sharedPreferences.getLong("user", -1);
        if (userId == -1) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        repository.USERS.getUserById(userId).observe(this, user -> {
            this.user = user;
            if (user != null && actionBar != null) {
                actionBar.setSubtitle(getString(R.string.user_logged_in_prefix)+user.getName());
                //don't show settings if the user is not an admin user
                if (!user.isAdmin() && menu != null) {
                    menu.removeItem(R.id.action_settings);
                }
            }
        });

        repository.IMAGES.getActiveImageSet().observe(this, this::onImageSetLoaded);

        startButton.setOnClickListener(v -> {
            if (imageSet == null) {
                //No image set selected
                Toast.makeText(MainActivity.this, R.string.noImageSetSelected, Toast.LENGTH_LONG).show();
            } else if (imageSet.getId() == 1) {
                //Default image set selected
                startActivity(new Intent(MainActivity.this, SessionRunningActivity.class));
            } else if (images == null) {
                //Not enough images
                Toast.makeText(MainActivity.this, R.string.noImagesInImageSet, Toast.LENGTH_LONG).show();
            } else {
                int goodImages = 0;
                int badImages = 0;
                for (Image i : images) {
                    if (i.getPush()) {
                        badImages++;
                    } else {
                        goodImages++;
                    }
                }
                if (goodImages == 0 || badImages == 0) {
                    //no good or bad images
                    Toast.makeText(MainActivity.this, R.string.noImagesInImageSet, Toast.LENGTH_LONG).show();
                } else {
                    //all good!
                    startActivity(new Intent(MainActivity.this, SessionRunningActivity.class));
                }
            }
        });

        resultsButton.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, SessionListActivity.class))
        );
    }

    private void onImageSetLoaded(ImageSet imageSet) {
        this.imageSet = imageSet;
        if(imageSet != null && imageSet.getId() != 1) { //if imageset is seleted (not default image set!)
            LiveData<List<Image>> liveDataImages = repository.IMAGES.getImagesByImageSetId(imageSet.getId());
            liveDataImages.observe(MainActivity.this, (newImages) -> this.images = newImages);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;

        if (user != null && !user.isAdmin()) {
            menu.removeItem(R.id.action_settings);
        } else if (user != null && user.isAdmin()) {
            menu.removeItem(R.id.action_load_config);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: // User chose the "Settings" item, show the SettingsActivity
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_about:  //User chose the "About" item, show the AboutActivity
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_logout: //User chose the "Log out" item, log out and how LoginActivity
                sharedPreferences.edit().putLong("user", -1).apply();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                return true;
            case R.id.action_load_config: //User chose the "Import settings" item, try to load it
                importSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("SetTextI18n")
    private void importSettings() {
        TextInputDialog textInputDialog = TextInputDialog.create(this,
                R.string.enter_url_for_download, "", 128);
        textInputDialog.getEditText().setText("http://www../aat_app_settings.ser");
        textInputDialog.setNegativeButton(R.string.buttonCancel, (d, w) -> d.cancel());
        textInputDialog.setPositiveButton(R.string.buttonOk, (d, w) -> {
            String url = textInputDialog.getInput();
            new DownloadSettingsTask(MainActivity.this).execute(url);
        });
        textInputDialog.show();
    }

    @Override
    public void settingsDownloadDone(Settings settings) {
        boolean success = false;

        if (settings != null) {
            success = Settings.importSettings(getApplication(), settings, true);
        }

        if(success) {
            Toast.makeText(this, R.string.loading_successfull, Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(this, R.string.error_could_not_load_data, Toast.LENGTH_LONG).show();
        }
    }
}
