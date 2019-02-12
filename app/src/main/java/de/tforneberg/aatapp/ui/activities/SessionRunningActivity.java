package de.tforneberg.aatapp.ui.activities;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.Transformation;
import com.ortiz.touchview.TouchImageView;

import java.util.ArrayList;
import java.util.List;

import de.tforneberg.aatapp.GlideApp;
import de.tforneberg.aatapp.GlideRequest;
import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.gestureProviders.GestureListener;
import de.tforneberg.aatapp.logic.transformations.ColoredBorder;
import de.tforneberg.aatapp.logic.transformations.Rotation;
import de.tforneberg.aatapp.logic.viewModels.SessionRunningViewModel;
import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.Settings;
import de.tforneberg.aatapp.ui.util.FadeOutAnimation;
import de.tforneberg.aatapp.ui.util.UpdateProgressBarTask;

public class SessionRunningActivity extends AppCompatActivity implements FadeOutAnimation.OnFadeOutFinishedListener, GestureListener {

    private final static int DISPLAY_INSTRUCTIONS_RESULT_CODE = 1;
    private final static float startingZoom = 0.6f;
    private SessionRunningViewModel viewModel;

    private TouchImageView imageView;
    private TextView textView1;
    private TextView textView2;
    private ProgressBar progressBar;
    private Toast resultMessageToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init ViewModel
        viewModel = ViewModelProviders.of(this).get(SessionRunningViewModel.class);
        viewModel.getIsReadyLiveData().observe(this, ready -> {
            if (ready != null && ready) {
                init();
                if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    startSession();
                } else {
                    viewModel.setSessionCanStart(true);
                }
            } else {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel.getSessionIsRunning()) {
            //session is running already, resume running session
            viewModel.assignNewStartingTime();
        } else if (viewModel.getSessionCanStart()) {
            //session not started yet, start initial session
            startSession();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.stop();
    }

    private void init() {
        //viewModel initialization that can only happen this late
        viewModel.setGestureListener(this);
        viewModel.initGestureProvider();

        //Set screen orientation (must happen before setContentView!)
        setRequestedOrientation(viewModel.getSettings().screenOrientationFlag);

        //Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_session_running);

        //init view objects
        textView1 = findViewById(R.id.aboutText);
        textView2 = findViewById(R.id.textView2);
        imageView = findViewById(R.id.imageView);
        imageView.setMinZoom(0.001f);
        imageView.setMaxZoom(3.5f);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax((int) viewModel.getSettings().timeBetweenImages);
    }

    private void startSession() {
        if (viewModel.getGestureProvider() == null) {
            finish(); //no valid gesture provider -> end this activity
        } else {
            if (viewModel.getSettings().showInstructions) {
                showNextRoundInstructions();
            } else {
                viewModel.setSessionIsRunning(true);
                startNewImage();
            }
        }
    }

    public void startNewRound() {
        viewModel.startNewRound();
        viewModel.setSessionIsRunning(true);
        startNewImage();
    }

    public void startNewImage() {
        long time1 = System.currentTimeMillis();

        //hide the imageViewPanel
        imageView.setVisibility(View.INVISIBLE);

        //show the progress bar
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        //set the image data for the next round
        viewModel.setNewCurrentImage();
        final Image currentImage = viewModel.getCurrentImage();

        //prevent zooming in the wrong direction
        if (viewModel.getCurrentImage().getPush()) {
            imageView.setMinZoom(0.001f);
            imageView.setMaxZoom(startingZoom);
        } else {
            imageView.setMaxZoom(3.5f);
            imageView.setMinZoom(startingZoom);
        }

        //get image representation
        Object imageToLoad;
        if (viewModel.getImageSet().getId() == 1) {// default image set
            imageToLoad = viewModel.getIdFromDefaultImage(this, currentImage); //resource identifier (int)
        } else {
            imageToLoad = viewModel.getFileFromImage(this, currentImage);
        }

        //get the image bitmap as GlideRequest
        GlideRequest<Drawable> glideReq = GlideApp.with(this).load(imageToLoad);
        List<Transformation<Bitmap>> transformations = new ArrayList<>();

        //define colored border rotation if activated
        if (viewModel.getSettings().hasColoredBorder) {
            int borderColor = viewModel.getBorderColorForCurrentImage();
            ColoredBorder coloredBorder = new ColoredBorder(borderColor, 15);
            transformations.add(coloredBorder);
        }

        //define rotation transformation if activated
        if(viewModel.getSettings().hasRotationAngle) {
            int rotationAngle = viewModel.getRotationAngleForCurrentImage();
            Rotation rotation = new Rotation(rotationAngle);
            transformations.add(rotation);
        }

        //apply transformations
        if (transformations.size() != 0) {
            MultiTransformation<Bitmap> multiTransformation = new MultiTransformation<>(transformations);
            glideReq = glideReq.transform(multiTransformation);
        }

        //draw transformed image into imageView
        glideReq.into(imageView);

        //update progress bar
        long time2 = System.currentTimeMillis();
        long diff = time2 - time1;
        progressBar.incrementProgressBy((int)diff);

        //further update the progress bar
        new UpdateProgressBarTask(progressBar).execute();

        //after the pause duration:
        new android.os.Handler().postDelayed(() -> {
            //reset and hide progress bar
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
            //show image view
            imageView.setZoom(startingZoom);
            imageView.setAlpha(1f);
            imageView.setVisibility(View.VISIBLE);

            //finally, tell the viewModel to start the gestureProvider and assign the new starting time
            viewModel.assignNewStartingTime();

        }, viewModel.getSettings().timeBetweenImages - (diff));

        //remove the resultMessageToast before the next image begins
        new android.os.Handler().postDelayed(() -> {
            if (resultMessageToast != null) resultMessageToast.cancel();
        }, viewModel.getSettings().timeBetweenImages - (diff) - 300);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        viewModel.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.stop();
    }

    public void resetZoom() {
        getImageView().setZoom(startingZoom);
    }

    public float getStartingZoom() {
        return startingZoom;
    }

    public float getCurrentZoom() {
        return imageView.getCurrentZoom();
    }

    public Context getContext() {
        return this;
    }

    public boolean userReacted(boolean push, float value, long finalReactionTimestamp, Long... firstReactionTimestamp) {
        boolean correctReaction = viewModel.userReacted(push, value, finalReactionTimestamp, firstReactionTimestamp);

        if (correctReaction) {
            new FadeOutAnimation(push, imageView, 500, this);

            if (viewModel.getSettings().resultMessageType == Settings.ResultMessageType.Toast) {
                createReactionToast();
            }
        }

        return correctReaction;
    }

    public void userStartedReacting() {
        viewModel.saveGestureStartedTime();
    }

    public void userAbortedReacting() {
        viewModel.resetGestureStartedTime();
    }

    public void onFadeOutFinished() {
        if (viewModel.getSettings().resultMessageType == Settings.ResultMessageType.Dialog) {
            createReactionDialog();
        } else {
            startNewImageOrRoundOrFinish();
        }
    }

    private void createReactionToast() {
        resultMessageToast = Toast.makeText(this, viewModel.getResultMessage(), Toast.LENGTH_SHORT);
        resultMessageToast.show();
    }

    private void createReactionDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SessionRunningActivity.this).create();
        alertDialog.setTitle(viewModel.getResultMessage());
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialogInterface, i) -> {});
        alertDialog.setOnDismissListener(dialogInterface -> startNewImageOrRoundOrFinish());
        alertDialog.show();
    }

    private void startNewImageOrRoundOrFinish() {
        if (viewModel.getCurrentImageNumber() < viewModel.getSettings().imagesPerRound) {
            startNewImage();
        } else {
            viewModel.increaseCurrentRoundNumber();
            if (viewModel.getCurrentRoundNumber() < viewModel.getSettings().rounds) {
                viewModel.setSessionIsRunning(false);
                showNextRoundInstructions();
            } else {
                viewModel.saveToDb();
                finish();
            }
        }
    }

    private void showNextRoundInstructions() {
        Intent intent = new Intent(this, InstructionActivity.class);
        intent.putExtra("roundNumber", viewModel.getCurrentRoundNumber());
        startActivityForResult(intent, DISPLAY_INSTRUCTIONS_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DISPLAY_INSTRUCTIONS_RESULT_CODE) {
            if (viewModel.getCurrentRoundNumber() == 0) {
                viewModel.setSessionIsRunning(true);
                startNewImage();
            } else {
                startNewRound();
            }
        }
    }

    public void updateTextView1(String newVal) {
        textView1.setText(newVal);
    }

    public void updateTextView2(String newVal) {
        textView2.setText(newVal);
    }

    @Override
    public void onBackPressed() {
        AlertDialog alertDialog = new AlertDialog.Builder(SessionRunningActivity.this).create();
        alertDialog.setTitle(getString(R.string.dialog_are_you_sure));
        alertDialog.setMessage(getString(R.string.dialog_do_you_really_want_to_leave));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), (dialog, which) -> {
                    dialog.dismiss();
                    SessionRunningActivity.super.onBackPressed();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    /**
     * This method dispatches all touch events on the whole Activity to the ViewModels' PinchZoomGestureProvider.
     * The PinchZoomGestureProvider then evaluates the touch gesture and decides whether to consume
     * it (=returns true) or not (=returns false). It consumes "wrong" gestures such as double tap
     * to zoom or scaling in the wrong direction (push direction on pull images and vice versa).
     * The PinchZoomGestureProvider also finds scaling (=PinchZoom) gestures in the event data
     * and evaluates if the user successfully reacted to an image or not.
     *<br\><br\>
     * If no PinchZoomGestureProvider is instantiated (meaning another gesture mode is active),
     * all touch events are consumed and not forwarded to the TouchImageView.
     * <br\><br\>
     * Only if a PinchZoomGestureProvider is instantiated and the touch event belongs to a valid
     * scaling gesture, it gets forwarded to the TouchImageView to do the actual zooming functionality.
     * <br\><br\>
     * By taking all events on the Activity and mapping the appropriate ones to the TouchImageView,
     * all touch handling/dispatching is settingsDownloadDone here, manually. Therefore, this method always returns true to
     * consume the touch event and prevent any views from falsely react to them.
     *
     * @param ev the MotionEvent
     * @return always true to consume all events, since dispatching is manually settingsDownloadDone here
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean consumed = viewModel.dispatchTouchEvent(ev);
        if (!consumed) {
            imageView.dispatchTouchEvent(ev);
        }
        return true;
    }

    public void updateTextView(String newText) {
        textView1.setText(newText);
    }

    public TouchImageView getImageView() {
        return imageView;
    }
}
