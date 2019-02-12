package de.tforneberg.aatapp.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.ImageSetViewModel;
import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.repo.ImageRepository;
import de.tforneberg.aatapp.ui.adapters.ImageItemListAdapter;
import de.tforneberg.aatapp.ui.adapters.ImageItemListAdapterInterface;
import de.tforneberg.aatapp.ui.adapters.TextItemListAdapter;

public class ImageSetActivity extends AppCompatActivity implements ImageItemListAdapterInterface, ImageRepository.ImageAddedListener {
    public static final int PICK_IMAGE_REQUEST_CODE = 1;
    public static final int SHOW_IMAGE_REQUEST_CODE = 2;

    private TextItemListAdapter listAdapter;
    private ImageSetViewModel viewModel;
    private ProgressBar progressBar;

    private boolean newImagePush;
    private long imageSetId = 0L;
    private ImageSet imageSet;
    private int amountOfImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_image_set);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //get the imageSetId from the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) { // Try to load id from intent
            imageSetId = extras.getLong("imageSetId");
        }
        if (imageSetId == 0L) finish(); // Not found? Finish!

        // Setup recycler view
        RecyclerView recyclerView = findViewById(R.id.imageSet_recyclerView);
        List<String> actions = Collections.singletonList(getString(R.string.menu_action_changeImagePushPull));
        listAdapter = new ImageItemListAdapter(this, this,
                actions, actions, TextItemListAdapter.Action.Delete);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        // Setup progress bar
        progressBar = findViewById(R.id.progress_loader);

        // Setup viewModel and data subscription (adapter->viewModel->Repository->Database)
        viewModel = ViewModelProviders.of(this).get(ImageSetViewModel.class);
        viewModel.initializeWithImageSetId(imageSetId);
        viewModel.getImages().observe(this, images -> {
            if (images != null) {
                listAdapter.setListObjects(images);
                amountOfImages = images.size();
            }
        });
        viewModel.getImageSet().observe(this, imageSet -> {
            if (getSupportActionBar() != null && imageSet != null) {
                getSupportActionBar().setTitle(imageSet.getName());
                ImageSetActivity.this.imageSet = imageSet;
            }
        });

        // Setup add button
        FloatingActionButton fab = findViewById(R.id.btn_addImage);
        fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ImageSetActivity.this);
            builder.setTitle(R.string.imageForPushOrPull);
            builder.setPositiveButton(R.string.push, (dialog, which) -> {
                newImagePush = true;
                dialog.dismiss();
                startImageSelectActivity();
            });
            builder.setNeutralButton(R.string.pull, (dialog, which) -> {
                newImagePush = false;
                dialog.dismiss();
                startImageSelectActivity();
            });
            builder.show();
        });
    }

    private void startImageSelectActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.selectNewImage)), PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            progressBar.setVisibility(View.VISIBLE);
            ArrayList<Uri> uriList = new ArrayList<>();

            if (data.getClipData() != null) { //several images selected
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    uriList.add(uri);
                }
            } else { //only one image selected
                uriList.add(data.getData());
            }

            for (Uri uri : uriList) {
                String filename = imageSetId + "_" + (++amountOfImages);
                try {
                    viewModel.addImage(this, uri, filename, newImagePush, imageSet, this);
                } catch (Exception e) {
                    onImageAdded(false);
                }
            }
        }
    }

    @Override
    public void onListItemSelected(Object item) {
        Intent showImageIntent = new Intent(ImageSetActivity.this, ShowImageActivity.class);
        showImageIntent.putExtra("imageId", ((Image) item).getId());
        showImageIntent.putExtra("imageSetId", imageSetId);
        startActivityForResult(showImageIntent, SHOW_IMAGE_REQUEST_CODE);
    }

    @Override
    public void handleListItemAction(Object item, TextItemListAdapter.Action action, String customActionString) {
        if (action.equals(TextItemListAdapter.Action.Delete)) {
            viewModel.deleteImage((Image) item, this);
        } else if (customActionString != null) {
            if (customActionString.equals(getString(R.string.menu_action_changeImagePushPull))) {
                if (item instanceof  Image) {
                    //only one image selected
                    Image image = (Image) item;
                    image.setPush(!image.getPush());
                    viewModel.updateImages(image);
                } else if (item instanceof List) {
                    //several images selected
                    List list = (List) item;
                    for (Object object : list) {
                        if (object instanceof Image) {
                            Image image = (Image) object;
                            image.setPush(!image.getPush());
                            viewModel.updateImages(image);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getListItemTitle(Object item) {
        return ((Image) item).getPath();
    }

    @Override
    public String getListItemDescription(Object item) {
        return ((Image) item).getPush() ? getString(R.string.push) : getString(R.string.pull);
    }

    @Override
    public File getListItemImageFile(Object item) {
        return viewModel.getFileFromImage((Image) item, this);
    }

    @Override
    public void onImageAdded(boolean success) {
        progressBar.setVisibility(View.GONE);
        if (!success) {
            Toast.makeText(this, R.string.couldNotImportImage, Toast.LENGTH_SHORT).show();
        }
    }

}
