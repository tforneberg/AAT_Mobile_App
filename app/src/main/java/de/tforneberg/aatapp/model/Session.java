package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Date;

@Entity(tableName = "sessions",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        onDelete = ForeignKey.NO_ACTION,
                        parentColumns = "id",
                        childColumns = "user_id"),
                @ForeignKey(entity = ImageSet.class,
                        onDelete = ForeignKey.NO_ACTION,
                        parentColumns = "id",
                        childColumns = "image_set_id")},
        indices = {@Index(value={"user_id"}) })
public class Session {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;

    @ColumnInfo(name="user_id")
    private long userId;

    @ColumnInfo(name="image_set_id")
    private long imageSetId = -1;

    @NonNull
    private String gestureMode;

    private boolean isLandscape;

    private boolean hasColoredBorder;

    private String borderColorPush;

    private String borderColorPull;

    private boolean hasRotationAngle;

    private int rotationAnglePush;

    private int rotationAnglePull;

    private long timeBetweenImages;

    @NonNull
    private String notificationType;

    @NonNull
    private Date date;

    public Session(long id, long userId, long imageSetId,
                   @NonNull String gestureMode, boolean isLandscape,
                   boolean hasColoredBorder, String borderColorPush,
                   String borderColorPull, boolean hasRotationAngle,
                   int rotationAnglePush, int rotationAnglePull,
                   long timeBetweenImages, @NonNull String notificationType,
                   @NonNull Date date) {
        this.id = id;
        this.userId = userId;
        this.imageSetId = imageSetId;
        this.gestureMode = gestureMode;
        this.isLandscape = isLandscape;
        this.hasColoredBorder = hasColoredBorder;
        this.borderColorPush = borderColorPush;
        this.borderColorPull = borderColorPull;
        this.hasRotationAngle = hasRotationAngle;
        this.rotationAnglePush = rotationAnglePush;
        this.rotationAnglePull = rotationAnglePull;
        this.timeBetweenImages = timeBetweenImages;
        this.notificationType = notificationType;
        this.date = date;
    }

    @Ignore
    public Session(long userId, long imageSetId,
                   @NonNull String gestureMode, boolean isLandscape,
                   boolean hasColoredBorder, String borderColorPush,
                   String borderColorPull, boolean hasRotationAngle,
                   int rotationAnglePush, int rotationAnglePull,
                   long timeBetweenImages, @NonNull String notificationType,
                   @NonNull Date date) {
        this.userId = userId;
        this.imageSetId = imageSetId;
        this.gestureMode = gestureMode;
        this.isLandscape = isLandscape;
        this.hasColoredBorder = hasColoredBorder;
        this.borderColorPush = borderColorPush;
        this.borderColorPull = borderColorPull;
        this.hasRotationAngle = hasRotationAngle;
        this.rotationAnglePush = rotationAnglePush;
        this.rotationAnglePull = rotationAnglePull;
        this.timeBetweenImages = timeBetweenImages;
        this.notificationType = notificationType;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getImageSetId() {
        return imageSetId;
    }

    public void setImageSetId(long imageSetId) {
        this.imageSetId = imageSetId;
    }

    @NonNull
    public String getGestureMode() {
        return gestureMode;
    }

    public void setGestureMode(@NonNull String gestureMode) {
        this.gestureMode = gestureMode;
    }


    public boolean isLandscape() {
        return isLandscape;
    }

    public void setLandscape(boolean landscape) {
        isLandscape = landscape;
    }


    public boolean isHasColoredBorder() {
        return hasColoredBorder;
    }

    public void setHasColoredBorder(boolean hasColoredBorder) {
        this.hasColoredBorder = hasColoredBorder;
    }

    public String getBorderColorPush() {
        return borderColorPush;
    }

    public void setBorderColorPush(String borderColorPush) {
        this.borderColorPush = borderColorPush;
    }

    public String getBorderColorPull() {
        return borderColorPull;
    }

    public void setBorderColorPull(String borderColorPull) {
        this.borderColorPull = borderColorPull;
    }

    public boolean isHasRotationAngle() {
        return hasRotationAngle;
    }

    public void setHasRotationAngle(boolean hasRotationAngle) {
        this.hasRotationAngle = hasRotationAngle;
    }

    public int getRotationAnglePush() {
        return rotationAnglePush;
    }

    public void setRotationAnglePush(int rotationAnglePush) {
        this.rotationAnglePush = rotationAnglePush;
    }

    public int getRotationAnglePull() {
        return rotationAnglePull;
    }

    public void setRotationAnglePull(int rotationAnglePull) {
        this.rotationAnglePull = rotationAnglePull;
    }

    public long getTimeBetweenImages() {
        return timeBetweenImages;
    }

    public void setTimeBetweenImages(long timeBetweenImages) {
        this.timeBetweenImages = timeBetweenImages;
    }

    @NonNull
    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(@NonNull String notificationType) {
        this.notificationType = notificationType;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = date;
    }
}
