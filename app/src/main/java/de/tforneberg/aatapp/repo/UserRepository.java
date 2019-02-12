package de.tforneberg.aatapp.repo;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

import de.tforneberg.aatapp.data.AatDatabase;
import de.tforneberg.aatapp.data.dao.UserDao;
import de.tforneberg.aatapp.model.User;

public class UserRepository {

    private UserDao userDao;

    UserRepository (AatDatabase database) {
        userDao = database.userDao();
    }

    //GET Methods
    public LiveData<User> getUserById(long id) { return userDao.getUserById(id); }

    public User getUserByIdSync(long id) { return userDao.getUserByIdSync(id); }

    public LiveData<User> getUserByName(String name) { return userDao.getUserByName(name); }

    public User getUserByNameSync(String name) { return userDao.getUserByNameSync(name); }

    public LiveData<List<User>> getAllUsers() { return userDao.getAllUsers(); }

    //INSERT Methods
    public void insertUser(User user) { new ImageRepository.InsertUserAsyncTask(userDao).execute(user); }

    public long insertUserSync(User user) { return userDao.insertUser(user); }

    //UPDATE Methods
    public void updateUser(User... users) {
        new UpdateUserAsyncTask(userDao).execute(users);
    }

    //DELETE methods
    public void deleteUser(User user) {
        new DeleteUserAsyncTask(userDao).execute(user);
    }

    //async update tasks
    static class UpdateUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao dao;
        UpdateUserAsyncTask(UserDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(final User... params) {
            dao.updateUsers(params[0]);
            return null;
        }
    }

    //async delete tasks
    static class DeleteUserAsyncTask extends AsyncTask<User, Void, Void> {
        private UserDao dao;
        DeleteUserAsyncTask(UserDao dao) { this.dao = dao; }

        @Override
        protected Void doInBackground(final User... params) {
            dao.deleteUsers(params[0]);
            return null;
        }
    }
}
