package de.tforneberg.aatapp.logic.gestureProviders;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import de.tforneberg.aatapp.logic.viewModels.SessionRunningViewModel;

/**
 * The PinchZoomGestureProvider handles the recognition of move gestures for the gesture mode "Pinch-Zoom".
 * After initialization, it needs to get started by calling its start()-method.
 * Also, it needs to be stopped again when it is not needed, anymore. This is done by calling stop().
 * For it to work, an activity or its viewModel needs to forward all MotionEvents to handleTouchEvent(MotionEvent ev).
 * <br/><br/>
 * If it detects a gesture, it calls viewModel.getGestureListener().userReacted(...).
 */
public class PinchZoomGestureProvider
        extends ScaleGestureDetector.SimpleOnScaleGestureListener implements GestureProvider {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector doubleTapDetector;
    private boolean started;
    private float upperTriggerLimit;
    private float lowerTriggerLimit;

    private GestureListener listener;
    private SessionRunningViewModel viewModel;

    private float startingZoom;

    public PinchZoomGestureProvider(SessionRunningViewModel viewModel) {
        this.listener = viewModel.getGestureListener();
        this.viewModel = viewModel;

        //calculate the trigger points at which a gesture gets registered as such
        startingZoom = listener.getStartingZoom();
        upperTriggerLimit = startingZoom * 1.25f;
        lowerTriggerLimit = startingZoom / 1.25f;

        //init a ScaleGestureDetector and register as its listener
        scaleGestureDetector = new ScaleGestureDetector(listener.getContext(), this);

        //this removes the double tap to zoom functionality from the TouchImageView
        doubleTapDetector = new GestureDetector(listener.getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                public boolean onDoubleTap(MotionEvent e) { return true; }
            }
        );
    }

    @Override
    public void start(long time) {
        this.started = true;
    }

    @Override
    public void stop() {
        this.started = false;
    }

    /**
     * This method filters out (by returning true) all the events on which the imageView
     * should not react. These are for example double tap gestures.
     * Also, it filters out reaction in the wrong direction (push reaction on pull images and vice versa).
     * Furthermore, it finds scaling (=PinchZoom) gestures in the event data
     * and evaluates (by calling the onScale(...) method) if the user successfully reacted to an image or not.
     *
     * @param ev the MotionEvent
     * @return true if the touch event got consumed and should not be reacted on by the imageView
     */
    public boolean handleTouchEvent(MotionEvent ev) {
        if (started) {
            //filters out double tap gestures to prevent double tap to zoom functionality
            if (doubleTapDetector.onTouchEvent(ev)) {
                return true;
            }

            //finds initial down event to measure first reaction time
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                listener.userStartedReacting();
            }

            //resets first reaction time
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                listener.userAbortedReacting();
            }

            //triggers onScale(...) if ev is a scale event
            scaleGestureDetector.onTouchEvent(ev);

            return false; //added instead of the commented out code package above
        }
        return true; //if the provider is not started, don't react on touch events by zooming the image!
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float currZoom = listener.getCurrentZoom();

        //fixes a bug where the TouchImageView somehow changes its zoom
        //in the wrong direction even though it should not be possible since all touch events
        //should be catched before they arrive at the TouchImageView
        boolean error1 = (currZoom > startingZoom) && viewModel.getCurrentImage().getPush();
        boolean error2 = (currZoom < startingZoom) && !viewModel.getCurrentImage().getPush();
        if (error1 || error2) {
            listener.resetZoom();
            return true;
        }

        //call listener.userReacted(...) if the current zoom exceeds the trigger limit
        if (currZoom > upperTriggerLimit) {
            stop();
            long finalReactionTimeStamp = System.nanoTime();
            listener.userReacted(false, currZoom, finalReactionTimeStamp);
        } else if (currZoom < lowerTriggerLimit) {
            stop();
            long finalReactionTimeStamp = System.nanoTime();
            listener.userReacted(true, currZoom, finalReactionTimeStamp);
        }

        return true; //always return true to declare the event as handled
    }
}
