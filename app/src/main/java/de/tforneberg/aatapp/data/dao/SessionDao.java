package de.tforneberg.aatapp.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.SessionRound;

/**
 * The DataAccessObject (Dao) interface definition for Session,
 * SessionRound and Reaction related database content.
 * It defines all methods to access the database for this content and (via Annotations)
 * connects them with SQL-Queries.
 */
@Dao
public interface SessionDao {
    @Query("SELECT * FROM session_rounds WHERE id = :id")
    LiveData<SessionRound> getSessionRoundById(long id);

    @Query("SELECT * FROM session_rounds WHERE session_id = :id")
    LiveData<List<SessionRound>> getSessionRoundsBySessionId(long id);

    @Query("SELECT * FROM session_rounds WHERE session_id = :id")
    List<SessionRound> getSessionRoundsBySessionIdSync(long id);

    @Query("SELECT * FROM session_rounds")
    LiveData<List<SessionRound>> getAllSessionRounds();

    @Query("SELECT * FROM sessions WHERE id = :id")
    LiveData<Session> getSessionById(long id);

    @Query("SELECT * FROM sessions WHERE id = :id")
    Session getSessionByIdSync(long id);

    @Query("SELECT * FROM sessions WHERE user_id = :userId")
    LiveData<List<Session>> getSessionsByUserId(long userId);

    @Query("SELECT * FROM sessions WHERE user_id = :userId")
    List<Session> getSessionsByUserIdSync(long userId);

    @Query("SELECT * FROM sessions")
    LiveData<List<Session>> getAllSessions();

    @Query("SELECT * FROM sessions")
    List<Session> getAllSessionsSync();

    @Query("SELECT * FROM reactions WHERE id = :id")
    LiveData<Reaction> getReactionById(long id);

    @Query("SELECT * FROM reactions WHERE session_round_id = :id")
    LiveData<List<Reaction>> getReactionsBySessionRoundId(long id);

    @Query("SELECT * FROM reactions WHERE session_round_id = :id")
    List<Reaction> getReactionsBySessionRoundIdSync(long id);

    @Query("SELECT * FROM reactions")
    LiveData<List<Reaction>> getAllReactions();

    @Insert
    long insertSessionRound(SessionRound sessionRound);

    @Insert
    long insertSession(Session session);

    @Insert
    Long[] insertReactions(Reaction... reaction);

    @Delete
    void deleteSessionRounds(SessionRound... sessionRounds);

    @Delete
    void deleteSessions(Session... sessions);

    @Delete
    void deleteReactions(Reaction... reactions);
}
