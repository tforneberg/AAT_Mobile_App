package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "image_sets")
public class ImageSet {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;

    @ColumnInfo(name="name")
    @NonNull
    private String name;

    @ColumnInfo(name="active")
    private boolean active;

    @Ignore
    public ImageSet(@NonNull String name) {
        this.name = name;
        this.active = false;
    }

    public ImageSet(long id, @NonNull String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
