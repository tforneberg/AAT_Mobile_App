package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import de.tforneberg.aatapp.model.User;
import de.tforneberg.aatapp.repo.Repository;
import de.tforneberg.aatapp.ui.activities.LoginActivity;

public class LoginViewModel extends AndroidViewModel {
    private Repository repository;
    private SharedPreferences sharedPrefs;
    private LoginOrRegisterTask authTask = null;

    public LoginViewModel (Application application) {
        super(application);
        repository = Repository.getInstance(application);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public LoginOrRegisterTask getAuthTask() {
        return authTask;
    }

    public boolean getLoggedIn() {
        long id = sharedPrefs.getLong("user", -1);
        return id != -1;
    }

    public void setAuthTask(LoginOrRegisterTask authTask) {
        this.authTask = authTask;
    }

    public void startUserLoginOrRegisterTask(LoginActivity activity, String name, String password, boolean login) {
        authTask = new LoginOrRegisterTask(activity, this, name, password, login);
        authTask.execute((Void) null);
    }

    public static class LoginOrRegisterTask extends AsyncTask<Void, Void, Boolean> {
        private final String userName;
        private final String password;
        private final boolean login;
        private final WeakReference<LoginActivity> activity;
        private final LoginViewModel viewModel;

        //boolean field used for customizing the error message to the user
        private boolean userExistsAlready = false;

        LoginOrRegisterTask(LoginActivity activity, LoginViewModel viewModel, String userName, String password, boolean login) {
            this.activity = new WeakReference<>(activity);
            this.viewModel = viewModel;
            this.userName = userName;
            this.password = password;
            this.login = login;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //get user with given username
            User user = viewModel.repository.USERS.getUserByNameSync(userName);

            byte[] hashedPw;
            byte[] salt;

            if (login && user != null) { //if action is login and user exists
                // get the users salt
                salt = user.getSalt();

                try {
                    //hash the entered password with the users stored salt
                    PBEKeySpec keySpecs = new PBEKeySpec(password.toCharArray(), salt, 1000, 64*8);
                    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                    hashedPw = skf.generateSecret(keySpecs).getEncoded();
                } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                    return false;
                }

                //get the users hashed pw that was stored in the DB
                byte[] pwFromDB = user.getPassword();

                //compare the two hash values to check if the entered password is valid
                int diff = hashedPw.length ^ pwFromDB.length;
                for (int i = 0; i < hashedPw.length && i < pwFromDB.length; i++) {
                    diff |= hashedPw[i] ^ pwFromDB[i];
                }

                //Login user if password is valid
                if (diff == 0) {
                    //login the user by setting a login "token" (should be improved in regards of security)
                    //IMPORTANT NOTE: this is not a secure login token and has to be improved!
                    return viewModel.sharedPrefs.edit().putLong("user", user.getId()).commit();
                }
            } else if(!login) { //if action is register and the user does not exist, yet
                if (user == null) {
                    try {
                        //fill the byte[] salt with the salt
                        salt = new byte[16];
                        SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);

                        //hash the password with the newly created salt
                        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                        PBEKeySpec keySpecs = new PBEKeySpec(password.toCharArray(), salt, 1000, 64*8);
                        hashedPw = skf.generateSecret(keySpecs).getEncoded();
                    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                        return false;
                    }

                    //Insert new user with name, newly created salt and hashed pw in database
                    User newUser = new User(userName, hashedPw, salt, false);
                    long id = viewModel.repository.USERS.insertUserSync(newUser);

                    //login the newly created user by setting a login "token"
                    //IMPORTANT NOTE: this is not a secure login token and has to be improved!
                    return viewModel.sharedPrefs.edit().putLong("user", id).commit();
                } else {
                    userExistsAlready = true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            activity.get().onPostExecute(success, userExistsAlready);
        }

        @Override
        protected void onCancelled() {
            activity.get().onCancelled();
        }
    }

    public boolean isUsernameValid(String userName) {
        return userName.length() > 3;
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 4;
    }

}
