package de.tforneberg.aatapp.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "session_rounds",
        foreignKeys = {@ForeignKey(entity = Session.class,
                        onDelete = ForeignKey.CASCADE,
                        parentColumns = "id",
                        childColumns = "session_id")},
        indices = {@Index(value={"session_id"})})
public class SessionRound {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    private long id;

    @ColumnInfo(name="session_id")
    private long sessionId;

    public SessionRound(long id, long sessionId) {
        this.id = id;
        this.sessionId = sessionId;
    }

    @Ignore
    public SessionRound(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getId() {
        return id;
    }

    public long getSessionId() {
        return sessionId;
    }
}
