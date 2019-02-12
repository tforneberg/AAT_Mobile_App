package de.tforneberg.aatapp.logic.viewModels;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.repo.Repository;

public class ImageSetsViewModel extends AndroidViewModel {

    private Repository repository;

    private LiveData<List<ImageSet>> imageSets;

    public ImageSetsViewModel(Application application) {
        super(application);
        repository = Repository.getInstance(application);

        imageSets = repository.IMAGES.getAllImageSets();
    }

    public LiveData<List<ImageSet>> getAllImageSets() {
        return imageSets;
    }

    public void addImageSet(ImageSet imageSet) {
        repository.IMAGES.insertImageSet(imageSet);
    }

    public void updateImageSets(ImageSet... imageSets) { repository.IMAGES.updateImageSets(imageSets);}

    public void setNewActiveImageSet(ImageSet newOne, ImageSet oldOne) { repository.IMAGES.setNewActiveImageSet(newOne, oldOne);}

    public void deleteImageSet(Activity activity, ImageSet imageSet) { repository.IMAGES.deleteImageSet(activity, imageSet); }
}
