package de.tforneberg.aatapp.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSetImageConnection;

/**
 * The DataAccessObject (Dao) interface definition for Image related database content.
 * It defines all methods to access the database for this content and (via Annotations)
 * connects them with SQL-Queries.
 */
@Dao
public interface ImageDao {
    @Query("SELECT * FROM images WHERE id = :id")
    LiveData<Image> getImageById(long id);

    @Query("SELECT * FROM images")
    LiveData<List<Image>> getAllImages();

    @Query("SELECT images.id, images.path, images.push FROM images " +
            "INNER JOIN image_set_image ON images.id = image_set_image.image_id " +
            "INNER JOIN image_sets ON image_set_image.image_set_id = image_sets.id " +
            "WHERE image_sets.id LIKE :imageSetId")
    LiveData<List<Image>> getImagesByImageSetId(long imageSetId);

    @Query("SELECT images.id, images.path, images.push FROM images " +
            "INNER JOIN image_set_image ON images.id = image_set_image.image_id " +
            "INNER JOIN image_sets ON image_set_image.image_set_id = image_sets.id " +
            "WHERE image_sets.id LIKE :imageSetId")
    List<Image> getImagesByImageSetIdSync(long imageSetId);

    @Insert
    long insertImage(Image image);

    @Insert
    void insertImageSetImageConnection(ImageSetImageConnection imageSetImage);

    @Update
    void updateImages(Image... image);

    @Delete
    void deleteImages(Image... image);

    @Query("DELETE FROM images")
    void deleteAllImages();
}