package de.tforneberg.aatapp.ui.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.data.SessionToCSVConverter;
import de.tforneberg.aatapp.logic.viewModels.SessionListViewModel;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.ui.adapters.TextItemListAdapter;
import de.tforneberg.aatapp.ui.adapters.TextItemListAdapterInterface;

public class SessionListActivity extends AppCompatActivity implements TextItemListAdapterInterface, SessionToCSVConverter.FileReadyListener {

    private TextItemListAdapter recyclerViewAdapter;

    private SessionListViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_list);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Setup recycler view
        RecyclerView recyclerView = findViewById(R.id.session_list_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> customActions = Collections.singletonList("Export");
        recyclerViewAdapter = new TextItemListAdapter(this, this,
                customActions, customActions,
                TextItemListAdapter.Action.Delete
        );

        recyclerView.setAdapter(recyclerViewAdapter);
        SimpleItemAnimator animator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        Objects.requireNonNull(animator).setSupportsChangeAnimations(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));

        // Setup data subscription (adapter->viewModel->Repository->Database)
        viewModel = ViewModelProviders.of(this).get(SessionListViewModel.class);

        viewModel.getIsReadyLiveData().observe(this, isReady -> {
            if (isReady!= null && isReady) {
                viewModel.getSessions().observe(this, sessions -> {
                    if (sessions != null) {
                        recyclerViewAdapter.setListObjects(sessions);
                    }
                });
            }
        });
    }

    @Override
    public void onListItemSelected(Object item) {
        Intent showSessionResults = new Intent(SessionListActivity.this, SessionResultsActivity.class);
        showSessionResults.putExtra("sessionID", ((Session)item).getId());
        startActivity(showSessionResults);
    }

    @Override
    public void handleListItemAction(Object item, TextItemListAdapter.Action action, String customActionName) {
        if (action.equals(TextItemListAdapter.Action.Delete)) {
            viewModel.deleteSession((Session) item);
        } else if (action.equals(TextItemListAdapter.Action.Custom)) {
            if (customActionName.equals("Export")) {
                Toast.makeText(this, "Exporting...", Toast.LENGTH_LONG).show();
                if (item instanceof List) {
                    ArrayList<Session> sessionList = new ArrayList<>();
                    for (Object o : (List) item) {
                        if (o instanceof Session) {
                            sessionList.add((Session)o);
                        }
                    }
                    new SessionToCSVConverter(this, this, sessionList).execute();
                } else {
                    new SessionToCSVConverter(this, this, (Session) item, true).execute();
                }
            }
        }
    }

    @Override
    public void onCSVFileReady(File file) {
        Uri uri = FileProvider.getUriForFile(this, "de.tforneberg.aatapp.fileprovider", file);

        Intent shareIntent = ShareCompat.IntentBuilder.from(this).setStream(uri).getIntent();
        shareIntent.setData(uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(shareIntent);
        }
    }

    @Override
    public String getListItemTitle(Object item) {
        Session session = (Session) item;

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yyyy HH:mm:ss");
        return "Session " + session.getId() + " - " + sdf.format(session.getDate());
    }

    @Override
    public String getListItemDescription(Object item) {
        Session session = (Session) item;
        return "User-ID: "+ session.getUserId();
    }
}
