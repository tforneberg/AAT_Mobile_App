package de.tforneberg.aatapp.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import java.util.Objects;

import de.tforneberg.aatapp.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        TextView aboutText = findViewById(R.id.aboutText);
        aboutText.setText(getText(R.string.app_info_text));
        aboutText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView thirdParty1 = findViewById(R.id.thirdParty1);
        TextView thirdParty2 = findViewById(R.id.thirdParty2);
        TextView thirdParty3 = findViewById(R.id.thirdParty3);

        thirdParty1.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(AboutActivity.this).create();
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttonOk), (dialogInterface, i) -> {});
            dialog.setMessage(getString(R.string.license_glide));
            dialog.show();
        });

        thirdParty2.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(AboutActivity.this).create();
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttonOk), (dialogInterface, i) -> {});
            dialog.setMessage(getString(R.string.license_touchImageView));
            dialog.show();
        });

        thirdParty3.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(AboutActivity.this).create();
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.buttonOk), (dialogInterface, i) -> {});
            dialog.setMessage(getString(R.string.license_fastCSV));
            dialog.show();
        });
    }
}
