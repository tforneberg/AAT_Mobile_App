package de.tforneberg.aatapp.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

public class ImageSetWithImages {

    @Embedded
    private ImageSet imageSet;

    @Relation(parentColumn = "id", entityColumn = "imageSetId", entity = ImageSetImageConnection.class, projection = "imageId")
    private List<Integer> imageIdList;

    public ImageSetWithImages(ImageSet imageSet, List<Integer> imageIdList) {
        this.imageSet = imageSet;
        this.imageIdList = imageIdList;
    }

    public ImageSet getImageSet() {
        return imageSet;
    }

    public void setImageSet(ImageSet imageSet) {
        this.imageSet = imageSet;
    }

    public List<Integer> getImageIdList() {
        return imageIdList;
    }

    public void setImageIdList(List<Integer> imageIdList) {
        this.imageIdList = imageIdList;
    }
}

