package de.tforneberg.aatapp.ui.activities;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.SessionResultsViewModel;
import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Settings;

public class SessionResultsActivity extends AppCompatActivity {

    private SessionResultsViewModel viewModel;
    private ViewPager resultsViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_results);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = ViewModelProviders.of(this).get(SessionResultsViewModel.class);
        //Get the session ID from the intent and and initialize the ViewModel data with it
        Bundle extras = getIntent().getExtras();
        if (extras != null) { // Try to load id from intent
            viewModel.initializeWithSessionId(extras.getLong("sessionID"));
        }
        if (viewModel.getSessionId() == 0L) finish(); //No session ID found? Finish the activity.

        resultsViewPager = findViewById(R.id.resultsViewPager);

        //when the ViewModel is settingsDownloadDone loading everything
        viewModel.getIsReadyLiveData().observe(this, isReady -> {
            if (isReady != null && isReady) {
                //setup the viewPager with a RoundResultsFragmentAdapter
                setTitle("Session "+viewModel.getSession().getId());
                int amountOfFragments = viewModel.getSessionRounds().size() + 1;
                resultsViewPager.setAdapter(new RoundResultsFragmentAdapter(getSupportFragmentManager(), amountOfFragments));
                resultsViewPager.setCurrentItem(viewModel.getCurrentTab());
                resultsViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int i) {
                        viewModel.setCurrentTab(i);
                    }
                });
            }
        });
    }

    public class RoundResultsFragmentAdapter extends FragmentPagerAdapter {
        private int numberOfViews;

        RoundResultsFragmentAdapter(FragmentManager fm, int numberOfViews) {
            super(fm);
            this.numberOfViews = numberOfViews;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                //General/overall Fragment
                return new OverallResultsFragment();
            } else {
                //Round results page
                Fragment fragment = new RoundResultsFragment();
                Bundle args = new Bundle();
                args.putInt(RoundResultsFragment.ROUND_NUMBER, i);
                fragment.setArguments(args);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return numberOfViews;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return getString(R.string.overall);
            } else {
                return getString(R.string.round) + " " + (position);
            }
        }
    }

    public static class OverallResultsFragment extends Fragment {
        private TextView textView1;
        private TextView textView2;
        private TextView textView3;
        private TextView textView4;
        private TextView textView5;
        private TextView textView6;
        private TextView textView7;
        private TextView textView8;
        private TextView textView9;
        private TextView textView10;
        private TextView textView11;
        private TextView textView12;
        SessionResultsViewModel viewModel;

        @SuppressLint("SetTextI18n")
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_overall_results, container, false);

            textView1 = v.findViewById(R.id.textView1);
            textView2 = v.findViewById(R.id.textView2);
            textView3 = v.findViewById(R.id.textView3);
            textView4 = v.findViewById(R.id.textView4);
            textView5 = v.findViewById(R.id.textView5);
            textView6 = v.findViewById(R.id.textView6);
            textView7 = v.findViewById(R.id.textView7);
            textView8 = v.findViewById(R.id.textView8);
            textView9 = v.findViewById(R.id.textView9);
            textView10 = v.findViewById(R.id.textView10);
            textView11 = v.findViewById(R.id.textView11);
            textView12 = v.findViewById(R.id.textView12);

            FragmentActivity activity = Objects.requireNonNull(getActivity());
            viewModel = ViewModelProviders.of(activity).get(SessionResultsViewModel.class);

            Context context = Objects.requireNonNull(getContext());

            String sessionId = String.valueOf(viewModel.getSession().getId());
            textView1.setText(Html.fromHtml("<b>Session ID: </b>"+sessionId));

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat sdf = new SimpleDateFormat("dd. MMM yyyy HH:mm:ss");
            String sessionDate =  sdf.format(viewModel.getSession().getDate());
            textView2.setText(Html.fromHtml("<b>"+getText(R.string.date)+": </b>"+sessionDate));

            String imageSetName = viewModel.getImageSet().getName();
            textView3.setText(Html.fromHtml("<b>Image Set: </b>"+imageSetName));

            String userName = viewModel.getUser().getName();
            String userID = String.valueOf(viewModel.getUser().getId());
            textView4.setText(Html.fromHtml("<b>"+getString(R.string.user)+": </b>"+userName + " (ID: "+userID+")"));

            String amountRounds = String.valueOf(viewModel.getSessionRounds().size());
            textView5.setText(Html.fromHtml("<b>"+getString(R.string.pref_amountOfRounds)+": </b>"+amountRounds));

            String imagesPerRound = String.valueOf(viewModel.getReactions().get(0).size());
            textView6.setText(Html.fromHtml("<b>"+getString(R.string.pref_imagesPerRound)+": </b>"+imagesPerRound));

            String gestureMode = String.valueOf(viewModel.getSession().getGestureMode());
            switch (gestureMode) {
                case "pinch":
                    gestureMode = getString(R.string.gestureMode_pinch); break;
                case "angle":
                    gestureMode = getString(R.string.gestureMode_angle); break;
                case "move":
                    gestureMode = getString(R.string.gestureMode_move); break;
            }
            textView7.setText(Html.fromHtml("<b>"+getString(R.string.gestureMode)+": </b>"+gestureMode));

            String orientation = viewModel.getSession().isLandscape() ? "Landscape" : "Portrait";
            textView8.setText(Html.fromHtml("<b>"+getString(R.string.pref_screenOrientation)+": </b>"+orientation));

            String color;
            if (viewModel.getSession().isHasColoredBorder()) {
                String colorPush = Settings.getColorNameFromFex(context, viewModel.getSession().getBorderColorPush());
                String colorPull = Settings.getColorNameFromFex(context, viewModel.getSession().getBorderColorPull());
                color = getString(R.string.yes) + ", Push: "+colorPush+", Pull: "+colorPull;
            } else {
                color = getString(R.string.no);
            }
            textView9.setText(Html.fromHtml("<b>"+getString(R.string.colored_borders)+": </b>"+color));

            String angle;
            if (viewModel.getSession().isHasRotationAngle()) {
                String anglePush = String.valueOf(viewModel.getSession().getRotationAnglePush());
                String anglePull = String.valueOf(viewModel.getSession().getRotationAnglePull());
                angle = getString(R.string.yes) + ", Push: "+anglePush+"°, Pull: "+anglePull+"°";
            } else {
                angle = getString(R.string.no);
            }
            textView10.setText(Html.fromHtml("<b>"+getString(R.string.rotation)+": </b>"+angle));

            String timeBetweenImages = String.valueOf(viewModel.getSession().getTimeBetweenImages()/1000)+"s";
            textView11.setText(Html.fromHtml("<b>"+getString(R.string.pref_timeBetweenImages)+": </b>"+timeBetweenImages));

            long bias = SessionResultsViewModel.calculateApproachAvoidanceBias(viewModel.getReactions());
            textView12.setText(Html.fromHtml("<b>Bias: </b>"+bias));

            return v;
        }

    }

    public static class RoundResultsFragment extends Fragment {
        public static final String ROUND_NUMBER = "roundNumber";
        private SessionResultsViewModel viewModel;

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_round_results, container, false);

            FragmentActivity activity = Objects.requireNonNull(getActivity());
            viewModel = ViewModelProviders.of(activity).get(SessionResultsViewModel.class);

            int roundNumber = Objects.requireNonNull(getArguments()).getInt(ROUND_NUMBER);
            List<Reaction> reactions = viewModel.getReactions().get(roundNumber-1);

            TableLayout table = v.findViewById(R.id.reactionTable);
            table.setColumnShrinkable(0, true);
            Context context = Objects.requireNonNull(getContext());

            TableRow.LayoutParams p1 = new android.widget.TableRow.LayoutParams();
            int marginP1 = dpToPixel(8, context);
            p1.setMargins(marginP1, marginP1, marginP1, marginP1);
            TableRow.LayoutParams p2 = new TableRow.LayoutParams(dpToPixel(1, context), ViewGroup.LayoutParams.MATCH_PARENT);
            TableRow.LayoutParams p3 = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPixel(1, context));
            int colorAccent = getResources().getColor(R.color.colorAccent);
            for(Reaction r : reactions) {
                TableRow row = new TableRow(context);
                row.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

                TextView imageName = new TextView(context);
                imageName.setLayoutParams(p1);
                imageName.setText(r.getImageName());
                row.addView(imageName);

                View line1 = new View(context);
                line1.setBackgroundColor(colorAccent);
                line1.setLayoutParams(p2);
                row.addView(line1);

                TextView time1 = new TextView(context);
                time1.setLayoutParams(p1);
                time1.setText(String.valueOf(r.getFirstReactionTime()));
                row.addView(time1);

                View line2 = new View(context);
                line2.setBackgroundColor(colorAccent);
                line2.setLayoutParams(p2);
                row.addView(line2);

                TextView time2 = new TextView(context);
                time2.setLayoutParams(p1);
                time2.setText(String.valueOf(r.getFinalReactionTime()));
                row.addView(time2);

                View line3 = new View(context);
                line3.setBackgroundColor(colorAccent);
                line3.setLayoutParams(p2);
                row.addView(line3);

                TextView pushOrPull = new TextView(context);
                pushOrPull.setLayoutParams(p1);
                pushOrPull.setText(r.isActionPush()? "Push" : "Pull");
                row.addView(pushOrPull);

                table.addView(row);

                if(reactions.indexOf(r) != reactions.size()-1) {
                    TableRow row2 = new TableRow(context);

                    View hLine1 = new View(context);
                    hLine1.setBackgroundColor(colorAccent);
                    hLine1.setLayoutParams(p3);
                    row2.addView(hLine1);

                    View vLline1 = new View(context);
                    vLline1.setBackgroundColor(colorAccent);
                    vLline1.setLayoutParams(p2);
                    row2.addView(vLline1);

                    View hLine2 = new View(context);
                    hLine2.setLayoutParams(p3);
                    hLine2.setBackgroundColor(colorAccent);
                    row2.addView(hLine2);

                    View vLline2 = new View(context);
                    vLline2.setBackgroundColor(colorAccent);
                    vLline2.setLayoutParams(p2);
                    row2.addView(vLline2);

                    View hLine3 = new View(context);
                    hLine3.setBackgroundColor(colorAccent);
                    hLine3.setLayoutParams(p3);
                    row2.addView(hLine3);

                    View vLline3 = new View(context);
                    vLline3.setBackgroundColor(colorAccent);
                    vLline3.setLayoutParams(p2);
                    row2.addView(vLline3);

                    View hLine4 = new View(context);
                    hLine4.setBackgroundColor(colorAccent);
                    hLine4.setLayoutParams(p3);
                    row2.addView(hLine4);

                    table.addView(row2);
                }
            }

            return v;
        }

        public static int dpToPixel(int dp, Context context) {
            float scale = context.getResources().getDisplayMetrics().density;
            return (int) ((float) dp * scale);
        }
    }

}
