package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "images")
public class Image {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;

    @ColumnInfo(name="path")
    @NonNull
    private String path;

    @ColumnInfo(name="push")
    @NonNull
    private Boolean push;

    @Ignore
    public Image(@NonNull String path, @NonNull Boolean push) {
        this.path = path;
        this.push = push;
    }

    public Image(long id, @NonNull String path, @NonNull Boolean push) {
        this.id = id;
        this.path = path;
        this.push = push;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    @NonNull
    public Boolean getPush() {
        return push;
    }

    public void setPush(@NonNull Boolean push) {
        this.push = push;
    }
}
