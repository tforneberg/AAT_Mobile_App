package de.tforneberg.aatapp.ui.util;

import android.view.animation.LinearInterpolator;

import com.ortiz.touchview.TouchImageView;

/**
 * The fade out animation for the displayed AAT images.
 */
public class FadeOutAnimation implements Runnable {

    private long startTime;
    private float zoomTime;
    private float zoom;
    private boolean zoomDirectionOut;
    private OnFadeOutFinishedListener listener;
    private TouchImageView imageView;
    private LinearInterpolator interpolator = new LinearInterpolator();

    public FadeOutAnimation(boolean zoomDirectionOut, TouchImageView imageView, float zoomTimeInMillis,
                            OnFadeOutFinishedListener finishedListener) {
        this.zoomTime = zoomTimeInMillis;
        this.imageView = imageView;
        this.zoomDirectionOut = zoomDirectionOut;
        this.listener = finishedListener;

        zoom = imageView.getCurrentZoom();
        startTime = System.currentTimeMillis();

        //start the actual animation
        imageView.postOnAnimation(this);
    }

    @Override
    public void run() {
        long currTime = System.currentTimeMillis();

        //determine how much of the animation time has already elapsed
        float elapsed = (currTime - startTime) / zoomTime;
        elapsed = Math.min(1f, elapsed);
        float t = interpolator.getInterpolation(elapsed);

        //zoom in or out depending on zoomDirectionOut or pull image
        if (zoomDirectionOut) {
            zoom = zoom * 0.975f;
        } else {
            zoom = zoom * 1.025f;
        }
        imageView.setZoom(zoom);

        //if zoomDirectionOut image, make image transparent
        if (zoomDirectionOut) {
            imageView.setAlpha(1 - t);
        }

        if (t < 1f) {
            //if animation time has not elapsed, yet: recursive call to run the animation again
            imageView.postOnAnimation(this);
        } else {
            //else: notify the listeners that the animation is finished
            if (listener != null) listener.onFadeOutFinished();
        }
    }

    /**
     * A Listener, whose onFadeOutFinished() method gets called when the FadeOutAnimation has finished.
     */
    public interface OnFadeOutFinishedListener {

        /**
         * This method gets called when the FadeOutAnimation has finished.
         */
        void onFadeOutFinished();
    }

}
