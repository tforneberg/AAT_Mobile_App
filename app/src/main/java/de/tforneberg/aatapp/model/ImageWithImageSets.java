package de.tforneberg.aatapp.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class ImageWithImageSets {

    @Embedded
    private Image image;

    @Relation(parentColumn = "id", entityColumn = "imageId", entity = ImageSetImageConnection.class, projection = "imageSetId")
    private List<ImageSet> imageSetIdList;

    public ImageWithImageSets(Image image, List<ImageSet> imageSetIdList) {
        this.image = image;
        this.imageSetIdList = imageSetIdList;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<ImageSet> getImageSetIdList() {
        return imageSetIdList;
    }

    public void setImageSetIdList(List<ImageSet> imageSetIdList) {
        this.imageSetIdList = imageSetIdList;
    }
}
