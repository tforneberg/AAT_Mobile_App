package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;

import java.io.File;

import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.repo.Repository;

public class ShowImageViewModel extends AndroidViewModel {
    private Repository repository;
    private LiveData<Image> image;

    public ShowImageViewModel (Application application) {
        super(application);
        repository = Repository.getInstance(application);
    }

    private void initializeWithImageId(long imageId) {
        image = repository.IMAGES.getImageById(imageId);
    }

    public LiveData<Image> getImageById(long imageId) {
        if (image == null) {
            initializeWithImageId(imageId);
        }
        return image;
    }

    public File getFileFromImage(Context context, Image image) {
        return repository.IMAGES.getFileFromImage(context, image);
    }
}
