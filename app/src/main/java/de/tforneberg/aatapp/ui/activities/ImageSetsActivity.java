package de.tforneberg.aatapp.ui.activities;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.ImageSetsViewModel;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.ui.adapters.ImageSetListAdapter;
import de.tforneberg.aatapp.ui.adapters.ImageSetListAdapterInterface;
import de.tforneberg.aatapp.ui.adapters.TextItemListAdapter;
import de.tforneberg.aatapp.ui.dialogs.TextInputDialog;

public class ImageSetsActivity extends AppCompatActivity implements ImageSetListAdapterInterface, Observer<List<ImageSet>> {

    private TextItemListAdapter listAdapter;
    private ImageSetsViewModel viewModel;
    private List<ImageSet> imageSets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_image_sets);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Setup recycler view
        RecyclerView recyclerView = findViewById(R.id.imageSets_recyclerView);
        listAdapter = new ImageSetListAdapter(this, this,
                TextItemListAdapter.Action.Delete,
                TextItemListAdapter.Action.Rename
        );
        recyclerView.setAdapter(listAdapter);
        ((SimpleItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        // Setup data subscription (adapter->viewModel->Repository->Database)
        viewModel = ViewModelProviders.of(this).get(ImageSetsViewModel.class);
        viewModel.getAllImageSets().observe(this, this);

        // Configure add image set button
        FloatingActionButton addImageSetButton = findViewById(R.id.btn_addImageSet);
        addImageSetButton.setOnClickListener(view -> onAddImageSetButtonClick());
    }

    private void onAddImageSetButtonClick() {
        TextInputDialog dialog = TextInputDialog.create(this,
                R.string.createNewImageSet, R.string.newNameHint, 20
        );
        dialog.setPositiveButton(R.string.buttonOk, (view, which) -> {
            String nameNewImageSet = dialog.getInput();
            if (nameNewImageSet.length() > 0) {
                viewModel.addImageSet(new ImageSet(nameNewImageSet));
            }
        });
        dialog.setNegativeButton(R.string.buttonCancel, (view, which) -> view.cancel());
        dialog.show();
    }

    private void onRenameListItemAction(ImageSet imageSet) {
        TextInputDialog dialog = TextInputDialog.create(this,
                R.string.renameImageSet, R.string.newNameHint, 20
        );
        dialog.setPositiveButton(R.string.buttonOk, (view, which) -> {
            String newName = dialog.getInput();
            if (newName.length() > 0) {
                imageSet.setName(newName);
                viewModel.updateImageSets(imageSet);
            }
        });
        dialog.setNegativeButton(R.string.buttonCancel, (view, which) -> view.cancel());
        dialog.show();
    }

    @Override public void onListItemSelected(Object item) {
        ImageSet imageSet = (ImageSet) item;
        if (imageSet.getId() != 1) { //default image set can not be opened
            Intent manageImageSet = new Intent(ImageSetsActivity.this, ImageSetActivity.class);
            manageImageSet.putExtra("imageSetId", ((ImageSet)item).getId());
            startActivity(manageImageSet);
        }
    }

    @Override public void handleListItemAction(Object item, TextItemListAdapter.Action action, String s) {
        if (((ImageSet)item).getId() != 1) { //default image set can not be edited
            if (action.equals(TextItemListAdapter.Action.Delete)) {
                viewModel.deleteImageSet(this, (ImageSet) item);
            } else if (action.equals(TextItemListAdapter.Action.Rename)){
                onRenameListItemAction((ImageSet) item);
            } else if (action.equals(TextItemListAdapter.Action.Copy)){
                Toast.makeText(this, "Copy ... not implemented yet", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Default image set can not be modified", Toast.LENGTH_LONG).show();
        }
    }

    @Override public String getListItemTitle(Object item) {
        return ((ImageSet)item).getName();
    }

    @Override public String getListItemDescription(Object item) {
        return "";
    }

    @Override
    public boolean getActiveState(Object item) {
        return ((ImageSet) item).isActive();
    }

    @Override
    public void onSwitchToggled(Object item, boolean isChecked) {
        //isChecked already contains the new state after toggling the switch/checkbox
        //If the item is checked now...
        if (isChecked) {
            ImageSet oldActive = null;
            ImageSet newActive = ((ImageSet) item);

            //find the previously selected imageSet and save it in oldActive
            for (ImageSet current : imageSets) {
                if (current.isActive()) {
                    oldActive = current;
                }
            }

            //if an imageSet was selected previously, deselect it.
            if (oldActive != null) {
                oldActive.setActive(false);
            }

            //activate the selection of the new imageSet
            newActive.setActive(true);

            //persist the new state to database
            viewModel.setNewActiveImageSet(newActive, oldActive);
        } else {
            //if the item just got unchecked ...
            //just deactivate the currently selected imageSet if it was clicked
            ImageSet imageSet = (ImageSet) item;
            if (imageSet.isActive()) {
                imageSet.setActive(false);
                viewModel.updateImageSets(imageSet);
            }
        }
    }

    @Override public void onChanged(List<ImageSet> imageSets) {
        this.imageSets = imageSets;
        listAdapter.setListObjects(imageSets);
    }
}
