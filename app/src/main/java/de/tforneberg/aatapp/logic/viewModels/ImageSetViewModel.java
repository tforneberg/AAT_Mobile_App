package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.util.List;

import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.repo.ImageRepository;
import de.tforneberg.aatapp.repo.Repository;

public class ImageSetViewModel extends AndroidViewModel {
    private Repository repository;
    private LiveData<List<Image>> images;
    private LiveData<ImageSet> imageSet;

    public ImageSetViewModel(Application application) {
        super(application);
        repository = Repository.getInstance(application);
    }

    public void initializeWithImageSetId(long imageSetId) {
        imageSet = repository.IMAGES.getImageSetById(imageSetId);
        images = repository.IMAGES.getImagesByImageSetId(imageSetId);
    }

    public LiveData<List<Image>> getImages() {
        return images;
    }

    public Uri getUriFromImage(Image image, Context context) {
        File file = new File(context.getFilesDir() + "/" + image.getPath());
        return Uri.parse(file.getAbsolutePath());
    }

    public File getFileFromImage(Image image, Context context) {
        return new File(context.getFilesDir() + "/" + image.getPath());
    }

    public LiveData<ImageSet> getImageSet() {
        return imageSet;
    }

    public void updateImages(Image... images) { repository.IMAGES.updateImages(images); }

    public void deleteImage(Image image, Context context) { repository.IMAGES.deleteImage(image, context); }

    public void addImage(Context context, Uri uri, String filename, boolean newImagePush,
                         ImageSet imageSet, ImageRepository.ImageAddedListener listener) {
        //get the data type of the image (e.g. ".jpeg", ...)
        String type = getApplication().getContentResolver().getType(uri);

        repository.IMAGES.addImage(context, uri, filename, type, newImagePush, imageSet, listener);
    }
}
