package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

@Entity(tableName = "image_set_image",
        foreignKeys = {
            @ForeignKey(entity = ImageSet.class,
                    onDelete = ForeignKey.CASCADE,
                    parentColumns = "id",
                    childColumns = "image_set_id"),
            @ForeignKey(entity = Image.class,
                    onDelete = ForeignKey.CASCADE,
                    parentColumns = "id",
                    childColumns = "image_id")},
        primaryKeys = {"image_set_id", "image_id"},
        indices = {@Index(value={"image_set_id"}), @Index(value = {"image_id"})})
public class ImageSetImageConnection {
    @ColumnInfo(name="image_set_id")
    @NonNull
    private String imageSetId;

    @ColumnInfo(name="image_id")
    @NonNull
    private String imageId;

    public ImageSetImageConnection(@NonNull String imageSetId, @NonNull String imageId) {
        this.imageSetId = imageSetId;
        this.imageId = imageId;
    }

    @NonNull
    public String getImageSetId() {
        return imageSetId;
    }

    public void setImageSetId(@NonNull String imageSetId) {
        this.imageSetId = imageSetId;
    }

    @NonNull
    public String getImageId() {
        return imageId;
    }

    public void setImageId(@NonNull String imageId) {
        this.imageId = imageId;
    }
}
