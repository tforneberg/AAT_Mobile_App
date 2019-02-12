package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.List;

import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.User;
import de.tforneberg.aatapp.repo.Repository;

public class SessionListViewModel extends AndroidViewModel {
    private Repository repository;
    private LiveData<List<Session>> sessions;
    private LiveData<Boolean> isReadyLiveData = new MutableLiveData<>();

    private static class LoadDataAsyncTask extends AsyncTask<Long, Void, Void> {
        private SessionListViewModel viewModel;

        LoadDataAsyncTask(SessionListViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        protected Void doInBackground(Long... id) {
            User user = viewModel.repository.USERS.getUserByIdSync(id[0]);
            if (user.isAdmin()) {
                viewModel.sessions = viewModel.repository.SESSIONS.getAllSessions();
            } else {
                viewModel.sessions = viewModel.repository.SESSIONS.getSessionsByUserId(user.getId());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((MutableLiveData<Boolean>)viewModel.isReadyLiveData).setValue(true);
        }
    }

    public SessionListViewModel(@NonNull Application application) {
        super(application);

        repository = Repository.getInstance(application);

        long userId = PreferenceManager.getDefaultSharedPreferences(application).getLong("user", -1);
        new LoadDataAsyncTask(this).execute(userId);
    }

    public LiveData<List<Session>> getSessions() {
        return sessions;
    }

    public LiveData<Boolean> getIsReadyLiveData() {
        return isReadyLiveData;
    }

    public void deleteSession(Session session) {
        repository.SESSIONS.deleteSession(session);
    }
}
