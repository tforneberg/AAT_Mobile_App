package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "users",
        indices = {@Index(value = "name", unique = true)})
public class User {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;

    @ColumnInfo(name="name")
    @NonNull
    private String name;

    @ColumnInfo(name="password")
    private byte[] password;

    @ColumnInfo(name="salt")
    private byte[] salt;

    @ColumnInfo(name="is_admin")
    private boolean isAdmin;

    @Ignore
    public User(@NonNull String name, byte[] password, byte[] salt, boolean isAdmin) {
        this.name = name;
        this.password = password;
        this.salt = salt;
        this.isAdmin = isAdmin;
    }

    public User(long id, @NonNull String name, @NonNull byte[] password, byte[] salt, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.salt = salt;
        this.isAdmin = isAdmin;
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

    @NonNull
    public byte[] getPassword() {
        return password;
    }

    public void setPassword(@NonNull byte[] password) {
        this.password = password;
    }

    public boolean isAdmin() { return isAdmin; }

    public void setAdmin(boolean admin) { isAdmin = admin; }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }
}
