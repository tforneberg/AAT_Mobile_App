package de.tforneberg.aatapp.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tforneberg.aatapp.model.ImageSet;

/**
 * The DataAccessObject (Dao) interface definition for ImageSet related database content.
 * It defines all methods to access the database for this content and (via Annotations)
 * connects them with SQL-Queries.
 */
@Dao
public interface ImageSetDao {
    @Query("SELECT * FROM image_sets WHERE id = :id")
    LiveData<ImageSet> getImageSetById(long id);

    @Query("SELECT * FROM image_sets WHERE id = :id")
    ImageSet getImageSetByIdSync(long id);

    @Query("SELECT * FROM image_sets")
    LiveData<List<ImageSet>> getAllImageSets();

    @Query("SELECT * FROM image_sets WHERE active = 1")
    LiveData<ImageSet> getActiveImageSet();

    @Query("UPDATE image_sets SET active = 0 WHERE active = 1")
    void deactivateCurrentlyActivatedImageSet();

    @Insert
    long insertImageSet(ImageSet imageSet);

    @Update
    void updateImageSets(ImageSet... imageSet);

    @Delete
    void deleteImageSets(ImageSet... imageSet);

    @Query("DELETE FROM image_sets")
    void deleteAllImageSets();
}
