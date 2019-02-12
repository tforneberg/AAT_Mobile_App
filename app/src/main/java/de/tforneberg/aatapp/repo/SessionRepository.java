package de.tforneberg.aatapp.repo;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import de.tforneberg.aatapp.data.AatDatabase;
import de.tforneberg.aatapp.data.dao.SessionDao;
import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.SessionRound;

public class SessionRepository {

    private SessionDao sessionDao;

    SessionRepository(AatDatabase database) {
        sessionDao = database.sessionDao();
    }

    //GET Methods
    public LiveData<SessionRound> getSessionRoundById(long id) { return sessionDao.getSessionRoundById(id); }

    public LiveData<List<SessionRound>> getSessionRoundsBySessionId(long sessionId) { return sessionDao.getSessionRoundsBySessionId(sessionId); }

    public List<SessionRound> getSessionRoundsBySessionIdSync(long sessionId) { return sessionDao.getSessionRoundsBySessionIdSync(sessionId); }

    public LiveData<List<SessionRound>> getAllSessionRounds() { return sessionDao.getAllSessionRounds(); }

    public LiveData<Session> getSessionById(long id) { return sessionDao.getSessionById(id); }

    public LiveData<List<Session>> getSessionsByUserId(long userId) { return sessionDao.getSessionsByUserId(userId); }

    public List<Session> getSessionsByUserIdSync(long userId) { return sessionDao.getSessionsByUserIdSync(userId); }

    public Session getSessionByIdSync(long id) { return sessionDao.getSessionByIdSync(id); }

    public LiveData<List<Session>> getAllSessions() { return sessionDao.getAllSessions(); }

    public List<Session> getAllSessionsSync() { return sessionDao.getAllSessionsSync(); }

    public LiveData<Reaction> getReactionById(long id) { return sessionDao.getReactionById(id); }

    public LiveData<List<Reaction>> getReactionsBySessionRoundId(long id) { return sessionDao.getReactionsBySessionRoundId(id); }

    public List<Reaction> getReactionsBySessionRoundIdSync(long id) { return sessionDao.getReactionsBySessionRoundIdSync(id); }

    public LiveData<List<Reaction>> getAllReactions() { return sessionDao.getAllReactions(); }

    //Insert Methods
    public void insertSessionRound(SessionRound sessionRound) {
        new InsertSessionRoundAsyncTask(sessionDao).execute(sessionRound);
    }

    public long insertSessionRoundSync(SessionRound sessionRound) {
        return sessionDao.insertSessionRound(sessionRound);
    }

    public void insertReaction(Reaction... reaction) {
        new InsertReactionTask(sessionDao).execute(reaction);
    }

    public void insertSession(Session session) {
        new InsertSessionAsyncTask(sessionDao).execute(session);
    }

    public long insertSesionSync(Session session) {
        return sessionDao.insertSession(session);
    }

    //Delete Methods
    public void deleteSessionRound(SessionRound sessionRound) {
        new DeleteSessionRoundAsyncTask(sessionDao).execute(sessionRound);
    }

    public void deleteSession(Session session) {
        new DeleteSessionAsyncTask(sessionDao).execute(session);
    }

    //Async Tasks
    static class InsertSessionRoundAsyncTask extends AsyncTask<SessionRound, Void, Long> {
        private SessionDao dao;
        InsertSessionRoundAsyncTask(SessionDao dao) { this.dao = dao; }

        @Override
        protected Long doInBackground(final SessionRound... params) {
            return dao.insertSessionRound(params[0]);
        }
    }

    static class InsertSessionAsyncTask extends AsyncTask<Session, Void, Long> {
        private SessionDao dao;
        InsertSessionAsyncTask(SessionDao dao) { this.dao = dao; }

        @Override
        protected Long doInBackground(final Session... params) {
            return dao.insertSession(params[0]);
        }
    }

    static class InsertReactionTask extends AsyncTask<Reaction, Void, Long[]> {
        private SessionDao dao;
        InsertReactionTask(SessionDao dao) { this.dao = dao; }

        @Override
        protected Long[] doInBackground(final Reaction... params) {
            return dao.insertReactions(params);
        }
    }

    static class DeleteSessionRoundAsyncTask extends AsyncTask<SessionRound, Void, Void> {
        private SessionDao dao;
        DeleteSessionRoundAsyncTask(SessionDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(SessionRound... sessionRounds) {
            dao.deleteSessionRounds(sessionRounds);
            return null;
        }
    }

    static class DeleteSessionAsyncTask extends AsyncTask<Session, Void, Void> {
        private SessionDao dao;
        DeleteSessionAsyncTask(SessionDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(Session... sessions) {
            dao.deleteSessions(sessions);
            return null;
        }
    }
}
