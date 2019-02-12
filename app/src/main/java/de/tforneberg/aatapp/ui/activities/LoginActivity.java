package de.tforneberg.aatapp.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameText;
    private EditText passwordText;
    private View progressView;
    private View loginFormView;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setSupportActionBar(findViewById(R.id.toolbar));

        //init ViewModel
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        // Set up the login form.
        userNameText = findViewById(R.id.userName);

        passwordText = findViewById(R.id.password);

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(view -> attemptLoginOrRegister(true));

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(view -> attemptLoginOrRegister(false));

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login/register attempt is made.
     */
    public void attemptLoginOrRegister(boolean login) {
        if (viewModel.getAuthTask() != null) {
            return;
        }

        // Reset errors.
        userNameText.setError(null);
        passwordText.setError(null);

        // Store values at the time of the login attempt.
        String user = userNameText.getText().toString();
        String password = passwordText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !viewModel.isPasswordValid(password)) {
            passwordText.setError(getString(R.string.error_invalid_password));
            focusView = passwordText;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(user)) {
            userNameText.setError(getString(R.string.error_field_required));
            focusView = userNameText;
            cancel = true;
        } else if (!viewModel.isUsernameValid(user)) {
            userNameText.setError(getString(R.string.error_invalid_user_name));
            focusView = userNameText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner and start a background task to perform the user login/register.
            showProgress(true);
            viewModel.startUserLoginOrRegisterTask(this, user, password, login);
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void onPostExecute(Boolean success, boolean userExistsAlready) {
        viewModel.setAuthTask(null);
        showProgress(false);

        if (success) {
            startMainActivity();
        } else {
            if (userExistsAlready) {
                userNameText.setError(getString(R.string.error_user_exists_already));
            } else {
                passwordText.setError(getString(R.string.error_incorrect_password));
                passwordText.requestFocus();
            }
        }
    }

    public void onCancelled() {
        viewModel.setAuthTask(null);
        showProgress(false);
    }

    private void startMainActivity() {
        finish();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

}

