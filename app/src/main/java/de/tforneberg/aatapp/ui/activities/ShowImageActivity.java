package de.tforneberg.aatapp.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ortiz.touchview.TouchImageView;

import java.util.Objects;

import de.tforneberg.aatapp.GlideApp;
import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.ShowImageViewModel;

public class ShowImageActivity extends AppCompatActivity {
    private TouchImageView imageView;
    private ShowImageViewModel viewModel;
    private long imageId = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            imageId = extras.getLong("imageId");
        }
        if(imageId == 0L) finish();

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Setup image view
        imageView = findViewById(R.id.imageView);

        // Setup data subscription
        viewModel = ViewModelProviders.of(this).get(ShowImageViewModel.class);
        viewModel.getImageById(imageId).observe(this, image -> {
            if (image != null) {
                getSupportActionBar().setTitle(image.getPath());
                GlideApp.with(ShowImageActivity.this)
                        .load(viewModel.getFileFromImage(ShowImageActivity.this, image))
                        .into(imageView);
            }
        });
    }
}
