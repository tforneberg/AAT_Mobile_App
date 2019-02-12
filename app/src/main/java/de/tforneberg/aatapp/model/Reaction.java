package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "reactions",
        foreignKeys = {
                @ForeignKey(entity = SessionRound.class,
                        onDelete = ForeignKey.CASCADE,
                        parentColumns = "id",
                        childColumns = "session_round_id")},
        indices = {@Index(value = {"session_round_id"})}
)
public class Reaction {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;

    @ColumnInfo(name="session_round_id")
    private long sessionRoundId;

    private long firstReactionTime;

    private long finalReactionTime;

    private boolean isActionPush;

    private boolean isActionCorrect;

    private long imageId;

    private String imageName;

    public Reaction(long id, long sessionRoundId, long firstReactionTime, long finalReactionTime,
                    boolean isActionPush, boolean isActionCorrect, long imageId, String imageName) {
        this.id = id;
        this.sessionRoundId = sessionRoundId;
        this.firstReactionTime = firstReactionTime;
        this.finalReactionTime = finalReactionTime;
        this.isActionPush = isActionPush;
        this.isActionCorrect = isActionCorrect;
        this.imageId = imageId;
        this.imageName = imageName;
    }

    @Ignore
    public Reaction(long sessionRoundId, long firstReactionTime, long finalReactionTime, boolean isActionPush, boolean isActionCorrect, long imageId, String imageName) {
        this.sessionRoundId = sessionRoundId;
        this.firstReactionTime = firstReactionTime;
        this.finalReactionTime = finalReactionTime;
        this.isActionPush = isActionPush;
        this.isActionCorrect = isActionCorrect;
        this.imageId = imageId;
        this.imageName = imageName;
    }

    public long getId() {
        return id;
    }

    public long getSessionRoundId() {
        return sessionRoundId;
    }

    public long getFirstReactionTime() {
        return firstReactionTime;
    }

    public void setFirstReactionTime(long firstReactionTime) {
        this.firstReactionTime = firstReactionTime;
    }

    public long getFinalReactionTime() {
        return finalReactionTime;
    }

    public void setFinalReactionTime(long finalReactionTime) {
        this.finalReactionTime = finalReactionTime;
    }

    public boolean isActionPush() {
        return isActionPush;
    }

    public void setActionPush(boolean actionPush) {
        isActionPush = actionPush;
    }

    public boolean isActionCorrect() {
        return isActionCorrect;
    }

    public void setActionCorrect(boolean actionCorrect) {
        isActionCorrect = actionCorrect;
    }

    public long getImageId(){
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

}
